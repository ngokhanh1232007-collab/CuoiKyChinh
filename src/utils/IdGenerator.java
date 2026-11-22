package utils;

/** Sinh mã tự động cho vé và khách hàng. */
public class IdGenerator {
    private static int ticketCounter   = 1;
    private static int customerCounter = 1;

    public static String newTicketId()   {
        return String.format("TK%03d", ticketCounter++);
    }
    public static String newCustomerId() {
        return String.format("KH%03d", customerCounter++);
    }

    /** Gọi khi load dữ liệu từ file để không bị trùng ID. */
    public static void setTicketCounter(int n)   { if (n > ticketCounter)   ticketCounter = n; }
    public static void setCustomerCounter(int n) { if (n > customerCounter) customerCounter = n; }
}
