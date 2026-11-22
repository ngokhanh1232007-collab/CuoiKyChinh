package service;

import model.*;
import repository.MovieRepository;
import java.util.ArrayList;
import java.util.List;

/** Nghiệp vụ quản lý phim. */
public class MovieService {
    private final MovieRepository repo;

    public MovieService(MovieRepository repo) { this.repo = repo; }

    /** Tất cả phim. */
    public List<Movie> getAll() { return repo.findAll(); }

    /** Chỉ phim đang chiếu. */
    public List<Movie> getShowing() {
        List<Movie> result = new ArrayList<>();
        for (Movie m : repo.findAll())
            if (m.isShowing()) result.add(m);
        return result;
    }

    /** Tìm theo tên (chứa keyword, không phân biệt hoa thường). */
    public List<Movie> search(String keyword) {
        List<Movie> result = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Movie m : repo.findAll())
            if (m.getTitle().toLowerCase().contains(kw)) result.add(m);
        return result;
    }

    /** Lấy phim theo ID, trả null nếu không có. */
    public Movie findById(String id) { return repo.findById(id); }
}
