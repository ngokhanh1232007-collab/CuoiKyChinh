package utils;

import java.util.Scanner;

/** Hàm đọc input an toàn từ console. */
public class Input {
    private Input() {}

    /** Đọc số nguyên trong [min, max], hỏi lại nếu sai. */
    public static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("  Vui long nhap so tu " + min + " den " + max + "!");
        }
    }

    /** Đọc chuỗi không rỗng, hỏi lại nếu rỗng. */
    public static String readText(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (!line.isEmpty()) return line;
            System.out.println("  Khong duoc de trong!");
        }
    }
}
