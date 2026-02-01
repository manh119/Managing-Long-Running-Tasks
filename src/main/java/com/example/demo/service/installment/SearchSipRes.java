package com.example.demo.service.installment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSipRes {
    private String code;    // Mã đại lý
    private String name;    // Tên đại lý
    private String address; // Địa chỉ đại lý

    /**
     * Vị trí xuất hiện của từ khóa tìm kiếm.
     * - Giá trị >= 0: Vị trí tìm thấy (0 = đầu chuỗi)
     * - Giá trị -1: Không tìm thấy
     *
     * Dùng để sắp xếp: order nhỏ hơn → match sớm hơn → xếp trước
     */
    private int order;
}