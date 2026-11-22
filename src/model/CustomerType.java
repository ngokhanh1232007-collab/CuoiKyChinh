package model;

// Enum loại khách hàng
public enum CustomerType {
    NORMAL("Khách thường"),
    STUDENT("Sinh viên"),
    VIP("Thành viên VIP");

    private final String name;
    CustomerType(String name) { this.name = name; }
    public String getName() { return name; }
}
