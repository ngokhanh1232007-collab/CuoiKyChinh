package model;

/** Giá khách thường: không giảm. */
public class NormalPrice implements TicketPricePolicy {
    @Override
    public double calculate(double basePrice, SeatType seatType) {
        return basePrice + seatType.getSurcharge();
    }
    @Override
    public String getPolicyName() { return "Khach thuong (khong giam)"; }
}
