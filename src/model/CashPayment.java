package model;

/** Thanh toán tiền mặt tại quầy. */
public class CashPayment implements PaymentMethod {
    @Override
    public String pay(double amount) {
        System.out.printf("  [Tien mat] Da nhan %,.0fd tai quay.%n", amount);
        return getName();
    }
    @Override
    public String getName() { return "Tien mat"; }
}
