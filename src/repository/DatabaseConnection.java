package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối JDBC tới cơ sở dữ liệu MySQL.
 * Các thông số kết nối được tách riêng thành các hằng số ở đầu file.
 */
public class DatabaseConnection {
    // === CẤU HÌNH KẾT NỐI MYSQL ===
    // Có thể chỉnh sửa trực tiếp hoặc truyền qua biến môi trường / System property
    public static final String URL = "jdbc:mysql://localhost:3306/cinema_booking_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    public static final String USER = "admin";
    public static final String PASSWORD = "MatKhau123@";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] Khong tim thay MySQL Driver: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Ngăn chặn khởi tạo đối tượng utility
    }

    /**
     * Mở một kết nối mới tới cơ sở dữ liệu MySQL.
     * Hỗ trợ override linh hoạt bằng biến môi trường DB_URL, DB_USER, DB_PASSWORD nếu cần.
     *
     * @return Connection đối tượng kết nối JDBC
     * @throws SQLException khi kết nối thất bại
     */
    public static Connection getConnection() throws SQLException {
        String envUrl = System.getenv("DB_URL");
        if (envUrl == null) envUrl = System.getProperty("db.url");
        String finalUrl = (envUrl != null && !envUrl.trim().isEmpty()) ? envUrl : URL;

        String envUser = System.getenv("DB_USER");
        if (envUser == null) envUser = System.getProperty("db.user");
        String finalUser = (envUser != null && !envUser.trim().isEmpty()) ? envUser : USER;

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass == null) envPass = System.getProperty("db.password");
        String finalPass = (envPass != null) ? envPass : PASSWORD;

        return DriverManager.getConnection(finalUrl, finalUser, finalPass);
    }
}
