package model;

/**
 * Interface phương thức thanh toán.
 * Polymorphism: Tiền mặt / Chuyển khoản / Ví điện tử
 * đều dùng chung phương thức pay().
 */
public interface PaymentMethod {
    /** Thực hiện thanh toán, trả về tên phương thức nếu thành công. */
    String pay(double amount);

    /** Tên hiển thị */
    String getName();
}
