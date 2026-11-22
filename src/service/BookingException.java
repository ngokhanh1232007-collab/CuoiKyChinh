package service;

/**
 * Exception duy nhất cho toàn bộ nghiệp vụ đặt vé.
 * Mọi lỗi nghiệp vụ đều ném BookingException với thông báo tiếng Việt rõ ràng.
 */
public class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}
