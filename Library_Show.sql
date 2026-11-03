USE library_management;

-- =====================================================
-- 1. DANH SÁCH BẢNG & TRIGGER
-- =====================================================
SHOW TABLES;
SHOW TRIGGERS;

-- =====================================================
-- 2. SỐ DÒNG CHÍNH XÁC CỦA TỪNG BẢNG
-- =====================================================
SELECT 'person' AS ten_bang, COUNT(*) AS so_dong FROM person
UNION ALL SELECT 'member', COUNT(*) FROM member
UNION ALL SELECT 'author', COUNT(*) FROM author
UNION ALL SELECT 'librarian', COUNT(*) FROM librarian
UNION ALL SELECT 'resource', COUNT(*) FROM resource
UNION ALL SELECT 'physical_book', COUNT(*) FROM physical_book
UNION ALL SELECT 'journal', COUNT(*) FROM journal
UNION ALL SELECT 'open_access', COUNT(*) FROM open_access
UNION ALL SELECT 'resource_author', COUNT(*) FROM resource_author
UNION ALL SELECT 'license', COUNT(*) FROM license
UNION ALL SELECT 'access_license', COUNT(*) FROM access_license
UNION ALL SELECT 'book_copy', COUNT(*) FROM book_copy
UNION ALL SELECT 'loan_record', COUNT(*) FROM loan_record
UNION ALL SELECT 'loaned_copy', COUNT(*) FROM loaned_copy
UNION ALL SELECT 'fine', COUNT(*) FROM fine
UNION ALL SELECT 'access_log', COUNT(*) FROM access_log;

-- =====================================================
-- 3. CẤU TRÚC TỪNG BẢNG
-- =====================================================
DESCRIBE person;
DESCRIBE member;
DESCRIBE author;
DESCRIBE librarian;
DESCRIBE resource;
DESCRIBE physical_book;
DESCRIBE journal;
DESCRIBE open_access;
DESCRIBE resource_author;
DESCRIBE license;
DESCRIBE access_license;
DESCRIBE book_copy;
DESCRIBE loan_record;
DESCRIBE loaned_copy;
DESCRIBE fine;
DESCRIBE access_log;

-- =====================================================
-- 4. XEM 10 DÒNG ĐẦU CỦA TỪNG BẢNG
-- =====================================================
SELECT * FROM person LIMIT 10;
SELECT * FROM member LIMIT 10;
SELECT * FROM author LIMIT 10;
SELECT * FROM librarian LIMIT 10;
SELECT * FROM resource LIMIT 10;
SELECT * FROM physical_book LIMIT 10;
SELECT * FROM journal LIMIT 10;
SELECT * FROM open_access LIMIT 10;
SELECT * FROM resource_author LIMIT 10;
SELECT * FROM license;
SELECT * FROM access_license LIMIT 10;
SELECT * FROM book_copy LIMIT 10;
SELECT * FROM loan_record LIMIT 10;
SELECT * FROM loaned_copy LIMIT 10;
SELECT * FROM fine LIMIT 10;
SELECT * FROM access_log LIMIT 10;

-- =====================================================
-- 5. TRUY VẤN JOIN MẪU (dễ đọc hơn)
-- =====================================================

-- 5.1 Ai mượn cuốn nào, do thủ thư nào cho mượn
SELECT lr.loan_id,
       pm.full_name  AS nguoi_muon,
       r.title       AS ten_sach,
       lr.loan_date, lr.due_date, lc.return_date,
       pl.full_name  AS thu_thu
FROM loan_record lr
JOIN loaned_copy lc ON lc.loan_id = lr.loan_id
JOIN book_copy  bc ON bc.copy_id = lc.copy_id
JOIN resource   r  ON r.resource_id = bc.resource_id
JOIN person     pm ON pm.person_id = lr.member_id
JOIN person     pl ON pl.person_id = lr.librarian_id
ORDER BY lr.loan_id
LIMIT 20;

-- 5.2 Danh sách phạt kèm người bị phạt
SELECT f.fine_id, p.full_name AS nguoi_bi_phat, r.title AS ten_sach,
       lc.return_date, lr.due_date, f.amount, f.paid_status
FROM fine f
JOIN loaned_copy lc ON lc.loaned_copy_id = f.loaned_copy_id
JOIN loan_record lr ON lr.loan_id = lc.loan_id
JOIN person p       ON p.person_id = lr.member_id
JOIN book_copy bc   ON bc.copy_id = lc.copy_id
JOIN resource r     ON r.resource_id = bc.resource_id
LIMIT 20;

-- 5.3 Sách và tác giả
SELECT r.resource_id, r.title, GROUP_CONCAT(p.full_name SEPARATOR ', ') AS tac_gia
FROM resource r
JOIN resource_author ra ON ra.resource_id = r.resource_id
JOIN person p           ON p.person_id = ra.author_id
GROUP BY r.resource_id, r.title
LIMIT 20;

-- 5.3b Phân loại resource (mỗi resource đúng 1 loại)
SELECT 'physical_book' AS loai, COUNT(*) AS so_luong FROM physical_book
UNION ALL SELECT 'journal', COUNT(*) FROM journal
UNION ALL SELECT 'open_access', COUNT(*) FROM open_access;

-- 5.4 Số bản sao theo trạng thái
SELECT status, COUNT(*) AS so_luong FROM book_copy GROUP BY status;

-- 5.5 Top 10 người mượn nhiều nhất
SELECT p.full_name, COUNT(*) AS so_lan_muon
FROM loan_record lr
JOIN person p ON p.person_id = lr.member_id
GROUP BY p.person_id, p.full_name
ORDER BY so_lan_muon DESC
LIMIT 10;

-- 5.6 Người vừa là member vừa là author / librarian (overlap)
SELECT p.person_id, p.full_name,
       (m.member_id IS NOT NULL)    AS la_member,
       (a.author_id IS NOT NULL)    AS la_author,
       (l.librarian_id IS NOT NULL) AS la_librarian
FROM person p
LEFT JOIN member m    ON m.member_id = p.person_id
LEFT JOIN author a    ON a.author_id = p.person_id
LEFT JOIN librarian l ON l.librarian_id = p.person_id
WHERE (m.member_id IS NOT NULL) + (a.author_id IS NOT NULL) + (l.librarian_id IS NOT NULL) > 1;

-- 5.7 Giấy phép của các tài liệu open access
SELECT oa.resource_id, oa.doi, l.license_type, al.granted_date
FROM access_license al
JOIN open_access oa ON oa.resource_id = al.resource_id
JOIN license l      ON l.license_id = al.license_id
LIMIT 20;

-- 5.8 Lịch sử truy cập tạp chí
SELECT al.access_id, p.full_name, r.title, al.access_time
FROM access_log al
JOIN person p   ON p.person_id = al.member_id
JOIN resource r ON r.resource_id = al.resource_id
ORDER BY al.access_time DESC
LIMIT 20;