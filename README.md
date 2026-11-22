# Hệ Thống Quản Lý Đặt Vé Xem Phim (Java OOP & MySQL Web Application)

> **Môn học:** Lập trình Hướng đối tượng (Java OOP)  
> **Kiến trúc:** Phân tầng sạch sẽ (Clean Layered Architecture: Model - Repository - Service - Controller/UI)  
> **Công nghệ:** Java thuần (JDK có sẵn, `HttpServer`) + MySQL Database (`mysql-connector-j` trong `lib/`) + Giao diện Web (HTML5/CSS3/Vanilla JS)  
> **Hỗ trợ 2 giao diện:** Giao diện Web (`main.WebServer`) và Giao diện Console Terminal (`main.Main`) dùng chung toàn bộ tầng Service & Model.

---

## 🌟 Tính Năng Nổi Bật Sau Khi Nâng Cấp

1. **Cơ sở dữ liệu MySQL (`cinema_booking_db`):**
   - 5 bảng chuẩn hóa có quan hệ khóa ngoại chặt chẽ: `movies`, `customers`, `showtimes`, `seats`, `tickets`.
   - Lưu trữ trực tiếp mọi biến động: Đặt vé mới, cập nhật ghế `BOOKED`, thanh toán `PAID`, thêm khách hàng mới.
   - Dữ liệu mẫu khởi tạo phong phú: 8 bộ phim, 10 suất chiếu, sơ đồ 44 ghế chuẩn cho mỗi suất chiếu, 5 khách hàng mẫu đủ 3 đối tượng, vé mẫu đã đặt.

2. **Backend Web Server thuần Java (`src/main/WebServer.java`):**
   - Sử dụng `com.sun.net.httpserver.HttpServer` tích hợp sẵn trong JDK, không phụ thuộc framework nặng nề (không dùng Spring Boot, Hibernate).
   - RESTful API JSON thuần (`JsonUtil.java`), xử lý CORS, MIME types và phục vụ file tĩnh trong thư mục `web/`.
   - Xử lý các quy tắc nghiệp vụ chặt chẽ, ném `BookingException` và trả về mã lỗi HTTP 400 kèm thông báo tiếng Việt thân thiện.

3. **Giao diện Web hiện đại (`web/`):**
   - Áp dụng nghiêm ngặt bảng màu 3 tông màu: **Trắng** (`#FFFFFF`, `#F8FAFC`), **Đen** (`#0F172A`, `#1E293B`), **Xanh dương** (`#2563EB`, `#1D4ED8`, `#DBEAFE`).
   - 5 tab chức năng:
     1. **Phim:** Hiển thị dạng lưới thẻ (card grid) 8 bộ phim, bộ lọc trạng thái (Tất cả / Đang chiếu / Ngừng chiếu), tìm kiếm realtime.
     2. **Đặt vé & Sơ đồ ghế:** Sơ đồ 44 ghế trực quan (Màn hình chiếu cong ở trên, hàng A-E cụm 4-4, hàng F ghế đôi cụm 2-2), tính giá vé tự động theo loại khách và phụ phí ghế, biên lai vé khi đặt thành công.
     3. **Thanh toán:** Danh sách vé chưa thanh toán, hỗ trợ 3 hình thức thanh toán (Tiền mặt, Chuyển khoản Vietcombank, Ví MoMo).
     4. **Khách hàng:** Hồ sơ danh sách khách hàng và form thêm khách hàng mới.
     5. **Vé & Doanh thu:** 4 thẻ thống kê (Tổng vé, Đã TT, Chưa TT, Doanh thu thực) và bảng quản lý toàn bộ vé đã đặt.

---

## 📁 Cấu Trúc Dự Án

