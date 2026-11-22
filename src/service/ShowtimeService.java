package service;

import model.*;
import repository.ShowtimeRepository;
import java.util.ArrayList;
import java.util.List;

/** Nghiệp vụ quản lý suất chiếu và sơ đồ ghế. */
public class ShowtimeService {
    private final ShowtimeRepository repo;

    public ShowtimeService(ShowtimeRepository repo) { this.repo = repo; }

    public List<Showtime> getAll() { return repo.findAll(); }

    public Showtime findById(String id) { return repo.findById(id); }

    /** Danh sách suất chiếu của một phim (đang chiếu). */
    public List<Showtime> getByMovie(String movieId) {
        List<Showtime> result = new ArrayList<>();
        for (Showtime st : repo.findAll())
            if (st.getMovie().getId().equals(movieId)) result.add(st);
        return result;
    }

    /**
     * In sơ đồ ghế dạng lưới.
     * [ ] = trống, [X] = đã đặt. N=Thường, V=VIP, C=Đôi
     */
    public void printSeatMap(Showtime st) {
        System.out.println("\n  === SO DO GHE: " + st.getRoomName() + " ===");
        System.out.println("  [ ] Trong | [X] Da dat | N=Thuong V=VIP C=Doi");
        System.out.println("  " + "-".repeat(50));

        // Nhóm ghế theo hàng (ký tự đầu của số ghế)
        char curRow = 0;
        for (Seat s : st.getSeats()) {
            char row = s.getNumber().charAt(0);
            if (row != curRow) {
                if (curRow != 0) System.out.println();
                System.out.print("  " + row + "  ");
                curRow = row;
            }
            String typeChar = s.getType().name().substring(0, 1); // N/V/C
            String mark = s.isAvailable() ? "[ ]" : "[X]";
            System.out.print(mark + s.getNumber().substring(1) + typeChar + " ");
        }
        System.out.println("\n  " + "-".repeat(50));
        System.out.printf("  Tong: %d ghe | Trong: %d | Da dat: %d%n",
            st.getSeats().size(), st.countAvailable(),
            st.getSeats().size() - st.countAvailable());
    }

    /** Cập nhật ghế và lưu xuống file. */
    public void updateSeat(Showtime st) {
        repo.update(st);
    }
}
