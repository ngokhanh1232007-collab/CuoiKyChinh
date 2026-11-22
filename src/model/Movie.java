package model;

/** Phim trong hệ thống. */
public class Movie {
    private String id;
    private String title;
    private String genre;
    private int    duration;        // phút
    private int    ageLimit;        // 0 = mọi lứa tuổi
    private MovieStatus status;

    public Movie(String id, String title, String genre,
                 int duration, int ageLimit, MovieStatus status) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.ageLimit = ageLimit;
        this.status = status;
    }

    // Getter / Setter
    public String getId()         { return id; }
    public String getTitle()      { return title; }
    public String getGenre()      { return genre; }
    public int    getDuration()   { return duration; }
    public int    getAgeLimit()   { return ageLimit; }
    public MovieStatus getStatus(){ return status; }
    public void setStatus(MovieStatus s) { this.status = s; }

    public boolean isShowing()    { return status == MovieStatus.SHOWING; }

    @Override
    public String toString() {
        String age = ageLimit == 0 ? "Mọi lứa" : ageLimit + "+";
        return String.format("[%s] %-30s | %s | %d phút | %s | %s",
            id, title, genre, duration, age, status.getName());
    }
}
