package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Suất chiếu: gắn phim + phòng + thời gian + danh sách ghế.
 * Dùng String cho thời gian cho đơn giản (VD: "19:00 20/11/2026").
 */
public class Showtime {
    private String id;
    private Movie  movie;
    private String roomName;
    private String startTime;    // VD: "19:00 20/11/2026"
    private String endTime;
    private double basePrice;
    private List<Seat> seats;

    public Showtime(String id, Movie movie, String roomName,
                    String startTime, String endTime, double basePrice) {
        this.id        = id;
        this.movie     = movie;
        this.roomName  = roomName;
        this.startTime = startTime;
        this.endTime   = endTime;
        this.basePrice = basePrice;
        this.seats     = new ArrayList<>();
    }

    // Getter / Setter
    public String      getId()        { return id; }
    public Movie       getMovie()     { return movie; }
    public String      getRoomName()  { return roomName; }
    public String      getStartTime() { return startTime; }
    public String      getEndTime()   { return endTime; }
    public double      getBasePrice() { return basePrice; }
    public List<Seat>  getSeats()     { return seats; }
    public void        setSeats(List<Seat> s) { this.seats = s; }

    /** Thêm ghế vào suất chiếu. */
    public void addSeat(Seat s) { seats.add(s); }

    /** Tìm ghế theo số ghế, trả null nếu không có. */
    public Seat findSeat(String number) {
        for (Seat s : seats) {
            if (s.getNumber().equalsIgnoreCase(number)) return s;
        }
        return null;
    }

    /** Đếm số ghế còn trống. */
    public int countAvailable() {
        int count = 0;
        for (Seat s : seats) if (s.isAvailable()) count++;
        return count;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s -> %s | Giá: %,.0fd | Trống: %d/%d",
            id, movie.getTitle(), roomName,
            startTime, endTime, basePrice,
            countAvailable(), seats.size());
    }
}
