# Excel to JSON Migration Tool

## 📋 Giới thiệu

Đây là công cụ migration sử dụng **Spring Batch** để chuyển đổi dữ liệu từ file Excel sang định dạng JSON.

Tool hỗ trợ xử lý dữ liệu lớn theo batch, theo dõi tiến trình và lưu metadata vào database.

---

## 🚀 Tính năng chính

- Đọc dữ liệu từ file Excel (.xlsx) bằng Apache POI
- Chuyển đổi dữ liệu từ Excel sang JSON
- Xuất dữ liệu ra file JSON
- Xử lý batch theo cơ chế chunk-based processing
- Theo dõi tiến trình xử lý
- Lưu metadata vào database (H2 / JPA)
- Validation dữ liệu tự động
- Logging chi tiết + error handling
- REST API trigger migration

---

## 🛠️ Công nghệ sử dụng

| Công nghệ | Version | Mục đích |
|----------|--------|----------|
| Spring Boot | 3.1.5 | Framework chính |
| Spring Batch | 5.x | Xử lý batch |
| Apache POI | 5.2.3 | Đọc file Excel |
| Jackson | 2.x | Xử lý JSON |
| H2 Database | 2.x | Lưu metadata |
| Lombok | 1.18.x | Giảm boilerplate |
| Maven | 3.x | Build tool |

---

## 📦 Cài đặt

### Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- Git

---

### Clone project

```bash
git clone https: https://github.com/ThienDuc1112/spring_batch.git
cd migration
