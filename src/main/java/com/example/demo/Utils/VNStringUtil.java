package com.example.demo.Utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class VNStringUtil {

    // Hàm chuyển tiếng Việt có dấu thành không dấu
    public static String removeAccents(String s) {
        if (s == null) return "";
        
        // 1. Chuẩn hóa chuỗi về dạng NFD (Tách ký tự gốc và dấu ra)
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        
        // 2. Dùng Regex để loại bỏ các dấu
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        
        // 3. Xử lý riêng chữ Đ/đ (Normalizer không xử lý được chữ này)
        return temp.replace('đ', 'd').replace('Đ', 'D');
    }

    // Hàm kiểm tra chứa (Contains) thông minh: Không phân biệt dấu, không phân biệt hoa thường
    public static boolean smartContains(String source, String keyword) {
        if (source == null || keyword == null) return false;

        // B1: Bỏ dấu cả 2
        String sourceNoAccent = removeAccents(source);
        String keywordNoAccent = removeAccents(keyword);

        // B2: Chuyển về chữ thường và kiểm tra contains
        return sourceNoAccent.toLowerCase().contains(keywordNoAccent.toLowerCase());
    }
}