```
CuoiKy-main/
├── database/
│   └── schema.sql                 # Script khởi tạo database cinema_booking_db và dữ liệu mẫu
├── lib/
│   └── mysql-connector-j-8.3.0.jar# Thư viện MySQL JDBC Driver thuần Java
├── src/
│   ├── model/                     # Giữ nguyên 100% các class dữ liệu & interface OOP
│   │   ├── User.java              # Abstract Base Class (Encapsulation, Abstraction)
│   │   ├── Customer.java          # Kế thừa User (Inheritance)
│   │   ├── CustomerType.java      # Enum: NORMAL, STUDENT, VIP
│   │   ├── Movie.java             # Phim (Mã, tên, thể loại, thời lượng, độ tuổi, trạng thái)
│   │   ├── MovieStatus.java       # Enum: SHOWING (Đang chiếu), STOPPED (Ngừng chiếu)
│   │   ├── Seat.java              # Ghế ngồi phòng chiếu
│   │   ├── SeatType.java          # Enum: NORMAL (+0đ), VIP (+20.000đ), COUPLE (+50.000đ)
│   │   ├── SeatStatus.java        # Enum: AVAILABLE (Trống), BOOKED (Đã đặt)
│   │   ├── Showtime.java          # Suất chiếu kèm danh sách 44 ghế
│   │   ├── Ticket.java            # Vé xem phim
│   │   ├── TicketPricePolicy.java # Interface chính sách giá (Strategy Pattern & Polymorphism)
│   │   ├── NormalPrice.java       # Khách thường (giá gốc +0%)
│   │   ├── StudentPrice.java      # Sinh viên (giảm 20%)
│   │   ├── VipPrice.java          # Thành viên VIP (giảm 15%)
│   │   ├── PaymentMethod.java     # Interface thanh toán (Polymorphism)
│   │   ├── CashPayment.java       # Thanh toán tiền mặt tại quầy
│   │   ├── BankTransferPayment.java # Thanh toán chuyển khoản Vietcombank
│   │   └── EWalletPayment.java    # Thanh toán ví điện tử MoMo
│   │
│   ├── service/                   # Giữ nguyên 100% logic nghiệp vụ
│   │   ├── BookingException.java  # Exception nghiệp vụ duy nhất với thông báo tiếng Việt
│   │   ├── MovieService.java      # Nghiệp vụ quản lý & tìm kiếm phim
│   │   ├── CustomerService.java   # Nghiệp vụ quản lý khách hàng & chính sách giá
│   │   ├── ShowtimeService.java   # Nghiệp vụ suất chiếu & sơ đồ ghế
│   │   └── BookingService.java    # Nghiệp vụ đặt vé, tính giá, thanh toán, kiểm tra rule
│   │
│   ├── repository/                # Tầng truy cập dữ liệu kết nối MySQL qua JDBC
│   │   ├── DatabaseConnection.java# Kết nối MySQL DriverManager với hằng số tách riêng
│   │   ├── MovieRepository.java   # Truy vấn bảng movies bằng PreparedStatement
│   │   ├── CustomerRepository.java# Truy vấn & lưu bảng customers, đồng bộ IdGenerator
│   │   ├── ShowtimeRepository.java# Truy vấn bảng showtimes & seats, cập nhật ghế
│   │   └── TicketRepository.java  # Truy vấn & lưu bảng tickets, cập nhật trạng thái thanh toán
│   │
│   ├── utils/                     # Tiện ích dùng chung
│   │   ├── IdGenerator.java       # Tự động sinh mã vé (TK001...), mã KH (KH001...)
│   │   ├── Input.java             # Đọc dữ liệu Scanner an toàn cho Console
│   │   └── JsonUtil.java          # Bộ tiện ích serialize & parse JSON thuần Java
│   │
│   └── main/
│       ├── Main.java              # Giao diện Console Terminal
│       └── WebServer.java         # Backend Web Server (HttpServer port 8080)
│
├── web/                           # Giao diện Web Client
│   ├── index.html                 # Cấu trúc HTML5 hiện đại (5 tab, modal, bảng, sơ đồ ghế)
│   ├── style.css                  # Bảng màu 3 tông Trắng - Đen - Xanh dương
│   └── app.js                     # Xử lý tương tác AJAX REST API Vanilla JS
│
├── run.sh                         # Script biên dịch và chạy trên Linux / Ubuntu / macOS
├── build.bat                      # Script biên dịch và chạy trên Windows
└── README.md
```

---

## 🎯 4 Tính Chất OOP & Design Pattern Khi Thuyết Minh Bảo Vệ

| Tính chất OOP / Pattern | Thể hiện cụ thể trong Dự Án |
|---|---|
| **Encapsulation (Đóng gói)** | Tất cả thuộc tính trong các lớp `model/` (`User`, `Customer`, `Movie`, `Showtime`, `Seat`, `Ticket`) đều đặt `private`, truy xuất thông qua các getter/setter và phương thức nghiệp vụ đóng gói logic nội tại (như `ticket.markPaid(method)`, `seat.isAvailable()`). |
| **Inheritance (Kế thừa)** | Lớp `Customer` kế thừa từ lớp cha trừu tượng `User` (`super(id, fullName, phone, email)`), tái sử dụng toàn bộ thông tin người dùng và bổ sung thuộc tính loại khách `CustomerType`. |
| **Abstraction (Trừu tượng)** | Lớp cơ sở trừu tượng `User` với phương thức abstract `getRole()`. Hai interface `TicketPricePolicy` và `PaymentMethod` ẩn chi tiết cài đặt, định nghĩa hợp đồng chung cho hệ thống. |
| **Polymorphism & Strategy Pattern** | <br>1. **Chính sách tính giá vé (`TicketPricePolicy`):** Lớp `BookingService` và `CustomerService` tính giá thông qua interface `calculate(basePrice, seatType)`. Tùy theo đối tượng khách hàng mà tự động kích hoạt `NormalPrice` (0%), `StudentPrice` (-20%), hoặc `VipPrice` (-15%).<br>2. **Phương thức thanh toán (`PaymentMethod`):** Gọi chung qua phương thức `pay(amount)`, áp dụng linh hoạt cho `CashPayment`, `BankTransferPayment`, hoặc `EWalletPayment`. |
| **Clean Layered Architecture** | Tách bạch rõ rệt giữa: Model (Dữ liệu), Repository (Truy vấn CSDL), Service (Quy tắc nghiệp vụ), Controller/Presentation (Web Server & Console UI). Tầng Service dùng chung 100% cho cả 2 giao diện. |

