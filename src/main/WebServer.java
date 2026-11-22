package main;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.*;
import repository.*;
import service.*;
import utils.JsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Backend Web Server thuần Java sử dụng com.sun.net.httpserver.HttpServer.
 * Cung cấp Static Files (web/) và RESTful JSON API kết nối MySQL.
 */
public class WebServer {
    private static final int DEFAULT_PORT = 8080;

    // Các tầng Model - Repository - Service dùng chung với Console
    private final MovieRepository    movieRepo;
    private final CustomerRepository customerRepo;
    private final ShowtimeRepository showtimeRepo;
    private final TicketRepository   ticketRepo;

    private final MovieService    movieSvc;
    private final CustomerService customerSvc;
    private final ShowtimeService showtimeSvc;
    private final BookingService  bookingSvc;

    public WebServer() {
        this.movieRepo    = new MovieRepository();
        this.customerRepo = new CustomerRepository();
        this.showtimeRepo = new ShowtimeRepository(movieRepo);
        this.ticketRepo   = new TicketRepository(customerRepo, showtimeRepo);

        this.movieSvc    = new MovieService(movieRepo);
        this.customerSvc = new CustomerService(customerRepo);
        this.showtimeSvc = new ShowtimeService(showtimeRepo);
        this.bookingSvc  = new BookingService(ticketRepo, showtimeSvc, customerSvc);
    }

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        int port = DEFAULT_PORT;
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try { port = Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
        }

