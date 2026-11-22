package model;

/** Vé xem phim sau khi đặt thành công. */
public class Ticket {
    private String   id;
    private Customer customer;
    private Showtime showtime;
    private Seat     seat;
    private double   finalPrice;
    private boolean  paid;          // false = chưa thanh toán
    private String   paymentMethod; // null = chưa thanh toán

    public Ticket(String id, Customer customer, Showtime showtime,
                  Seat seat, double finalPrice) {
        this.id            = id;
        this.customer      = customer;
        this.showtime      = showtime;
        this.seat          = seat;
        this.finalPrice    = finalPrice;
        this.paid          = false;
        this.paymentMethod = null;
    }

    // Getter / Setter
    public String   getId()            { return id; }
    public Customer getCustomer()      { return customer; }
    public Showtime getShowtime()      { return showtime; }
    public Seat     getSeat()          { return seat; }
    public double   getFinalPrice()    { return finalPrice; }
    public boolean  isPaid()           { return paid; }
    public String   getPaymentMethod() { return paymentMethod; }

    public void markPaid(String method) {
        this.paid = true;
        this.paymentMethod = method;
    }

    @Override
    public String toString() {
        String status = paid ? "Da thanh toan (" + paymentMethod + ")" : "Chua thanh toan";
        return String.format("[%s] KH: %-15s | %s | Ghe: %s | %,.0fd | %s",
            id, customer.getFullName(), showtime.getMovie().getTitle(),
            seat.getNumber(), finalPrice, status);
    }
}
