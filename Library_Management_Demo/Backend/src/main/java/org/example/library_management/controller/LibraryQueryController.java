package org.example.library_management.controller;

import org.example.library_management.security.JwtPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LibraryQueryController {
    private final JdbcTemplate jdbc;

    public LibraryQueryController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/catalog/resources")
    public List<Map<String, Object>> catalog(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String excludeType,
            @RequestParam(required = false, name = "q") String query,
            @AuthenticationPrincipal JwtPrincipal principal) {
        List<Object> values = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT resource_id AS resourceId, title, language, publisher_name AS publisherName,
                       genre, publish_year AS publishYear, resource_type AS resourceType,
                       standard_code AS standardCode, shelf_location AS shelfLocation, price,
                       subject_area AS subjectArea, authors, total_copies AS totalCopies,
                       available_copies AS availableCopies
                FROM vw_resource_catalog WHERE 1=1
                """);
        if (type != null && !type.isBlank()) {
            if (!List.of("Physical_Book", "Journal", "Open_Access").contains(type)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại tài liệu không hợp lệ.");
            }
            if ("Journal".equals(type) && !isMember(principal)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Đăng nhập thành viên còn hạn để xem tạp chí số.");
            }
            sql.append(" AND resource_type = ?");
            values.add(type);
        }
        if (!isMember(principal)) {
            sql.append(" AND resource_type <> 'Journal'");
        } else if (excludeType != null && !excludeType.isBlank()) {
            sql.append(" AND resource_type <> ?");
            values.add(excludeType);
        }
        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(COALESCE(authors,'')) LIKE LOWER(?))");
            values.add("%" + query.trim() + "%");
            values.add("%" + query.trim() + "%");
        }
        sql.append(" ORDER BY title");
        return jdbc.queryForList(sql.toString(), values.toArray());
    }

    @GetMapping("/catalog/resources/{resourceId}")
    public Map<String, Object> resource(@PathVariable long resourceId,
                                        @AuthenticationPrincipal JwtPrincipal principal) {
        Map<String, Object> resource = findResource(resourceId);
        if ("Journal".equals(resource.get("resourceType")) && !isMember(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Đăng nhập thành viên còn hạn để xem tạp chí số.");
        }
        if ("Journal".equals(resource.get("resourceType"))) {
            jdbc.update("INSERT INTO Access_Log (member_id, resource_id, access_time) VALUES (?, ?, CURRENT_TIMESTAMP)",
                    principal.identifier(), resourceId);
            resource.put("accessTime", jdbc.queryForObject("SELECT CURRENT_TIMESTAMP", java.time.LocalDateTime.class));
        }
        if ("Open_Access".equals(resource.get("resourceType"))) {
            resource.put("licenses", jdbc.queryForList("""
                    SELECT license_id AS licenseId, license_type AS licenseType,
                           license_description AS licenseDescription, can_download AS canDownload,
                           can_modify AS canModify, can_redistribute AS canRedistribute,
                           commercial_use AS commercialUse, granted_date AS grantedDate
                    FROM vw_open_access_resources WHERE resource_id = ?
                    """, resourceId));
        }
        return resource;
    }

    @GetMapping("/member/profile")
    public Map<String, Object> memberProfile(@AuthenticationPrincipal JwtPrincipal principal) {
        String memberId = principal.identifier();
        Map<String, Object> profile = jdbc.queryForMap("""
                SELECT member_id AS memberId, full_name AS fullName, email, join_date AS joinDate,
                       expiry_date AS expiryDate, account_status AS accountStatus,
                       is_valid_member AS validMember, current_borrowed_count AS currentBorrowedCount,
                       remaining_borrow_quota AS remainingBorrowQuota,
                       overdue_books_count AS overdueBooksCount, unpaid_fines_count AS unpaidFinesCount
                FROM vw_member_eligibility WHERE member_id = ?
                """, memberId);
        BigDecimal totalUnpaid = jdbc.queryForObject("""
                SELECT COALESCE(SUM(f.amount), 0)
                FROM Fine f
                JOIN Loaned_Copy lc ON lc.loaned_copy_id = f.loaned_copy_id
                JOIN Loan_Record lr ON lr.loan_id = lc.loan_id
                WHERE lr.member_id = ? AND f.status = 'Unpaid'
                """, BigDecimal.class, memberId);
        profile.put("totalUnpaidAmount", totalUnpaid == null ? BigDecimal.ZERO : totalUnpaid);
        return profile;
    }

    @GetMapping("/member/loans")
    public List<Map<String, Object>> memberLoans(@AuthenticationPrincipal JwtPrincipal principal) {
        return loansForMember(principal.identifier());
    }

    @GetMapping("/member/fines")
    public List<Map<String, Object>> memberFines(@AuthenticationPrincipal JwtPrincipal principal) {
        return jdbc.queryForList("""
                SELECT loaned_copy_id AS loanedCopyId, fine_id AS fineId,
                       book_title AS bookTitle, amount, reason, payment_status AS paymentStatus,
                       paid_date AS paidDate, due_date AS dueDate
                FROM vw_fine_details WHERE member_id = ? ORDER BY fine_id DESC
                """, principal.identifier());
    }

    @GetMapping("/member/journals/recent")
    public List<Map<String, Object>> recentJournals(@AuthenticationPrincipal JwtPrincipal principal) {
        return jdbc.queryForList("""
                SELECT al.access_id AS accessId, al.resource_id AS resourceId,
                       r.title AS title, j.issn, j.subject_area AS subjectArea,
                       al.access_time AS accessTime
                FROM Access_Log al
                JOIN Journal j ON j.resource_id = al.resource_id
                JOIN Resource r ON r.resource_id = j.resource_id
                WHERE al.member_id = ? ORDER BY al.access_time DESC LIMIT 20
                """, principal.identifier());
    }

    @GetMapping("/member/journals/{resourceId}")
    public Map<String, Object> viewJournal(@AuthenticationPrincipal JwtPrincipal principal,
                                           @PathVariable long resourceId) {
        Map<String, Object> resource = findResource(resourceId);
        if (!"Journal".equals(resource.get("resourceType"))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tạp chí.");
        }
        jdbc.update("INSERT INTO Access_Log (member_id, resource_id, access_time) VALUES (?, ?, CURRENT_TIMESTAMP)",
                principal.identifier(), resourceId);
        resource.put("accessTime", jdbc.queryForObject("SELECT CURRENT_TIMESTAMP", java.time.LocalDateTime.class));
        return resource;
    }

    Map<String, Object> findResource(long resourceId) {
        try {
            return jdbc.queryForMap("""
                    SELECT resource_id AS resourceId, title, language, publisher_name AS publisherName,
                           genre, publish_year AS publishYear, resource_type AS resourceType,
                           standard_code AS standardCode, shelf_location AS shelfLocation, price,
                           subject_area AS subjectArea, authors, total_copies AS totalCopies,
                           available_copies AS availableCopies
                    FROM vw_resource_catalog WHERE resource_id = ?
                    """, resourceId);
        } catch (org.springframework.dao.EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu.");
        }
    }

    static List<Map<String, Object>> loansForMember(JdbcTemplate jdbc, String memberId) {
        return jdbc.queryForList("""
                SELECT h.loan_id AS loanId, h.loaned_copy_id AS loanedCopyId,
                       h.member_id AS memberId, h.resource_id AS resourceId, h.book_title AS bookTitle,
                       h.copy_no AS copyNo, h.loan_date AS loanDate, h.due_date AS dueDate,
                       h.return_date AS returnDate,
                       CASE WHEN h.return_date IS NULL THEN 'Đang mượn' ELSE 'Đã trả' END AS statusLabel,
                       COALESCE(SUM(f.amount), 0) AS fineAmount,
                       COALESCE(SUM(CASE WHEN f.status = 'Unpaid' THEN f.amount ELSE 0 END), 0) AS unpaidFineAmount,
                       CASE WHEN SUM(CASE WHEN f.status = 'Unpaid' THEN 1 ELSE 0 END) > 0 THEN 'Đang nợ'
                            WHEN SUM(CASE WHEN f.status = 'Paid' THEN 1 ELSE 0 END) > 0 THEN 'Đã đóng'
                            ELSE 'Không có' END AS paymentStatus
                FROM vw_loan_history h
                LEFT JOIN Fine f ON f.loaned_copy_id = h.loaned_copy_id
                WHERE h.member_id = ?
                GROUP BY h.loan_id, h.loaned_copy_id, h.member_id, h.resource_id, h.book_title,
                         h.copy_no, h.loan_date, h.due_date, h.return_date
                ORDER BY h.loan_date DESC, h.loaned_copy_id DESC
                """, memberId);
    }

    private static boolean isMember(JwtPrincipal principal) {
        return principal != null && "Member".equals(principal.role());
    }

    List<Map<String, Object>> loansForMember(String memberId) {
        return loansForMember(jdbc, memberId);
    }
}
