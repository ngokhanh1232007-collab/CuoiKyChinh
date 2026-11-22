package model;

/** Ghế trong phòng chiếu phim. */
public class Seat {
    private String     id;
    private String     number;   // VD: A1, B2
    private SeatType   type;
    private SeatStatus status;

    public Seat(String id, String number, SeatType type, SeatStatus status) {
        this.id     = id;
        this.number = number;
        this.type   = type;
        this.status = status;
    }

    // Getter / Setter
    public String     getId()     { return id; }
    public String     getNumber() { return number; }
    public SeatType   getType()   { return type; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus s) { this.status = s; }

    public boolean isAvailable()  { return status == SeatStatus.AVAILABLE; }

    @Override
    public String toString() {
        String mark = isAvailable() ? "[ ]" : "[X]";
        return mark + number + "(" + type.getName() + ")";
    }
}