---

## 📋 Quy Tắc Nghiệp Vụ (Business Rules) Được Kiểm Thử Tự Động

- **BR-01 (Chặn phim ngừng chiếu):** Không cho phép đặt vé cho phim có trạng thái `STOPPED` (Ném `BookingException`).
- **BR-02 (Chính sách ưu đãi giá):** Giá vé cuối cùng được áp dụng tỷ lệ chiết khấu tương ứng với từng nhóm khách hàng (Sinh viên giảm 20%, Thành viên VIP giảm 15%).
- **BR-03 (Chặn trùng ghế):** Ghế đã được đánh dấu `BOOKED` không thể đặt lại (Ném `BookingException`).
- **BR-04 (Phụ phí loại ghế):** Ghế thường phụ phí +0đ, Ghế VIP phụ phí +20.000đ, Ghế Đôi phụ phí +50.000đ cộng dồn vào giá vé cơ bản của suất chiếu.
- **BR-05 (Kiểm tra giá hợp lệ):** Giá vé cuối cùng sau khi tính toán luôn phải lớn hơn 0.
- **BR-06 (Thanh toán an toàn):** Vé đã thanh toán (`paid = true`) không được thanh toán lại lần thứ hai.
- **BR-07 (Duy nhất số điện thoại):** Khách hàng mới khi thêm vào hệ thống không được trùng số điện thoại với khách hàng đã có.

---

## 🚀 Hướng Dẫn Cài Đặt & Chạy Ứng Dụng

### Bước 1: Khởi tạo Cơ Sở Dữ Liệu MySQL

Đảm bảo MySQL service đang chạy trên máy (port mặc định 3306). Mở terminal hoặc MySQL Workbench và chạy lệnh sau để import file `database/schema.sql`:

```bash
# Sử dụng tài khoản MySQL của bạn (VD: user 'admin' hoặc 'root')
mysql -u admin -p cinema_booking_db < database/schema.sql
```

> **Lưu ý cấu hình tài khoản:** Nếu bạn dùng tài khoản khác hoặc mật khẩu khác, hãy mở file `src/repository/DatabaseConnection.java` và chỉnh sửa 2 biến `USER` và `PASSWORD` ở đầu file, hoặc truyền qua biến môi trường `DB_USER` và `DB_PASSWORD`.

### Bước 2: Biên Dịch & Chạy Chương Trình

#### Trên Linux / Ubuntu / macOS:
```bash
# Cấp quyền thực thi cho script (chỉ cần làm 1 lần)
chmod +x run.sh

# Chạy Giao Diện Web (Mặc định)
./run.sh

# Hoặc chạy Giao Diện Console Terminal
./run.sh console
```

#### Trên Windows:
```cmd
# Chạy Giao Diện Web (Mặc định)
build.bat

# Hoặc chạy Giao Diện Console Terminal
build.bat console
```

### Bước 3: Mở Giao Diện Web

Mở trình duyệt web bất kỳ (Chrome, Edge, Firefox) và truy cập:
👉 **`http://localhost:8080`**

---

## 📡 Danh Sách REST API Endpoints (Backend)

| Method | Endpoint | Mô tả |
|---|---|---|
| `GET` | `/api/movies` | Lấy danh sách phim (hỗ trợ lọc `?status=all\|showing` và `?keyword=...`) |
| `GET` | `/api/showtimes` | Lấy danh sách suất chiếu (hỗ trợ lọc theo `?movieId=...`) |
| `GET` | `/api/showtimes/{id}/seats` | Lấy chi tiết suất chiếu kèm toàn bộ 44 ghế phòng chiếu |
| `GET` | `/api/customers` | Lấy danh sách hồ sơ khách hàng |
| `POST` | `/api/customers` | Thêm khách hàng mới (kiểm tra không trùng SĐT) |
| `POST` | `/api/bookings/preview` | Tính giá vé chi tiết trước khi đặt vé |
| `POST` | `/api/bookings` | Đặt vé xem phim (bắt `BookingException` trả về HTTP 400 tiếng Việt) |
| `GET` | `/api/tickets` | Lấy danh sách vé đã đặt kèm 4 chỉ số thống kê doanh thu |
| `POST` | `/api/tickets/pay` | Thanh toán vé qua Tiền mặt / Chuyển khoản / Ví MoMo |
