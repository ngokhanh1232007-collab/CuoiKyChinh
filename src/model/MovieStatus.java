package model;

// Enum trạng thái phim
public enum MovieStatus {
    SHOWING("Đang chiếu"),
    STOPPED("Ngừng chiếu");

    private final String name;
    MovieStatus(String name) { this.name = name; }
    public String getName() { return name; }
}
