package utils;

import model.*;

import java.util.*;

/**
 * Utility chuyển đổi đối tượng sang định dạng JSON và phân tích JSON request thuần Java (không dùng thư viện ngoài).
 */
public class JsonUtil {

    private JsonUtil() {}

    /**
     * Escape chuỗi cho an toàn trong JSON.
     */
    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * Chuyển danh sách chuỗi JSON thành mảng JSON [ ... ].
     */
    public static String toArray(List<String> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(items.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Chuyển Movie thành JSON.
     */
    public static String movieToJson(Movie m) {
        if (m == null) return "null";
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"title\":\"%s\",\"genre\":\"%s\",\"duration\":%d,\"ageLimit\":%d,\"status\":\"%s\",\"statusName\":\"%s\",\"isShowing\":%b}",
            escape(m.getId()),
            escape(m.getTitle()),
            escape(m.getGenre()),
            m.getDuration(),
            m.getAgeLimit(),
            m.getStatus().name(),
            escape(m.getStatus().getName()),
            m.isShowing()
        );
    }

    /**
     * Chuyển Customer thành JSON.
     */
    public static String customerToJson(Customer c) {
        if (c == null) return "null";
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"fullName\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"type\":\"%s\",\"typeName\":\"%s\"}",
            escape(c.getId()),
            escape(c.getFullName()),
            escape(c.getPhone()),
            escape(c.getEmail()),
            c.getType().name(),
            escape(c.getType().getName())
        );
    }

    /**
     * Chuyển Seat thành JSON.
     */
    public static String seatToJson(Seat s) {
        if (s == null) return "null";
        String num = s.getNumber();
        String row = num.length() > 0 ? String.valueOf(num.charAt(0)) : "";
        int col = 0;
        try {
            if (num.length() > 1) col = Integer.parseInt(num.substring(1));
        } catch (NumberFormatException ignored) {}

        return String.format(Locale.US,
            "{\"id\":\"%s\",\"number\":\"%s\",\"row\":\"%s\",\"col\":%d,\"type\":\"%s\",\"typeName\":\"%s\",\"surcharge\":%d,\"status\":\"%s\",\"statusName\":\"%s\",\"isAvailable\":%b}",
            escape(s.getId()),
            escape(num),
            escape(row),
            col,
            s.getType().name(),
            escape(s.getType().getName()),
            s.getType().getSurcharge(),
            s.getStatus().name(),
            escape(s.getStatus().getName()),
            s.isAvailable()
        );
    }

    /**
     * Chuyển Showtime thành JSON (chưa kèm danh sách ghế).
     */
    public static String showtimeToJson(Showtime st) {
        if (st == null) return "null";
        Movie m = st.getMovie();
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"movieId\":\"%s\",\"movieTitle\":\"%s\",\"genre\":\"%s\",\"movieShowing\":%b,\"roomName\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"basePrice\":%.0f,\"availableSeats\":%d,\"totalSeats\":%d}",
            escape(st.getId()),
            escape(m.getId()),
            escape(m.getTitle()),
            escape(m.getGenre()),
            m.isShowing(),
            escape(st.getRoomName()),
            escape(st.getStartTime()),
            escape(st.getEndTime()),
            st.getBasePrice(),
            st.countAvailable(),
            st.getSeats().size()
        );
    }

    /**
     * Chuyển Showtime kèm toàn bộ 44 ghế thành JSON.
     */
    public static String showtimeWithSeatsToJson(Showtime st) {
        if (st == null) return "null";
        List<String> seatJsonList = new ArrayList<>();
        for (Seat s : st.getSeats()) {
            seatJsonList.add(seatToJson(s));
        }
        Movie m = st.getMovie();
        return String.format(Locale.US,
            "{\"id\":\"%s\",\"movieId\":\"%s\",\"movieTitle\":\"%s\",\"genre\":\"%s\",\"movieShowing\":%b,\"roomName\":\"%s\",\"startTime\":\"%s\",\"endTime\":\"%s\",\"basePrice\":%.0f,\"availableSeats\":%d,\"totalSeats\":%d,\"seats\":%s}",
            escape(st.getId()),
            escape(m.getId()),
            escape(m.getTitle()),
            escape(m.getGenre()),
            m.isShowing(),
            escape(st.getRoomName()),
            escape(st.getStartTime()),
            escape(st.getEndTime()),
            st.getBasePrice(),
            st.countAvailable(),
            st.getSeats().size(),
            toArray(seatJsonList)
        );
    }

    /**
     * Chuyển Ticket thành JSON.
     */
    public static String ticketToJson(Ticket t) {
        if (t == null) return "null";
        Customer c = t.getCustomer();
        Showtime st = t.getShowtime();
        Movie m = st.getMovie();
        Seat s = t.getSeat();

        return String.format(Locale.US,
            "{\"id\":\"%s\",\"customerId\":\"%s\",\"customerName\":\"%s\",\"customerType\":\"%s\",\"customerTypeName\":\"%s\",\"movieId\":\"%s\",\"movieTitle\":\"%s\",\"showtimeId\":\"%s\",\"roomName\":\"%s\",\"startTime\":\"%s\",\"seatNumber\":\"%s\",\"seatType\":\"%s\",\"seatTypeName\":\"%s\",\"finalPrice\":%.0f,\"paid\":%b,\"paymentMethod\":%s}",
            escape(t.getId()),
            escape(c.getId()),
            escape(c.getFullName()),
            c.getType().name(),
            escape(c.getType().getName()),
            escape(m.getId()),
            escape(m.getTitle()),
            escape(st.getId()),
            escape(st.getRoomName()),
            escape(st.getStartTime()),
            escape(s.getNumber()),
            s.getType().name(),
            escape(s.getType().getName()),
            t.getFinalPrice(),
            t.isPaid(),
            t.getPaymentMethod() == null ? "null" : "\"" + escape(t.getPaymentMethod()) + "\""
        );
    }

    /**
     * Tạo thông báo lỗi dạng JSON.
     */
    public static String error(String message) {
        return "{\"success\":false,\"error\":\"" + escape(message) + "\"}";
    }

    /**
     * Tạo thông báo thành công dạng JSON.
     */
    public static String success(String message) {
        return "{\"success\":true,\"message\":\"" + escape(message) + "\"}";
    }

    /**
     * Phân tích một JSON Object phẳng thành Map<String, String>.
     * Hỗ trợ các thuộc tính dạng chuỗi, số, boolean.
     */
    public static Map<String, String> parseObject(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (trimmed.isEmpty()) return map;

        boolean inQuote = false;
        StringBuilder currentKey = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        boolean parsingKey = true;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            if (c == '\\' && inQuote && i + 1 < trimmed.length()) {
                char next = trimmed.charAt(i + 1);
                if (parsingKey) currentKey.append(next);
                else currentValue.append(next);
                i++;
                continue;
            }

            if (c == '"') {
                inQuote = !inQuote;
                continue;
            }

            if (!inQuote) {
                if (c == ':' && parsingKey) {
                    parsingKey = false;
                    continue;
                } else if (c == ',') {
                    String k = currentKey.toString().trim();
                    String v = currentValue.toString().trim();
                    if (!k.isEmpty()) {
                        map.put(k, v);
                    }
                    currentKey.setLength(0);
                    currentValue.setLength(0);
                    parsingKey = true;
                    continue;
                }
            }

            if (parsingKey) {
                currentKey.append(c);
            } else {
                currentValue.append(c);
            }
        }

        String k = currentKey.toString().trim();
        String v = currentValue.toString().trim();
        if (!k.isEmpty()) {
            map.put(k, v);
        }

        return map;
    }
}
