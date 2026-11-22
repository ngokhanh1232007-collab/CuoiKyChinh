package model;

// Enum loại ghế – phụ phí tính bằng đồng
public enum SeatType {
    NORMAL("Thường",   0),
    VIP   ("VIP",  20000),
    COUPLE("Đôi",  50000);

    private final String name;
    private final int surcharge;   // phụ phí thêm vào giá cơ bản

    SeatType(String name, int surcharge) {
        this.name = name;
        this.surcharge = surcharge;
    }

    public String getName() { return name; }
    public int getSurcharge() { return surcharge; }
}
