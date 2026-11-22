package main;

import model.*;
import repository.*;
import service.*;
import utils.*;

import java.util.List;
import java.util.Scanner;


public class Main {

    // === Dependencies ===
    static Scanner sc;
    static MovieRepository    movieRepo;
    static CustomerRepository customerRepo;
    static ShowtimeRepository showtimeRepo;
    static TicketRepository   ticketRepo;

    static MovieService    movieSvc;
    static CustomerService customerSvc;
    static ShowtimeService showtimeSvc;
    static BookingService  bookingSvc;

    public static void main(String[] args) throws Exception {
        // Đặt encoding UTF-8 cho output
        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        sc = new Scanner(System.in, "UTF-8");

        System.out.println("Dang khoi tao he thong...");

        // Khởi tạo repository (đúng thứ tự phụ thuộc)
        movieRepo    = new MovieRepository();
        customerRepo = new CustomerRepository();
        showtimeRepo = new ShowtimeRepository(movieRepo);
        ticketRepo   = new TicketRepository(customerRepo, showtimeRepo);

        // Khởi tạo service
        movieSvc    = new MovieService(movieRepo);
        customerSvc = new CustomerService(customerRepo);
        showtimeSvc = new ShowtimeService(showtimeRepo);
        bookingSvc  = new BookingService(ticketRepo, showtimeSvc, customerSvc);

        System.out.println("Khoi dong thanh cong!\n");

        // Vòng lặp menu chính
        boolean run = true;
        while (run) {
            printMainMenu();
            int choice = Input.readInt(sc, "Chon chuc nang: ", 0, 6);
            switch (choice) {
                case 1 -> menuMovies();
                case 2 -> menuShowtimes();
                case 3 -> menuCustomers();
                case 4 -> menuBookTicket();
                case 5 -> menuPayment();
                case 6 -> menuViewTickets();
                case 0 -> run = false;
            }
        }

        System.out.println("\nCam on da su dung he thong! Chuc xem phim vui ve! :)");
        sc.close();
    }

