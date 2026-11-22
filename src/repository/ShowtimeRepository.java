package repository;

import model.Movie;
import model.Seat;
import model.SeatStatus;
import model.SeatType;
import model.Showtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu Suất chiếu và Sơ đồ ghế tương ứng từ MySQL (bảng `showtimes` và `seats`).
 */
public class ShowtimeRepository {
    private final MovieRepository movieRepo;

    public ShowtimeRepository(MovieRepository movieRepo) {
        this.movieRepo = movieRepo;
    }

    /**
     * Lấy toàn bộ danh sách suất chiếu kèm danh sách ghế tương ứng.
     *
     * @return danh sách suất chiếu
     */
    public List<Showtime> findAll() {
        List<Showtime> list = new ArrayList<>();
        String sql = "SELECT id, movie_id, room_name, start_time, end_time, base_price FROM showtimes ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String movieId = rs.getString("movie_id");
                String roomName = rs.getString("room_name");
                String startTime = rs.getString("start_time");
                String endTime = rs.getString("end_time");
                double basePrice = rs.getDouble("base_price");

                Movie movie = movieRepo.findById(movieId);
                if (movie != null) {
                    Showtime st = new Showtime(id, movie, roomName, startTime, endTime, basePrice);
                    loadSeats(conn, st);
                    list.add(st);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowtimeRepository] Loi truy van findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm suất chiếu theo ID kèm danh sách ghế.
     *
     * @param id mã suất chiếu (VD: ST01)
     * @return Showtime hoặc null nếu không tìm thấy
     */
    public Showtime findById(String id) {
        String sql = "SELECT id, movie_id, room_name, start_time, end_time, base_price FROM showtimes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String movieId = rs.getString("movie_id");
                    String roomName = rs.getString("room_name");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    double basePrice = rs.getDouble("base_price");

                    Movie movie = movieRepo.findById(movieId);
                    if (movie != null) {
                        Showtime st = new Showtime(id, movie, roomName, startTime, endTime, basePrice);
                        loadSeats(conn, st);
                        return st;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ShowtimeRepository] Loi truy van findById (" + id + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Nạp toàn bộ danh sách ghế của suất chiếu từ bảng `seats`,
     * sắp xếp theo hàng (A-F) và số thứ tự tăng dần.
     */
    private void loadSeats(Connection conn, Showtime st) throws SQLException {
        String sql = "SELECT id, seat_number, type, status FROM seats WHERE showtime_id = ? "
                   + "ORDER BY SUBSTRING(seat_number, 1, 1) ASC, CAST(SUBSTRING(seat_number, 2) AS UNSIGNED) ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, st.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String seatId = rs.getString("id");
                    String seatNumber = rs.getString("seat_number");
                    SeatType type = SeatType.valueOf(rs.getString("type"));
                    SeatStatus status = SeatStatus.valueOf(rs.getString("status"));

                    st.addSeat(new Seat(seatId, seatNumber, type, status));
                }
            }
        }
    }

    /**
     * Cập nhật trạng thái ghế của suất chiếu vào cơ sở dữ liệu MySQL.
     *
     * @param st đối tượng suất chiếu chứa danh sách ghế cần cập nhật
     */
    public void update(Showtime st) {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Seat s : st.getSeats()) {
                ps.setString(1, s.getStatus().name());
                ps.setString(2, s.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            System.err.println("[ShowtimeRepository] Loi cap nhat ghe: " + e.getMessage());
        }
    }
}
