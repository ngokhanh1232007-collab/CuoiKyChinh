package service;

import model.*;
import repository.TicketRepository;
import utils.IdGenerator;
import java.util.List;

/**
 * Nghiệp vụ đặt vé và thanh toán.
 * Áp dụng đầy đủ các Business Rules.
 */
public class BookingService {
    private final TicketRepository  ticketRepo;
    private final ShowtimeService   showtimeSvc;
    private final CustomerService   customerSvc;

    public BookingService(TicketRepository ticketRepo,
                          ShowtimeService  showtimeSvc,
                          CustomerService  customerSvc) {
        this.ticketRepo  = ticketRepo;
        this.showtimeSvc = showtimeSvc;
        this.customerSvc = customerSvc;
    }

    /**
     * Đặt vé – kiểm tra toàn bộ Business Rules trước khi lưu.
     *
     * @throws BookingException khi vi phạm bất kỳ rule nào
     */
    public Ticket book(String customerId, String showtimeId, String seatNumber)
            throws BookingException {

        // 1. Khách hàng tồn tại?
        Customer customer = customerSvc.findById(customerId);
        if (customer == null)
            throw new BookingException("Khong tim thay khach hang: " + customerId);

        // 2. Suất chiếu tồn tại?
        Showtime showtime = showtimeSvc.findById(showtimeId);
        if (showtime == null)
            throw new BookingException("Khong tim thay suat chieu: " + showtimeId);

        // 3. Phim đang chiếu? (BR-01)
        if (!showtime.getMovie().isShowing())
            throw new BookingException("Phim \"" + showtime.getMovie().getTitle()
                                     + "\" da NGUNG CHIEU, khong the dat ve!");

        // 4. Ghế tồn tại? (BR-03)
        Seat seat = showtime.findSeat(seatNumber);
        if (seat == null)
            throw new BookingException("Ghe " + seatNumber
                                     + " khong ton tai trong phong chieu!");

        // 5. Ghế còn trống? (BR-03)
        if (!seat.isAvailable())
            throw new BookingException("Ghe " + seatNumber + " da co nguoi dat!");

        // 6. Tính giá (BR-04: phụ phí ghế, BR-02: theo loại khách)
        TicketPricePolicy policy = customerSvc.getPricePolicy(customer.getType());
        double finalPrice = policy.calculate(showtime.getBasePrice(), seat.getType());

        // 7. Giá phải > 0 (BR-05)
        if (finalPrice <= 0)
            throw new BookingException("Gia ve khong hop le: " + finalPrice);

        // 8. Cập nhật trạng thái ghế -> BOOKED và lưu
        seat.setStatus(SeatStatus.BOOKED);
        showtimeSvc.updateSeat(showtime);

        // 9. Tạo và lưu vé
        String ticketId = IdGenerator.newTicketId();
        Ticket ticket = new Ticket(ticketId, customer, showtime, seat, finalPrice);
        ticketRepo.save(ticket);

        return ticket;
    }

    /**
     * Thanh toán vé.
     * @throws BookingException nếu vé không tồn tại hoặc đã thanh toán
     */
    public void pay(String ticketId, PaymentMethod method) throws BookingException {
        Ticket ticket = ticketRepo.findById(ticketId);
        if (ticket == null)
            throw new BookingException("Khong tim thay ve: " + ticketId);
        if (ticket.isPaid())
            throw new BookingException("Ve " + ticketId + " da duoc thanh toan truoc do!");

        // Thực hiện thanh toán (Polymorphism)
        String methodName = method.pay(ticket.getFinalPrice());

        // Cập nhật trạng thái vé -> PAID
        ticket.markPaid(methodName);
        ticketRepo.update(ticket);
    }

    public List<Ticket> getAllTickets()       { return ticketRepo.findAll(); }
    public Ticket findTicket(String id)       { return ticketRepo.findById(id); }

    /** Tóm tắt chi tiết giá trước khi xác nhận. */
    public String priceDetail(Customer c, Showtime st, Seat seat) {
        TicketPricePolicy policy = customerSvc.getPricePolicy(c.getType());
        double base      = st.getBasePrice();
        double surcharge = seat.getType().getSurcharge();
        double total     = policy.calculate(base, seat.getType());
        return String.format(
            "  Gia co ban      : %,.0fd%n" +
            "  Phu phi ghe(%s) : %,.0fd%n" +
            "  Chinh sach      : %s%n" +
            "  => GIA VE CUOI  : %,.0fd",
            base, seat.getType().getName(), (double)surcharge,
            policy.getPolicyName(), total);
    }
}
