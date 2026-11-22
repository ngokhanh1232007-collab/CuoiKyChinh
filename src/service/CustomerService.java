package service;

import model.*;
import repository.CustomerRepository;
import utils.IdGenerator;
import java.util.List;

/** Nghiệp vụ quản lý khách hàng. */
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) { this.repo = repo; }

    public List<Customer> getAll() { return repo.findAll(); }

    public Customer findById(String id) { return repo.findById(id); }

    /** Thêm khách hàng mới, kiểm tra SĐT không trùng. */
    public Customer add(String name, String phone, String email, CustomerType type)
            throws BookingException {
        for (Customer c : repo.findAll()) {
            if (c.getPhone().equals(phone))
                throw new BookingException("So dien thoai " + phone + " da ton tai!");
        }
        String id = IdGenerator.newCustomerId();
        Customer c = new Customer(id, name, phone, email, type);
        repo.save(c);
        return c;
    }

    /** Chọn chính sách giá theo loại khách (Polymorphism). */
    public TicketPricePolicy getPricePolicy(CustomerType type) {
        switch (type) {
            case STUDENT: return new StudentPrice();
            case VIP:     return new VipPrice();
            default:      return new NormalPrice();
        }
    }
}
