package model;

/** Thanh toán qua ví điện tử MoMo. */
public class EWalletPayment implements PaymentMethod {
    private static final String WALLET = "MoMo";
    private static final String PHONE  = "0909888777";

    @Override
    public String pay(double amount) {
        System.out.printf("  [Vi dien tu] %s - SDT: %s - So tien: %,.0fd%n",
                          WALLET, PHONE, amount);
        return getName();
    }
    @Override
    public String getName() { return "Vi dien tu " + WALLET; }
}
