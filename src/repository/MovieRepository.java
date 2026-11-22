package repository;

import model.Movie;
import model.MovieStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu Phim từ cơ sở dữ liệu MySQL (bảng `movies`).
 * Sử dụng JDBC PreparedStatement theo kiến trúc phân tầng sạch.
 */
public class MovieRepository {

    public MovieRepository() {
        // Khởi tạo repository kết nối MySQL
    }

    /**
     * Lấy danh sách tất cả các bộ phim trong cơ sở dữ liệu.
     *
     * @return danh sách phim
     */
    public List<Movie> findAll() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT id, title, genre, duration, age_limit, status FROM movies ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String genre = rs.getString("genre");
                int duration = rs.getInt("duration");
                int ageLimit = rs.getInt("age_limit");
                MovieStatus status = MovieStatus.valueOf(rs.getString("status"));

                list.add(new Movie(id, title, genre, duration, ageLimit, status));
            }
        } catch (SQLException e) {
            System.err.println("[MovieRepository] Loi truy van findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm phim theo mã ID.
     *
     * @param id mã phim (VD: MV01)
     * @return Movie nếu tìm thấy, ngược lại null
     */
    public Movie findById(String id) {
        String sql = "SELECT id, title, genre, duration, age_limit, status FROM movies WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String genre = rs.getString("genre");
                    int duration = rs.getInt("duration");
                    int ageLimit = rs.getInt("age_limit");
                    MovieStatus status = MovieStatus.valueOf(rs.getString("status"));
                    return new Movie(id, title, genre, duration, ageLimit, status);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieRepository] Loi truy van findById (" + id + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Thêm một bộ phim mới vào cơ sở dữ liệu.
     */
    public void save(Movie m) {
        String sql = "INSERT INTO movies (id, title, genre, duration, age_limit, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getId());
            ps.setString(2, m.getTitle());
            ps.setString(3, m.getGenre());
            ps.setInt(4, m.getDuration());
            ps.setInt(5, m.getAgeLimit());
            ps.setString(6, m.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MovieRepository] Loi luu phim: " + e.getMessage());
        }
    }
}