    // ================================================================
    //  MENU CHINH
    // ================================================================
    static void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("   HE THONG QUAN LY VE XEM PHIM");
        System.out.println("========================================");
        System.out.println(" [1] Xem / Tim kiem phim");
        System.out.println(" [2] Xem suat chieu & so do ghe");
        System.out.println(" [3] Quan ly khach hang");
        System.out.println(" [4] Dat ve xem phim");
        System.out.println(" [5] Thanh toan ve");
        System.out.println(" [6] Xem danh sach ve da dat");
        System.out.println(" [0] Thoat");
        System.out.println("========================================");
    }

    // ================================================================
    //  MENU 1: PHIM
    // ================================================================
    static void menuMovies() {
        System.out.println("\n--- PHIM ---");
        System.out.println(" [1] Xem phim dang chieu");
        System.out.println(" [2] Xem tat ca phim");
        System.out.println(" [3] Tim kiem phim theo ten");
        System.out.println(" [0] Quay lai");
        int ch = Input.readInt(sc, "Chon: ", 0, 3);
        List<Movie> movies = switch (ch) {
            case 1  -> movieSvc.getShowing();
            case 2  -> movieSvc.getAll();
            case 3  -> {
                String kw = Input.readText(sc, "Nhap ten phim: ");
                yield movieSvc.search(kw);
            }
            default -> null;
        };
        if (movies != null) printMovies(movies);
    }

    static void printMovies(List<Movie> movies) {
        System.out.println();
        if (movies.isEmpty()) { System.out.println("  Khong co phim nao."); return; }
        System.out.printf("  %-5s %-32s %-16s %6s %8s  %s%n",
            "Ma", "Ten phim", "The loai", "TL(p)", "Do tuoi", "Trang thai");
        System.out.println("  " + "-".repeat(80));
        for (Movie m : movies) {
            String age = m.getAgeLimit() == 0 ? "Moi lua" : m.getAgeLimit() + "+";
            System.out.printf("  %-5s %-32s %-16s %6d %8s  %s%n",
                m.getId(), m.getTitle(), m.getGenre(),
                m.getDuration(), age, m.getStatus().getName());
        }
        System.out.println("  Tong: " + movies.size() + " phim.");
    }

    // ================================================================
    //  MENU 2: SUAT CHIEU
    // ================================================================
    static void menuShowtimes() {
        System.out.println("\n--- SUAT CHIEU ---");
        System.out.println(" [1] Xem tat ca suat chieu");
        System.out.println(" [2] Xem suat chieu theo phim");
        System.out.println(" [3] Xem so do ghe");
        System.out.println(" [0] Quay lai");
        int ch = Input.readInt(sc, "Chon: ", 0, 3);

        if (ch == 1) {
            printShowtimes(showtimeSvc.getAll());

        } else if (ch == 2) {
            printMovies(movieSvc.getShowing());
            String movieId = Input.readText(sc, "Nhap ma phim: ");
            printShowtimes(showtimeSvc.getByMovie(movieId));

        } else if (ch == 3) {
            printShowtimes(showtimeSvc.getAll());
            String stId = Input.readText(sc, "Nhap ma suat chieu: ");
            Showtime st = showtimeSvc.findById(stId);
            if (st == null) System.out.println("  Khong tim thay suat chieu!");
            else            showtimeSvc.printSeatMap(st);
        }
    }

    static void printShowtimes(List<Showtime> showtimes) {
        System.out.println();
        if (showtimes.isEmpty()) { System.out.println("  Khong co suat chieu nao."); return; }
        System.out.printf("  %-5s %-26s %-18s %-18s %12s  %s%n",
            "Ma", "Phim", "Phong", "Bat dau", "Gia ve", "Trong/Tong");
        System.out.println("  " + "-".repeat(90));
        for (Showtime st : showtimes) {
            System.out.printf("  %-5s %-26s %-18s %-18s %,.0f d  %d/%d%n",
                st.getId(), st.getMovie().getTitle(), st.getRoomName(),
                st.getStartTime(), st.getBasePrice(),
                st.countAvailable(), st.getSeats().size());
        }
    }

    // ================================================================
    //  MENU 3: KHACH HANG
    // ================================================================
    static void menuCustomers() {
        System.out.println("\n--- KHACH HANG ---");
        System.out.println(" [1] Xem danh sach khach hang");
        System.out.println(" [2] Them khach hang moi");
        System.out.println(" [0] Quay lai");
        int ch = Input.readInt(sc, "Chon: ", 0, 2);

        if (ch == 1) {
            printCustomers(customerSvc.getAll());
        } else if (ch == 2) {
            addCustomer();
        }
    }

    static void printCustomers(List<Customer> customers) {
        System.out.println();
        if (customers.isEmpty()) { System.out.println("  Khong co khach hang nao."); return; }
        System.out.printf("  %-7s %-22s %-13s %-24s %s%n",
            "Ma KH", "Ho ten", "Dien thoai", "Email", "Loai KH");
        System.out.println("  " + "-".repeat(75));
        for (Customer c : customers) {
            System.out.printf("  %-7s %-22s %-13s %-24s %s%n",
                c.getId(), c.getFullName(), c.getPhone(),
                c.getEmail(), c.getType().getName());
        }
        System.out.println("  Tong: " + customers.size() + " khach hang.");
    }

    static void addCustomer() {
        System.out.println("\n  -- Them khach hang moi --");
        String name  = Input.readText(sc, "  Ho ten     : ");
        String phone = Input.readText(sc, "  Dien thoai : ");
        String email = Input.readText(sc, "  Email       : ");
        System.out.println("  Loai KH: [1] Khach thuong  [2] Sinh vien  [3] VIP");
        int t = Input.readInt(sc, "  Chon: ", 1, 3);
        CustomerType type = switch (t) {
            case 2  -> CustomerType.STUDENT;
            case 3  -> CustomerType.VIP;
            default -> CustomerType.NORMAL;
        };
        try {
            Customer c = customerSvc.add(name, phone, email, type);
            System.out.println("  Them thanh cong! " + c);
        } catch (BookingException e) {
            System.out.println("  LOI: " + e.getMessage());
        }
    }

    // ================================================================
    //  MENU 4: DAT VE
    // ================================================================
    static void menuBookTicket() {
        System.out.println("\n--- DAT VE XEM PHIM ---");
        try {
            // Bước 1: Chọn khách hàng
            printCustomers(customerSvc.getAll());
            String custId = Input.readText(sc, "Nhap ma khach hang: ");

            // Bước 2: Chọn phim
            printMovies(movieSvc.getShowing());
            String movieId = Input.readText(sc, "Nhap ma phim: ");

            // Bước 3: Chọn suất chiếu
            printShowtimes(showtimeSvc.getByMovie(movieId));
            String stId = Input.readText(sc, "Nhap ma suat chieu: ");

            // Bước 4: Hiển thị sơ đồ ghế và chọn ghế
            Showtime st = showtimeSvc.findById(stId);
            if (st == null) { System.out.println("Khong tim thay suat chieu!"); return; }
            showtimeSvc.printSeatMap(st);
            System.out.println("  Phu phi: Thuong +0d | VIP +20,000d | Doi +50,000d");
            String seatNum = Input.readText(sc, "Nhap so ghe (VD: A1, B2): ").toUpperCase();

            // Bước 5: Hiển thị chi tiết giá
            Customer c = customerSvc.findById(custId);
            Seat seat   = st.findSeat(seatNum);
            if (c != null && seat != null) {
                System.out.println("\n  --- CHI TIET GIA VE ---");
                System.out.println(bookingSvc.priceDetail(c, st, seat));
            }

            // Bước 6: Xác nhận
            System.out.print("\n  Xac nhan dat ve? (Y/N): ");
            String confirm = sc.nextLine().trim();
            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("  Da huy. Ve khong duoc dat.");
                return;
            }

            // Bước 7: Thực hiện đặt vé
            Ticket ticket = bookingSvc.book(custId, stId, seatNum);
            System.out.println("\n  === DAT VE THANH CONG! ===");
            printTicketDetail(ticket);

        } catch (BookingException e) {
            System.out.println("\n  LOI: " + e.getMessage());
        }
    }

    // ================================================================
    //  MENU 5: THANH TOAN
    // ================================================================
    static void menuPayment() {
        System.out.println("\n--- THANH TOAN VE ---");

        // Hiển thị vé chưa thanh toán
        boolean hasUnpaid = false;
        System.out.println("  Ve chua thanh toan:");
        for (Ticket t : bookingSvc.getAllTickets()) {
            if (!t.isPaid()) {
                System.out.println("  " + t);
                hasUnpaid = true;
            }
        }
        if (!hasUnpaid) { System.out.println("  Khong co ve nao chua thanh toan."); return; }

        String ticketId = Input.readText(sc, "Nhap ma ve: ");
        Ticket ticket = bookingSvc.findTicket(ticketId);
        if (ticket == null) { System.out.println("  Khong tim thay ve!"); return; }

        System.out.println("\n  Thong tin ve:");
        printTicketDetail(ticket);

        System.out.println("\n  Chon phuong thuc thanh toan:");
        System.out.println("  [1] Tien mat");
        System.out.println("  [2] Chuyen khoan ngan hang");
        System.out.println("  [3] Vi dien tu (MoMo)");
        int ch = Input.readInt(sc, "  Chon: ", 1, 3);
        PaymentMethod method = switch (ch) {
            case 2  -> new BankTransferPayment();
            case 3  -> new EWalletPayment();
            default -> new CashPayment();
        };

        try {
            bookingSvc.pay(ticketId, method);
            System.out.println("\n  THANH TOAN THANH CONG!");
            System.out.printf("  Phuong thuc: %s | So tien: %,.0fd%n",
                method.getName(), ticket.getFinalPrice());
        } catch (BookingException e) {
            System.out.println("\n  LOI: " + e.getMessage());
        }
    }

    // ================================================================
    //  MENU 6: XEM VE
    // ================================================================
    static void menuViewTickets() {
        System.out.println("\n--- DANH SACH VE DA DAT ---");
        List<Ticket> tickets = bookingSvc.getAllTickets();
        if (tickets.isEmpty()) { System.out.println("  Chua co ve nao."); return; }

        int paid = 0, unpaid = 0;
        double revenue = 0;
        System.out.printf("  %-7s %-18s %-22s %-5s %12s  %s%n",
            "Ma ve", "Khach hang", "Phim", "Ghe", "Gia ve", "Trang thai TT");
        System.out.println("  " + "-".repeat(85));
        for (Ticket t : tickets) {
            String status = t.isPaid() ? "Da TT (" + t.getPaymentMethod() + ")" : "Chua TT";
            System.out.printf("  %-7s %-18s %-22s %-5s %,.0f d   %s%n",
                t.getId(), t.getCustomer().getFullName(),
                t.getShowtime().getMovie().getTitle(),
                t.getSeat().getNumber(), t.getFinalPrice(), status);
            if (t.isPaid()) { paid++; revenue += t.getFinalPrice(); }
            else unpaid++;
        }
        System.out.println("  " + "-".repeat(85));
        System.out.printf("  Tong: %d ve | Da TT: %d | Chua TT: %d | Doanh thu: %,.0fd%n",
            tickets.size(), paid, unpaid, revenue);
    }

    // ================================================================
    //  HELPER: In chi tiết vé
    // ================================================================
    static void printTicketDetail(Ticket t) {
        System.out.println("  +------------------------------------------+");
        System.out.printf("  | Ma ve       : %-27s|%n", t.getId());
        System.out.printf("  | Khach hang  : %-27s|%n", t.getCustomer().getFullName());
        System.out.printf("  | Loai KH     : %-27s|%n", t.getCustomer().getType().getName());
        System.out.printf("  | Phim        : %-27s|%n", t.getShowtime().getMovie().getTitle());
        System.out.printf("  | Suat chieu  : %-27s|%n", t.getShowtime().getStartTime());
        System.out.printf("  | Phong       : %-27s|%n", t.getShowtime().getRoomName());
        System.out.printf("  | Ghe         : %-3s (%-23s)|%n",
            t.getSeat().getNumber(), t.getSeat().getType().getName());
        System.out.printf("  | Gia ve      : %-27s|%n",
            String.format("%,.0fd", t.getFinalPrice()));
        System.out.printf("  | Trang thai  : %-27s|%n",
            t.isPaid() ? "Da thanh toan" : "Chua thanh toan");
        System.out.println("  +------------------------------------------+");
    }
}
