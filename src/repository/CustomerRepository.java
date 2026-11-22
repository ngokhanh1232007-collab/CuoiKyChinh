package repository;

import model.Customer;
import model.CustomerType;
import utils.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý dữ liệu Khách hàng từ cơ sở dữ liệu MySQL (bảng `customers`).
 * Tự động đồng bộ bộ đếm IdGenerator từ các bản ghi hiện có trong database.
 */
public class CustomerRepository {

    public CustomerRepository() {
        syncMaxId();
    }

    /**
     * Đồng bộ ID khách hàng tiếp theo với cơ sở dữ liệu để tránh trùng khóa chính.
     */
    private void syncMaxId() {
        String sql = "SELECT id FROM customers";
        int maxNum = 1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id != null && id.startsWith("KH")) {
                    try {
                        int num = Integer.parseInt(id.substring(2));
                        if (num >= maxNum) maxNum = num + 1;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerRepository] Loi dong bo IdGenerator: " + e.getMessage());
        }
        IdGenerator.setCustomerCounter(maxNum);
    }

    /**
     * Lấy toàn bộ danh sách khách hàng.
     *
     * @return danh sách khách hàng
     */
    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT id, full_name, phone, email, type FROM customers ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String fullName = rs.getString("full_name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                CustomerType type = CustomerType.valueOf(rs.getString("type"));

                list.add(new Customer(id, fullName, phone, email, type));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerRepository] Loi truy van findAll: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm khách hàng theo ID.
     *
     * @param id mã khách hàng (VD: KH001)
     * @return Customer hoặc null nếu không tồn tại
     */
    public Customer findById(String id) {
        String sql = "SELECT id, full_name, phone, email, type FROM customers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fullName = rs.getString("full_name");
                    String phone = rs.getString("phone");
                    String email = rs.getString("email");
                    CustomerType type = CustomerType.valueOf(rs.getString("type"));
                    return new Customer(id, fullName, phone, email, type);
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerRepository] Loi truy van findById (" + id + "): " + e.getMessage());
        }
        return null;
    }

    /**
     * Lưu khách hàng mới vào cơ sở dữ liệu MySQL.
     *
     * @param c đối tượng Customer cần thêm
     */
    public void save(Customer c) {
        String sql = "INSERT INTO customers (id, full_name, phone, email, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getId());
            ps.setString(2, c.getFullName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getType().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[CustomerRepository] Loi luu khach hang: " + e.getMessage());
        }
    }
}
