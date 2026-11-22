package model;

/**
 * Lớp trừu tượng (abstract) đại diện cho người dùng chung.
 * Áp dụng Encapsulation: thuộc tính private + getter/setter.
 * Áp dụng Abstraction: lớp abstract không thể new trực tiếp.
 */
public abstract class User {
    private String id;
    private String fullName;
    private String phone;
    private String email;

    public User(String id, String fullName, String phone, String email) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    // Getter / Setter
    public String getId()          { return id; }
    public String getFullName()    { return fullName; }
    public String getPhone()       { return phone; }
    public String getEmail()       { return email; }
    public void setId(String id)         { this.id = id; }
    public void setFullName(String n)    { this.fullName = n; }
    public void setPhone(String p)       { this.phone = p; }
    public void setEmail(String e)       { this.email = e; }

    // Mỗi lớp con phải tự mô tả vai trò của mình (Abstraction)
    public abstract String getRole();

    @Override
    public String toString() {
        return "[" + id + "] " + fullName + " | " + phone;
    }
}