        WebServer serverApp = new WebServer();
        serverApp.start(port);
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(16));

        // Router: Định tuyến API và Static files
        server.createContext("/api/movies", new MoviesHandler());
        server.createContext("/api/showtimes", new ShowtimesHandler());
        server.createContext("/api/customers", new CustomersHandler());
        server.createContext("/api/bookings/preview", new BookingPreviewHandler());
        server.createContext("/api/bookings", new BookingHandler());
        server.createContext("/api/tickets/pay", new PaymentHandler());
        server.createContext("/api/tickets", new TicketsHandler());
        server.createContext("/", new StaticFileHandler());

        server.start();
        System.out.println("========================================================");
        System.out.println("  CINEMA BOOKING SYSTEM - JAVA WEB SERVER");
        System.out.println("  Server dang chay tai: http://localhost:" + port);
        System.out.println("  Ket noi MySQL: cinema_booking_db (Port 3306)");
        System.out.println("  Nhan Ctrl + C de dung server.");
        System.out.println("========================================================");
    }

    // =========================================================================
    // API HANDLERS
    // =========================================================================

    /**
     * GET /api/movies?status=all|showing&keyword=...
     */
    private class MoviesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String status = query.getOrDefault("status", "all");
            String keyword = query.get("keyword");

            List<Movie> list;
            if ("showing".equalsIgnoreCase(status)) {
                list = movieSvc.getShowing();
            } else {
                list = movieSvc.getAll();
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String kw = keyword.toLowerCase().trim();
                List<Movie> filtered = new ArrayList<>();
                for (Movie m : list) {
                    if (m.getTitle().toLowerCase().contains(kw) || m.getGenre().toLowerCase().contains(kw)) {
                        filtered.add(m);
                    }
                }
                list = filtered;
            }

            List<String> jsonItems = new ArrayList<>();
            for (Movie m : list) {
                jsonItems.add(JsonUtil.movieToJson(m));
            }
            sendJson(exchange, 200, JsonUtil.toArray(jsonItems));
        }
    }

    /**
     * GET /api/showtimes?movieId=...
     * GET /api/showtimes/{id}/seats
     */
    private class ShowtimesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String path = exchange.getRequestURI().getPath(); // e.g. /api/showtimes/ST01/seats
            if (path.matches("^/api/showtimes/[^/]+/seats/?$")) {
                String[] parts = path.split("/");
                String showtimeId = parts[3];
                Showtime st = showtimeSvc.findById(showtimeId);
                if (st == null) {
                    sendJson(exchange, 404, JsonUtil.error("Khong tim thay suat chieu: " + showtimeId));
                    return;
                }
                sendJson(exchange, 200, JsonUtil.showtimeWithSeatsToJson(st));
                return;
            }

            // Normal list showtimes
            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());
            String movieId = query.get("movieId");
            List<Showtime> list;
            if (movieId != null && !movieId.trim().isEmpty()) {
                list = showtimeSvc.getByMovie(movieId.trim());
            } else {
                list = showtimeSvc.getAll();
            }

            List<String> jsonItems = new ArrayList<>();
            for (Showtime st : list) {
                jsonItems.add(JsonUtil.showtimeToJson(st));
            }
            sendJson(exchange, 200, JsonUtil.toArray(jsonItems));
        }
    }

    /**
     * GET /api/customers
     * POST /api/customers
     */
    private class CustomersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Customer> list = customerSvc.getAll();
                List<String> items = new ArrayList<>();
                for (Customer c : list) {
                    items.add(JsonUtil.customerToJson(c));
                }
                sendJson(exchange, 200, JsonUtil.toArray(items));
                return;
            }

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readBody(exchange);
                Map<String, String> data = JsonUtil.parseObject(body);
                String name = data.get("name");
                String phone = data.get("phone");
                String email = data.get("email");
                String typeStr = data.get("type");

                if (name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
                    sendJson(exchange, 400, JsonUtil.error("Ho ten va So dien thoai khong duoc de trong!"));
                    return;
                }

                CustomerType type = CustomerType.NORMAL;
                if (typeStr != null) {
                    try {
                        type = CustomerType.valueOf(typeStr.toUpperCase().trim());
                    } catch (IllegalArgumentException ignored) {}
                }

                try {
                    Customer c = customerSvc.add(name.trim(), phone.trim(), email != null ? email.trim() : "", type);
                    sendJson(exchange, 201, "{\"success\":true,\"customer\":" + JsonUtil.customerToJson(c) + "}");
                } catch (BookingException e) {
                    sendJson(exchange, 400, JsonUtil.error(e.getMessage()));
                }
                return;
            }

            sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
        }
    }

    /**
     * POST /api/bookings/preview
     * Payload: { "customerId": "KH001", "showtimeId": "ST01", "seatNumber": "C3" }
     */
    private class BookingPreviewHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = JsonUtil.parseObject(body);
            String customerId = data.get("customerId");
            String showtimeId = data.get("showtimeId");
            String seatNumber = data.get("seatNumber");

            if (customerId == null || showtimeId == null || seatNumber == null) {
                sendJson(exchange, 400, JsonUtil.error("Thieu thong tin xem truoc gia ve!"));
                return;
            }

            Customer customer = customerSvc.findById(customerId);
            if (customer == null) {
                sendJson(exchange, 400, JsonUtil.error("Khong tim thay khach hang: " + customerId));
                return;
            }

            Showtime showtime = showtimeSvc.findById(showtimeId);
            if (showtime == null) {
                sendJson(exchange, 400, JsonUtil.error("Khong tim thay suat chieu: " + showtimeId));
                return;
            }

            Seat seat = showtime.findSeat(seatNumber);
            if (seat == null) {
                sendJson(exchange, 400, JsonUtil.error("Ghe " + seatNumber + " khong ton tai!"));
                return;
            }

            TicketPricePolicy policy = customerSvc.getPricePolicy(customer.getType());
            double basePrice = showtime.getBasePrice();
            int surcharge = seat.getType().getSurcharge();
            double finalPrice = policy.calculate(basePrice, seat.getType());
            String detailText = bookingSvc.priceDetail(customer, showtime, seat);

            String json = String.format(Locale.US,
                "{\"success\":true,\"customerId\":\"%s\",\"customerName\":\"%s\",\"customerType\":\"%s\",\"customerTypeName\":\"%s\",\"movieId\":\"%s\",\"movieTitle\":\"%s\",\"movieShowing\":%b,\"roomName\":\"%s\",\"startTime\":\"%s\",\"seatNumber\":\"%s\",\"seatType\":\"%s\",\"seatTypeName\":\"%s\",\"basePrice\":%.0f,\"surcharge\":%d,\"policyName\":\"%s\",\"finalPrice\":%.0f,\"isAvailable\":%b,\"detailText\":\"%s\"}",
                JsonUtil.escape(customer.getId()),
                JsonUtil.escape(customer.getFullName()),
                customer.getType().name(),
                JsonUtil.escape(customer.getType().getName()),
                JsonUtil.escape(showtime.getMovie().getId()),
                JsonUtil.escape(showtime.getMovie().getTitle()),
                showtime.getMovie().isShowing(),
                JsonUtil.escape(showtime.getRoomName()),
                JsonUtil.escape(showtime.getStartTime()),
                JsonUtil.escape(seat.getNumber()),
                seat.getType().name(),
                JsonUtil.escape(seat.getType().getName()),
                basePrice,
                surcharge,
                JsonUtil.escape(policy.getPolicyName()),
                finalPrice,
                seat.isAvailable(),
                JsonUtil.escape(detailText)
            );
            sendJson(exchange, 200, json);
        }
    }

    /**
     * POST /api/bookings
     * Payload: { "customerId": "KH001", "showtimeId": "ST01", "seatNumber": "C3" }
     */
    private class BookingHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = JsonUtil.parseObject(body);
            String customerId = data.get("customerId");
            String showtimeId = data.get("showtimeId");
            String seatNumber = data.get("seatNumber");

            if (customerId == null || showtimeId == null || seatNumber == null) {
                sendJson(exchange, 400, JsonUtil.error("Thieu thong tin dat ve!"));
                return;
            }

            try {
                Ticket ticket = bookingSvc.book(customerId, showtimeId, seatNumber);
                String res = "{\"success\":true,\"message\":\"Dat ve thanh cong!\",\"ticket\":"
                           + JsonUtil.ticketToJson(ticket) + "}";
                sendJson(exchange, 201, res);
            } catch (BookingException e) {
                sendJson(exchange, 400, JsonUtil.error(e.getMessage()));
            }
        }
    }

    /**
     * GET /api/tickets
     */
    private class TicketsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            List<Ticket> all = bookingSvc.getAllTickets();
            int paidCount = 0;
            int unpaidCount = 0;
            double totalRevenue = 0;

            List<String> items = new ArrayList<>();
            for (Ticket t : all) {
                items.add(JsonUtil.ticketToJson(t));
                if (t.isPaid()) {
                    paidCount++;
                    totalRevenue += t.getFinalPrice();
                } else {
                    unpaidCount++;
                }
            }

            String json = String.format(Locale.US,
                "{\"success\":true,\"total\":%d,\"paidCount\":%d,\"unpaidCount\":%d,\"totalRevenue\":%.0f,\"tickets\":%s}",
                all.size(), paidCount, unpaidCount, totalRevenue, JsonUtil.toArray(items)
            );
            sendJson(exchange, 200, json);
        }
    }

    /**
     * POST /api/tickets/pay
     * Payload: { "ticketId": "TK003", "paymentMethod": "CASH" | "BANK_TRANSFER" | "E_WALLET" }
     */
    private class PaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String body = readBody(exchange);
            Map<String, String> data = JsonUtil.parseObject(body);
            String ticketId = data.get("ticketId");
            String methodStr = data.get("paymentMethod");

            if (ticketId == null || ticketId.trim().isEmpty()) {
                sendJson(exchange, 400, JsonUtil.error("Ma ve khong duoc de trong!"));
                return;
            }

            PaymentMethod method;
            if (methodStr != null && (methodStr.equalsIgnoreCase("BANK_TRANSFER") || methodStr.contains("Chuyen khoan") || methodStr.contains("Vietcombank"))) {
                method = new BankTransferPayment();
            } else if (methodStr != null && (methodStr.equalsIgnoreCase("E_WALLET") || methodStr.contains("MoMo") || methodStr.contains("Vi"))) {
                method = new EWalletPayment();
            } else {
                method = new CashPayment();
            }

            try {
                bookingSvc.pay(ticketId.trim(), method);
                Ticket updated = bookingSvc.findTicket(ticketId.trim());
                String res = "{\"success\":true,\"message\":\"Thanh toan thanh cong bang " + method.getName()
                           + "!\",\"ticket\":" + JsonUtil.ticketToJson(updated) + "}";
                sendJson(exchange, 200, res);
            } catch (BookingException e) {
                sendJson(exchange, 400, JsonUtil.error(e.getMessage()));
            }
        }
    }

    /**
     * Static file handler phục vụ thư mục `web/` (index.html, style.css, app.js,...)
     */
    private class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return;
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }

            String rawPath = exchange.getRequestURI().getPath();
            if (rawPath == null || rawPath.equals("/")) {
                rawPath = "/index.html";
            }

            // Phòng ngừa Path Traversal
            File file = new File("web" + rawPath).getCanonicalFile();
            File webDir = new File("web").getCanonicalFile();
            if (!file.getPath().startsWith(webDir.getPath()) || !file.exists() || file.isDirectory()) {
                sendResponse(exchange, 404, "<h1>404 Not Found</h1>", "text/html; charset=UTF-8");
                return;
            }

            String mimeType = "application/octet-stream";
            String lower = file.getName().toLowerCase();
            if (lower.endsWith(".html")) mimeType = "text/html; charset=UTF-8";
            else if (lower.endsWith(".css")) mimeType = "text/css; charset=UTF-8";
            else if (lower.endsWith(".js")) mimeType = "application/javascript; charset=UTF-8";
            else if (lower.endsWith(".json")) mimeType = "application/json; charset=UTF-8";
            else if (lower.endsWith(".png")) mimeType = "image/png";
            else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mimeType = "image/jpeg";
            else if (lower.endsWith(".svg")) mimeType = "image/svg+xml";
            else if (lower.endsWith(".ico")) mimeType = "image/x-icon";

            byte[] bytes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytes);
            }

            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    // =========================================================================
    // HTTP UTILITY HELPERS
    // =========================================================================

    private boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String text, String contentType) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length > 0) {
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
                params.put(k, v);
            }
        }
        return params;
    }
}
