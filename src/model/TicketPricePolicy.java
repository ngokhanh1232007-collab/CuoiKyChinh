package model;

/**
 * Interface chính sách tính giá vé.
 * Áp dụng Abstraction & Polymorphism:
 * mỗi loại khách có cách tính giá riêng.
 */
public interface TicketPricePolicy {
    /** Tính giá cuối = (giá cơ bản + phụ phí ghế) * hệ số giảm */
    double calculate(double basePrice, SeatType seatType);

    /** Tên chính sách để hiển thị */
    String getPolicyName();
}
