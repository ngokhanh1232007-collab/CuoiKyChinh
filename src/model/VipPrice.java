package model;

/** Giá thành viên VIP: giảm 15%. */
public class VipPrice implements TicketPricePolicy {
    @Override
    public double calculate(double basePrice, SeatType seatType) {
        double price = basePrice + seatType.getSurcharge();
        return price * 0.85;   // giảm 15%
    }
    @Override
    public String getPolicyName() { return "Thanh vien VIP (giam 15%)"; }
}
