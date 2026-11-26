# Hệ thống quản lý thư viện

Dự án xây dựng cơ sở dữ liệu cho hệ thống quản lý thư viện, bao gồm phân tích yêu cầu, thiết kế mô hình dữ liệu, xây dựng lược đồ quan hệ, triển khai cơ sở dữ liệu bằng SQL và tích hợp ứng dụng.

## Tổng quan dự án

Mục tiêu của dự án là thiết kế và triển khai một cơ sở dữ liệu đáp ứng các nghiệp vụ chính của hệ thống thư viện, từ quản lý thông tin cá nhân, thành viên, tài nguyên thư viện đến hoạt động mượn – trả sách, tiền phạt và truy cập tài nguyên số.

Dự án được chia thành bốn giai đoạn:

## Thành viên nhóm

| Mã sinh viên | Tên thành viên |
|---|---|
| N24DCCN107 | Trương Phước Đông |
| N24DCCN101 | Nguyễn Hữu Đại |
| N24DCCN098 | Trình Gia Bảo |

## Các giai đoạn thực hiện

| Giai đoạn | Thời gian | Nội dung chính |
|---|---|---|
| **Phase 1: Thiết kế khái niệm** | Tuần 3–4 | Xác định vấn đề, các quy tắc nghiệp vụ và xây dựng mô hình ER/EER tổng thể. |
| **Phase 2: Thiết kế logic và chuẩn hóa** | Tuần 5–8 | Ánh xạ mô hình ER/EER sang lược đồ quan hệ, xác định khóa ngoại và chứng minh chuẩn hóa (3NF/BCNF). |
| **Phase 3: Triển khai và truy vấn** | Tuần 9–10 | Xây dựng SQL DDL, thêm dữ liệu mẫu bằng DML, viết các truy vấn phức tạp (JOIN, truy vấn con, hàm tổng hợp) và triển khai Trigger/View. |
| **Phase 4: Tích hợp và bảo vệ** | Tuần 11–12 | Tích hợp giao diện và backend, chuẩn bị nội dung trình bày dự án, thuyết trình nhóm và đánh giá đồng đẳng. |

## Các nhánh của repository

Repository được tổ chức theo từng giai đoạn của dự án:

- `main` — Nhánh chính của dự án.
- `phase1` — Thiết kế khái niệm (Conceptual Design).
- `phase2` — Thiết kế logic và chuẩn hóa (Logical Design & Normalization).
- `phase3` — Triển khai cơ sở dữ liệu và truy vấn (Implementation & Queries).
- `phase4` — Tích hợp và bảo vệ dự án (Integration & Defense).

## Công nghệ và nội dung

- Thiết kế cơ sở dữ liệu quan hệ.
- Mô hình hóa dữ liệu ER/EER.
- SQL: DDL, DML và các câu truy vấn.
- Các phép kết nối bảng, truy vấn con và hàm tổng hợp.
- Chuẩn hóa cơ sở dữ liệu: 3NF và BCNF.
- Ràng buộc khóa chính, khóa ngoại và các ràng buộc toàn vẹn dữ liệu.
- Trigger và View.
- Tích hợp giao diện và backend bằng các công nghệ như Python/Flask, Node.js hoặc Java.

## Sản phẩm của dự án

Repository lưu trữ tài liệu phân tích và thiết kế, mô hình ER/EER, lược đồ quan hệ, các script SQL, dữ liệu mẫu, truy vấn và các thành phần phục vụ tích hợp ứng dụng.

---

**Dự án học phần Cơ sở dữ liệu – Hệ thống quản lý thư viện.**
