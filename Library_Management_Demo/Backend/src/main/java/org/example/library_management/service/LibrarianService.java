package org.example.library_management.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LibrarianService {
    private final JdbcTemplate jdbc;

    public LibrarianService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void borrow(String librarianId, String memberId, List<BorrowCopy> copies) {
        verifyActiveLibrarian(librarianId);
        if (memberId == null || memberId.isBlank() || copies == null || copies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã thành viên và ít nhất một bản sao là bắt buộc.");
        }
        if (memberId.trim().length() > 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã thành viên tối đa 10 ký tự.");
        }
        if (copies.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Procedure borrow_book tạo một phiếu mượn cho một bản sao mỗi lần.");
        }
        BorrowCopy copy = copies.getFirst();
        if (copy == null || copy.resourceId() <= 0 || copy.copyNo() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bản sao sách không hợp lệ.");
        }

        try {
            callBorrowBookProcedure(memberId.trim(), librarianId, copy);
        } catch (DataAccessException exception) {
            throw mapBorrowProcedureFailure(exception);
        }
    }

    private void callBorrowBookProcedure(String memberId, String librarianId, BorrowCopy copy) {
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            try (CallableStatement statement = connection.prepareCall("{CALL borrow_book(?, ?, ?, ?)}")) {
                statement.setString(1, memberId);
                statement.setString(2, librarianId);
                statement.setLong(3, copy.resourceId());
                statement.setInt(4, copy.copyNo());

                boolean hasResults = statement.execute();
                while (true) {
                    if (hasResults) {
                        try (ResultSet resultSet = statement.getResultSet()) {
                            while (resultSet != null && resultSet.next()) { }
                        }
                    } else if (statement.getUpdateCount() == -1) {
                        break;
                    }
                    hasResults = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
                }
            }
            return null;
        });
    }

    private ResponseStatusException mapBorrowProcedureFailure(DataAccessException exception) {
        String message = exception.getMostSpecificCause().getMessage();
        String normalized = message == null ? "" : message.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("member does not exist")) {
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành viên.", exception);
        }
        if (normalized.contains("membership has expired")) {
            return new ResponseStatusException(HttpStatus.FORBIDDEN, "Thẻ thành viên đã hết hạn.", exception);
        }
        if (normalized.contains("borrowing limit exceeded")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Thành viên đã đạt giới hạn 5 bản sao.", exception);
        }
        if (normalized.contains("overdue books")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Thành viên còn sách quá hạn chưa trả.", exception);
        }
        if (normalized.contains("unpaid fines")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Thành viên còn tiền phạt chưa thanh toán.", exception);
        }
        if (normalized.contains("book copy is not available")) {
            return new ResponseStatusException(HttpStatus.CONFLICT, "Bản sao không ở trạng thái có sẵn.", exception);
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Không thể gọi procedure borrow_book. Hãy kiểm tra procedure trong database.", exception);
    }

    @Transactional
    public void returnCopy(String librarianId, long loanedCopyId) {
        verifyActiveLibrarian(librarianId);
        Map<String, Object> item;
        try {
            item = jdbc.queryForMap("""
                    SELECT lc.loaned_copy_id AS loanedCopyId, lc.return_date AS returnDate,
                           lc.resource_id AS resourceId, lc.copy_no AS copyNo,
                           lr.member_id AS memberId
                    FROM Loaned_Copy lc JOIN Loan_Record lr ON lr.loan_id=lc.loan_id
                    JOIN Physical_Book pb ON pb.resource_id=lc.resource_id
                    WHERE lc.loaned_copy_id=? FOR UPDATE
                    """, loanedCopyId);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy lượt mượn.");
        }
        if (item.get("returnDate") != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bản sao này đã được trả.");
        }
        refreshMemberFines(String.valueOf(item.get("memberId")), databaseDate());
        jdbc.update("""
                UPDATE Loaned_Copy SET return_date=CURRENT_DATE, return_librarian_id=?
                WHERE loaned_copy_id=? AND return_date IS NULL
                """, librarianId, loanedCopyId);
        String condition = jdbc.queryForObject("""
                SELECT copy_condition FROM Book_Copy WHERE resource_id=? AND copy_no=? FOR UPDATE
                """, String.class, item.get("resourceId"), item.get("copyNo"));
        String nextStatus = "Lost".equals(condition) ? "Lost" : "Damaged".equals(condition) ? "Damaged" : "Available";
        jdbc.update("UPDATE Book_Copy SET status=? WHERE resource_id=? AND copy_no=?",
                nextStatus, item.get("resourceId"), item.get("copyNo"));
    }

    @Transactional
    public void setCopyCondition(String librarianId, long resourceId, int copyNo, String condition) {
        verifyActiveLibrarian(librarianId);
        if (!Set.of("New", "Good", "Fair", "Damaged", "Lost").contains(condition)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tình trạng bản sao không hợp lệ.");
        }
        Map<String, Object> copy;
        try {
            copy = jdbc.queryForMap("""
                    SELECT bc.status, pb.price FROM Book_Copy bc
                    JOIN Physical_Book pb ON pb.resource_id=bc.resource_id
                    WHERE bc.resource_id=? AND bc.copy_no=? FOR UPDATE
                    """, resourceId, copyNo);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bản sao.");
        }
        Integer activeLoanCount = jdbc.queryForObject("""
                SELECT COUNT(*) FROM Loaned_Copy
                WHERE resource_id=? AND copy_no=? AND return_date IS NULL
                """, Integer.class, resourceId, copyNo);
        String status = condition.equals("Lost") ? "Lost" : condition.equals("Damaged") ? "Damaged"
                : activeLoanCount != null && activeLoanCount > 0 ? "Borrowed" : "Available";
        jdbc.update("UPDATE Book_Copy SET copy_condition=?, status=? WHERE resource_id=? AND copy_no=?",
                condition, status, resourceId, copyNo);
        if (condition.equals("Lost") || condition.equals("Damaged")) {
            List<Long> activeCopies = jdbc.queryForList("""
                    SELECT loaned_copy_id FROM Loaned_Copy
                    WHERE resource_id=? AND copy_no=? AND return_date IS NULL
                    """, Long.class, resourceId, copyNo);
            BigDecimal price = new BigDecimal(String.valueOf(copy.get("price")));
            for (Long activeCopy : activeCopies) recordFine(activeCopy, "Replacement cost", price);
        }
    }

    @Transactional
    public void payFine(String librarianId, long loanedCopyId, long fineId) {
        verifyActiveLibrarian(librarianId);
        int updated = jdbc.update("""
                UPDATE Fine SET status='Paid', paid_date=CURRENT_DATE, librarian_id=?
                WHERE loaned_copy_id=? AND fine_id=? AND status='Unpaid'
                """, librarianId, loanedCopyId, fineId);
        if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khoản phạt chưa thanh toán.");
    }

    @Transactional
    public int refreshAllOverdueFines() {
        LocalDate today = databaseDate();
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT lc.loaned_copy_id AS loanedCopyId, lr.due_date AS dueDate,
                       pb.price AS price, bc.status AS copyStatus
                FROM Loaned_Copy lc JOIN Loan_Record lr ON lr.loan_id=lc.loan_id
                JOIN Book_Copy bc ON bc.resource_id=lc.resource_id AND bc.copy_no=lc.copy_no
                JOIN Physical_Book pb ON pb.resource_id=lc.resource_id
                WHERE lc.return_date IS NULL
                  AND ((lr.due_date < CURRENT_DATE AND bc.status='Borrowed')
                       OR bc.status IN ('Damaged','Lost'))
                FOR UPDATE
                """);
        for (Map<String, Object> row : rows) refreshOneFine(row, today);
        return rows.size();
    }

    private void refreshMemberFines(String memberId, LocalDate today) {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT lc.loaned_copy_id AS loanedCopyId, lr.due_date AS dueDate,
                       pb.price AS price, bc.status AS copyStatus
                FROM Loaned_Copy lc JOIN Loan_Record lr ON lr.loan_id=lc.loan_id
                JOIN Book_Copy bc ON bc.resource_id=lc.resource_id AND bc.copy_no=lc.copy_no
                JOIN Physical_Book pb ON pb.resource_id=lc.resource_id
                WHERE lr.member_id=? AND lc.return_date IS NULL
                  AND ((lr.due_date < CURRENT_DATE AND bc.status='Borrowed')
                       OR bc.status IN ('Damaged','Lost'))
                FOR UPDATE
                """, memberId);
        for (Map<String, Object> row : rows) refreshOneFine(row, today);
    }

    private void refreshOneFine(Map<String, Object> row, LocalDate today) {
        long loanedCopyId = ((Number) row.get("loanedCopyId")).longValue();
        BigDecimal price = new BigDecimal(String.valueOf(row.get("price")));
        String status = String.valueOf(row.get("copyStatus"));
        if (status.equals("Damaged") || status.equals("Lost")) {
            recordFine(loanedCopyId, "Replacement cost", price);
            return;
        }
        LocalDate dueDate = asLocalDate(row.get("dueDate"));
        long overdueDays = today.toEpochDay() - dueDate.toEpochDay();
        if (overdueDays > 0) {
            BigDecimal amount = price.min(BigDecimal.valueOf(overdueDays).multiply(BigDecimal.valueOf(5000)));
            recordFine(loanedCopyId, "Overdue", amount);
        }
    }

    private void recordFine(long loanedCopyId, String reason, BigDecimal amount) {
        List<Long> pending = jdbc.queryForList("""
                SELECT fine_id FROM Fine WHERE loaned_copy_id=? AND reason=? AND status='Unpaid'
                ORDER BY fine_id DESC LIMIT 1 FOR UPDATE
                """, Long.class, loanedCopyId, reason);
        if (!pending.isEmpty()) {
            jdbc.update("UPDATE Fine SET amount=? WHERE loaned_copy_id=? AND fine_id=?",
                    amount, loanedCopyId, pending.getFirst());
            return;
        }
        Long nextFineId = jdbc.queryForObject("SELECT COALESCE(MAX(fine_id),0)+1 FROM Fine WHERE loaned_copy_id=?",
                Long.class, loanedCopyId);
        jdbc.update("""
                INSERT INTO Fine (loaned_copy_id, fine_id, amount, reason, status)
                VALUES (?, ?, ?, ?, 'Unpaid')
                """, loanedCopyId, nextFineId, amount, reason);
    }

    private void verifyActiveLibrarian(String librarianId) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM Librarian l
                JOIN Account a ON a.person_id=l.person_id AND a.role='Librarian'
                WHERE l.librarian_id=? AND l.Leave_date IS NULL AND a.status='Active'
                """, Integer.class, librarianId);
        if (count == null || count != 1) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản thủ thư không hoạt động.");
    }

    private LocalDate databaseDate() {
        return jdbc.queryForObject("SELECT CURRENT_DATE", LocalDate.class);
    }

    private static LocalDate asLocalDate(Object value) {
        if (value instanceof LocalDate localDate) return localDate;
        if (value instanceof Date sqlDate) return sqlDate.toLocalDate();
        return LocalDate.parse(String.valueOf(value));
    }

    public record BorrowCopy(long resourceId, int copyNo) { }
}
