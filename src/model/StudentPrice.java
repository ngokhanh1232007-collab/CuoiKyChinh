package model;

/** Giá sinh viên: giảm 20%. */
public class StudentPrice implements TicketPricePolicy {
    @Override
    public double calculate(double basePrice, SeatType seatType) {
        double price = basePrice + seatType.getSurcharge();
        return price * 0.80;   // giảm 20%
    }
    @Override
    public String getPolicyName() { return "Sinh vien (giam 20%)"; }
}
