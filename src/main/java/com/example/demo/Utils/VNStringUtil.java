package com.example.demo.Utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VNStringUtil {
    private static final Pattern DIACRITICAL_MARKS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /**
     * Loại bỏ dấu tiếng Việt khỏi chuỗi.
     * Ví dụ: "Tiếng Việt" → "Tieng Viet"
     *
     * @param input chuỗi đầu vào có dấu
     * @return chuỗi đã loại bỏ dấu
     */
    public static String removeAccents(String input) {
        // Kiểm tra null hoặc chuỗi rỗng để tránh lỗi
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Bước 1: Chuẩn hóa chuỗi về dạng NFD (Normalization Form Decomposition)
        // NFD tách ký tự gốc và dấu thành 2 phần riêng biệt
        // Ví dụ: "á" → "a" + "´"
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Bước 2: Dùng regex để xóa tất cả các dấu thanh đã được tách ra
        String withoutMarks = DIACRITICAL_MARKS_PATTERN.matcher(normalized).replaceAll("");

        // Bước 3: Xử lý riêng chữ Đ/đ vì Normalizer không tách được ký tự này
        // Phải thay thế thủ công Đ→D và đ→d
        return withoutMarks.replace('đ', 'd').replace('Đ', 'D');
    }

    /**
     * Chuẩn hóa chuỗi: loại bỏ dấu + trim khoảng trắng + chuyển thường.
     * Ví dụ: "  Hà Nội  " → "ha noi"
     *
     * @param input chuỗi cần chuẩn hóa
     * @return chuỗi đã được chuẩn hóa
     */
    public static String normalizeString(String input) {
        return removeAccents(input.trim()).toLowerCase();
    }

    /**
     * Tìm vị trí xuất hiện của từ khóa trong code hoặc name.
     * Ưu tiên tìm trong code trước, nếu không có mới tìm trong name.
     *
     * @param code mã đại lý
     * @param name tên đại lý
     * @param normalizedKeyword từ khóa tìm kiếm đã được chuẩn hóa (không dấu, chữ thường)
     * @return vị trí tìm thấy (index), -1 nếu không tìm thấy
     */
    public static int findKeywordIndex(String code, String name, String normalizedKeyword) {
        // Nếu từ khóa null hoặc rỗng thì không tìm được → trả về -1
        if (normalizedKeyword == null || normalizedKeyword.isEmpty()) {
            return -1;
        }

        String normalizedCode = normalizeString(code);
        int codeIndex = normalizedCode.indexOf(normalizedKeyword);

        if (codeIndex != -1) {
            return codeIndex;
        }

        String normalizedName = normalizeString(name);
        return normalizedName.indexOf(normalizedKeyword);
    }

    public static void main(String[] args) {
        VNStringUtil.findKeywordIndex("POS800028", "SIPTEST27", "sip");
    }
}