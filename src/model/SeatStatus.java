package model;

// Enum trạng thái ghế
public enum SeatStatus {
    AVAILABLE("Trống"),
    BOOKED   ("Đã đặt");

    private final String name;
    SeatStatus(String name) { this.name = name; }
    public String getName() { return name; }
}
