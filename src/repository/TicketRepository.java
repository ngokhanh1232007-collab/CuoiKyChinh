package repository;

import model.Customer;
import model.Seat;
import model.Showtime;
import model.Ticket;
import utils.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu Vé xem phim từ cơ sở dữ liệu MySQL (bảng `tickets`).
 * Đồng bộ bộ đếm IdGenerator để sinh mã vé tăng dần chuẩn TK001, TK002...
 */
public class TicketRepository {
    private final CustomerRepository customerRepo;
    private final ShowtimeRepository showtimeRepo;

    public TicketRepository(CustomerRepository customerRepo,
                            ShowtimeRepository showtimeRepo) {
        this.customerRepo = customerRepo;
        this.showtimeRepo = showtimeRepo;
        syncMaxId();
    }

    /**
     * Đồng bộ mã vé lớn nhất hiện có trong database vào IdGenerator.
     */
    private void syncMaxId() {
        String sql = "SELECT id FROM tickets";
        int maxNum = 1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.startsWith("TK")) {
                    try {
                        int num = Integer.parseInt(id.substring(2));
                        if (num >= maxNum) maxNum = num + 1;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            System.err.println("[TicketRepository] Loi dong bo IdGenerator: " + e.getMessage());
        }
        IdGenerator.setTicketCounter(maxNum);
    }

    /**
     * Lấy toàn bộ danh sách vé trong hệ thống.
     *
     * @return danh sách vé xem phim
     */
    public List<Ticket> findAll() {
        List<Ticket> list = new ArrayList<>();
        String sql = "SELECT id, customer_id, showtime_id, seat_number, final_price, paid, payment_method "
                   + "FROM tickets ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String customerId = rs.getString("customer_id");
                String showtimeId = rs.getString("showtime_id");
                String seatNumber = rs.getString("seat_number");
                double finalPrice = rs.getDouble("final_price");
                boolean paid = rs.getBoolean("paid");
                String paymentMethod = rs.getString("payment_method");

                Customer c = customerRepo.findById(customerId);
                Showtime st = showtimeRepo.findById(showtimeId);
                if (c == null || st == null) continue;

                Seat seat = st.findSeat(seatNumber);
                if (seat == null) continue;

                Ticket t = new Ticket(id, c, st, seat, finalPrice);
                if (paid) {
                    t.markPaid(paymentMethod);
                }
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("[TicketRepository] Loi truy van findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm vé theo mã vé.
     *
     * @param id mã vé (VD: TK001)
     * @return Ticket hoặc null nếu không tồn tại
     */
    public Ticket findById(String id) {
        String sql = "SELECT id, customer_id, showtime_id, seat_number, final_price, paid, payment_method "
                   + "FROM tickets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String customerId = rs.getString("customer_id");
                    String showtimeId = rs.getString("showtime_id");
                    String seatNumber = rs.getString("seat_number");
                    double finalPrice = rs.getDouble("final_price");
                    boolean paid = rs.getBoolean("paid");
                    String paymentMethod = rs.getString("payment_method");

                    Customer c = customerRepo.findById(customerId);
                    Showtime st = showtimeRepo.findById(showtimeId);
                    if (c == null || st == null) return null;

                    Seat seat = st.findSeat(seatNumber);
                    if (seat == null) return null;

                    Ticket t = new Ticket(id, c, st, seat, finalPrice);
                    if (paid) {
                        t.markPaid(paymentMethod);
                    }
                    return t;
                }
            }
        } catch (SQLException e) {
            System.err.println("[TicketRepository] Loi truy van findById (" + id + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Lưu thông tin vé mới vào bảng `tickets`.
     *
     * @param t vé xem phim mới
     */
    public void save(Ticket t) {
        String sql = "INSERT INTO tickets (id, customer_id, showtime_id, seat_number, final_price, paid, payment_method) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getId());
            ps.setString(2, t.getCustomer().getId());
            ps.setString(3, t.getShowtime().getId());
            ps.setString(4, t.getSeat().getNumber());
            ps.setDouble(5, t.getFinalPrice());
            ps.setBoolean(6, t.isPaid());
            ps.setString(7, t.getPaymentMethod());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TicketRepository] Loi luu ve: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái thanh toán và phương thức thanh toán của vé.
     *
     * @param t vé xem phim đã cập nhật
     */
    public void update(Ticket t) {
        String sql = "UPDATE tickets SET paid = ?, payment_method = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, t.isPaid());
            ps.setString(2, t.getPaymentMethod());
            ps.setString(3, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TicketRepository] Loi cap nhat ve: " + e.getMessage());
        }
    }
}
