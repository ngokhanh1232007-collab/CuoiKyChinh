package model;

/** Thanh toán chuyển khoản ngân hàng. */
public class BankTransferPayment implements PaymentMethod {
    private static final String BANK    = "Vietcombank";
    private static final String ACCOUNT = "1234567890";

    @Override
    public String pay(double amount) {
        System.out.printf("  [Chuyen khoan] %s - TK: %s - So tien: %,.0fd%n",
                          BANK, ACCOUNT, amount);
        return getName();
    }
    @Override
    public String getName() { return "Chuyen khoan " + BANK; }
}
