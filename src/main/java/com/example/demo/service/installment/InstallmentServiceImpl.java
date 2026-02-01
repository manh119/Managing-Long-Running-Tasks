package com.example.demo.service.installment;

import com.example.demo.Utils.VNStringUtil;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstallmentServiceImpl {
    /**
     * @param searchKey search key theo mã đại lý (code) hoặc tên đại lý (name) *
     * @param posResList list mã đại lý theo nhân viên *
     * @return những đại lý theo search key
     */
    public static List<SearchSipRes> filterBySearchKey(String searchKey, List<SipPosRes> posResList) {
        if (StringUtils.isBlank(searchKey) || posResList == null || posResList.isEmpty()) {
            return Collections.emptyList();
        }
        String normalizedKeyword = VNStringUtil.normalizeString(searchKey);

        // Chuyển đổi mỗi POS thành SearchResult và tính vị trí match +
        // Sắp xếp theo thứ tự xuất hiện (order nhỏ → xếp trước)
        return posResList.stream()
                .map(pos -> createSearchResult(pos, normalizedKeyword))
                .filter(result -> result.getOrder() != -1)
                .sorted(Comparator.comparingInt(SearchSipRes::getOrder))
                .toList();
    }

    /**
     * Tạo đối tượng SearchSipRes từ SipPosRes và tính toán vị trí match.
     *
     * @param sipPosRes dữ liệu đại lý gốc
     * @param normalizedKeyword từ khóa đã chuẩn hóa
     * @return đối tượng SearchSipRes với thông tin đã được mapping
     */
    private static SearchSipRes createSearchResult(SipPosRes sipPosRes, String normalizedKeyword) {
        // Tìm vị trí xuất hiện của từ khóa trong code hoặc name
        int matchOrder = VNStringUtil.findKeywordIndex(
                sipPosRes.getCode(),
                sipPosRes.getName(),
                normalizedKeyword
        );

        // Tạo đối tượng SearchSipRes với Builder pattern
        return SearchSipRes.builder()
                .code(sipPosRes.getCode())
                .name(sipPosRes.getName())
                // Kiểm tra null cho location trước khi lấy name
                .address(sipPosRes.getLocation() != null ? sipPosRes.getLocation().getName() : "")
                .order(matchOrder) // Lưu vị trí match để sắp xếp sau
                .build();
    }




}





