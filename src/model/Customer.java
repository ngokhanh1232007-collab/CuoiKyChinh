package model;

/**
 * Khách hàng – kế thừa User (Inheritance).
 * Bổ sung thuộc tính loại khách để áp dụng chính sách giá (Polymorphism).
 */
public class Customer extends User {
    private CustomerType type;  // NORMAL / STUDENT / VIP

    public Customer(String id, String fullName, String phone,
                    String email, CustomerType type) {
        super(id, fullName, phone, email);
        this.type = type;
    }

    public CustomerType getType()            { return type; }
    public void setType(CustomerType type)   { this.type = type; }

    @Override
    public String getRole() {
        return "Khách hàng - " + type.getName();
    }

    @Override
    public String toString() {
        return "[" + getId() + "] " + getFullName()
             + " | " + getPhone() + " | " + type.getName();
    }
}
