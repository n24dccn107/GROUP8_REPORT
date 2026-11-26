package org.example.library_management.controller;

import org.example.library_management.security.JwtPrincipal;
import org.example.library_management.service.LibrarianService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/librarian")
public class LibrarianController {
    private static final List<String> COPY_STATUSES = List.of("Available", "Borrowed", "Damaged", "Lost");

    private final JdbcTemplate jdbc;
    private final LibrarianService librarianService;

    public LibrarianController(JdbcTemplate jdbc, LibrarianService librarianService) {
        this.jdbc = jdbc;
        this.librarianService = librarianService;
    }

    @GetMapping("/members")
    public List<Map<String, Object>> members() {
        return jdbc.queryForList("""
                SELECT v.member_id AS memberId, v.full_name AS fullName, v.email, v.expiry_date AS expiryDate,
                       v.account_status AS accountStatus, v.current_borrowed_count AS currentBorrowedCount,
                       v.overdue_books_count AS overdueBooksCount,
                       COALESCE((SELECT SUM(f.amount) FROM Fine f
                           JOIN Loaned_Copy lc ON lc.loaned_copy_id=f.loaned_copy_id
                           JOIN Loan_Record lr ON lr.loan_id=lc.loan_id
                           WHERE lr.member_id=v.member_id AND f.status='Unpaid'),0) AS totalUnpaidAmount
                FROM vw_member_eligibility v ORDER BY v.overdue_books_count DESC, v.member_id 
                """);
    }

    @GetMapping("/members/{memberId}/profile")
    public Map<String, Object> memberProfile(@PathVariable String memberId) {
        try {
            return jdbc.queryForMap("""
                    SELECT member_id AS memberId, full_name AS fullName, email, join_date AS joinDate,
                           expiry_date AS expiryDate, account_status AS accountStatus,
                           is_valid_member AS validMember, current_borrowed_count AS currentBorrowedCount,
                           remaining_borrow_quota AS remainingBorrowQuota,
                           overdue_books_count AS overdueBooksCount, unpaid_fines_count AS unpaidFinesCount,
                           COALESCE((SELECT SUM(f.amount) FROM Fine f
                               JOIN Loaned_Copy lc ON lc.loaned_copy_id=f.loaned_copy_id
                               JOIN Loan_Record lr ON lr.loan_id=lc.loan_id
                               WHERE lr.member_id=vw_member_eligibility.member_id AND f.status='Unpaid'),0) AS totalUnpaidAmount
                    FROM vw_member_eligibility WHERE member_id=?
                    """, memberId);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thành viên.");
        }
    }

    @GetMapping("/members/{memberId}/loans")
    public List<Map<String, Object>> memberLoans(@PathVariable String memberId) {
        return LibraryQueryController.loansForMember(jdbc, memberId);
    }

    @GetMapping("/copies")
    public List<Map<String, Object>> copies(@RequestParam(required = false) String status) {
        if (status != null && !"All".equalsIgnoreCase(status) && !COPY_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái bản sao không hợp lệ.");
        }
        String sql = """
                SELECT bc.resource_id AS resourceId, rc.title, rc.authors,
                       bc.copy_no AS copyNo, bc.status AS copyStatus,
                       bc.copy_condition AS copyCondition, lr.member_id AS memberId,
                       lr.due_date AS dueDate
                FROM Book_Copy bc
                JOIN vw_resource_catalog rc ON rc.resource_id = bc.resource_id
                LEFT JOIN Loaned_Copy lc ON lc.resource_id = bc.resource_id
                    AND lc.copy_no = bc.copy_no AND lc.return_date IS NULL
                LEFT JOIN Loan_Record lr ON lr.loan_id = lc.loan_id
                """;
        boolean filterStatus = status != null && !"All".equalsIgnoreCase(status);
        if (filterStatus) sql += " WHERE bc.status = ?";
        sql += " ORDER BY FIELD(bc.status, 'Borrowed', 'Available', 'Damaged', 'Lost'), rc.title, bc.copy_no LIMIT 1000";
        return filterStatus ? jdbc.queryForList(sql, status) : jdbc.queryForList(sql);
    }

    @GetMapping("/fines")
    public List<Map<String, Object>> fines(@RequestParam(required = false) String status) {
        if (status != null && !List.of("Paid", "Unpaid").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái phạt không hợp lệ.");
        }
        String sql = """
                SELECT loaned_copy_id AS loanedCopyId, fine_id AS fineId, member_id AS memberId,
                       member_name AS memberName, book_title AS bookTitle, amount, reason,
                       payment_status AS paymentStatus, paid_date AS paidDate,
                       collected_by_librarian AS collectedByLibrarian
                FROM vw_fine_details
                """;
        if (status != null) sql += " WHERE payment_status=?";
        sql += " ORDER BY payment_status, member_id, fine_id DESC LIMIT 500";
        return status == null ? jdbc.queryForList(sql) : jdbc.queryForList(sql, status);
    }

    @PostMapping("/loans")
    public Map<String, Object> borrow(@AuthenticationPrincipal JwtPrincipal principal,
                                      @RequestBody BorrowRequest request) {
        librarianService.borrow(principal.identifier(), request.memberId(), request.copies());
        return Map.of("copyCount", request.copies().size(), "status", "Created", "procedure", "borrow_book");
    }

    @PostMapping("/loans/copies/{loanedCopyId}/return")
    public Map<String, String> returnCopy(@AuthenticationPrincipal JwtPrincipal principal,
                                          @PathVariable long loanedCopyId) {
        librarianService.returnCopy(principal.identifier(), loanedCopyId);
        return Map.of("status", "Returned");
    }

    @PatchMapping("/copies/{resourceId}/{copyNo}/condition")
    public Map<String, String> updateCopyCondition(@AuthenticationPrincipal JwtPrincipal principal,
                                                    @PathVariable long resourceId,
                                                    @PathVariable int copyNo,
                                                    @RequestBody CopyConditionRequest request) {
        librarianService.setCopyCondition(principal.identifier(), resourceId, copyNo, request.condition());
        return Map.of("status", request.condition());
    }

    @PostMapping("/fines/{loanedCopyId}/{fineId}/pay")
    public Map<String, String> payFine(@AuthenticationPrincipal JwtPrincipal principal,
                                       @PathVariable long loanedCopyId,
                                       @PathVariable long fineId) {
        librarianService.payFine(principal.identifier(), loanedCopyId, fineId);
        return Map.of("status", "Paid");
    }

    public record BorrowRequest(String memberId, List<LibrarianService.BorrowCopy> copies) { }
    public record CopyConditionRequest(String condition) { }
}
