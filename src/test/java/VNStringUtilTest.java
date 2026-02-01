import com.example.demo.Utils.VNStringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VNStringUtilTest {

    @Nested
    @DisplayName("Tests cho hàm removeAccents (Loại bỏ dấu) - 100 Case")
    class RemoveAccentsTests {

        @ParameterizedTest(name = "{index} => Dầu vào: {0} -> Mong đợi: {1}")
        @CsvSource({
                // 1. Nhóm chữ cái đơn (Nguyên âm + các loại dấu) - 35 cases
                "á, a", "à, a", "ả, a", "ã, a", "ạ, a",
                "ă, a", "ắ, a", "ằ, a", "ẳ, a", "ẵ, a", "ặ, a",
                "â, a", "ấ, a", "ầ, a", "ẩ, a", "ẫ, a", "ậ, a",
                "é, e", "è, e", "ẻ, e", "ẽ, e", "ẹ, e",
                "ê, e", "ế, e", "ề, e", "ể, e", "ễ, e", "ệ, e",
                "í, i", "ì, i", "ỉ, i", "ĩ, i", "ị, i",
                "ó, o", "ò, o", "ỏ, o", "õ, o", "ọ, o",
                "ô, o", "ố, o", "ồ, o", "ổ, o", "ỗ, o", "ộ, o",
                "ơ, o", "ớ, o", "ờ, o", "ở, o", "ỡ, o", "ợ, o",
                "ú, u", "ù, u", "ủ, u", "ũ, u", "ụ, u",
                "ư, u", "ứ, u", "ừ, u", "ử, u", "ữ, u", "ự, u",
                "ý, y", "ỳ, y", "ỷ, y", "ỹ, y", "ỵ, y",

                // 2. Nhóm chữ cái đặc biệt đ/Đ - 2 cases
                "đ, d", "Đ, D",

                // 3. Nhóm chữ Hoa có dấu - 15 cases
                "Á, A", "À, A", "Ả, A", "Ã, A", "Ạ, A",
                "Ắ, A", "Ằ, A", "Ẳ, A", "Ẵ, A", "Ặ, A",
                "Ế, E", "Ề, E", "Ể, E", "Ễ, E", "Ệ, E",
                "Ố, O", "Ồ, O", "Ổ, O", "Ỗ, O", "Ộ, O",

                // 4. Nhóm từ phức/Câu (Dấu kết hợp) - 20 cases
                "Hồ Chí Minh, Ho Chi Minh",
                "Hà Nội, Ha Noi",
                "Đà Nẵng, Da Nang",
                "Trường Sa, Truong Sa",
                "Hoàng Sa, Hoang Sa",
                "Công Hòa Xã Hội Chủ Nghĩa, Cong Hoa Xa Hoi Chu Nghia",
                "Độc lập Tự do Hạnh phúc, Doc lap Tu do Hanh phuc",
                "Bánh chưng, Banh chung",
                "Rượu vang, Ruou vang",
                "Đường sắt, Duong sat",
                "Mì tôm, Mi tom",
                "Quảng Ngãi, Quang Ngai",
                "Đắk Lắk, Dak Lak",
                "Thừa Thiên Huế, Thua Thien Hue",
                "Điện Biên Phủ, Dien Bien Phu",
                "Khánh Hòa, Khanh Hoa",
                "Vũng Tàu, Vung Tau",
                "Lạng Sơn, Lang Son",
                "Hải Phòng, Hai Phong",
                "Cần Thơ, Can Tho",

                // 5. Nhóm ký tự đặc biệt & Số & Ký hiệu - 15 cases
                "12345, 12345",
                "!@#$%, !@#$%",
                "(**), (**)",
                "100%, 100%",
                "25°C, 25°C",
                "admin@gmail.com, admin@gmail.com",
                "https://google.com, https://google.com",
                "+84 123, +84 123",
                "JSON_CODE_123, JSON_CODE_123",
                "Tab\tSpace, Tab\tSpace",
                "New\nLine, New\nLine",
                "..., ...",
                "---, ---",
                "Emoji 😊, Emoji 😊",
                "Bracket [test], Bracket [test]",

                // 6. Nhóm hỗn hợp phức tạp - 13 cases
                "đại-lý_ABC, dai-ly_ABC",
                "123_Đường_Láng, 123_Duong_Lang",
                "Mã số thuế: 0101, Ma so thue: 0101",
                "Lý Thường Kiệt 123, Ly Thuong Kiet 123",
                "ĐẶNG VĂN A, DANG VAN A",
                "nguyễn thị b, nguyen thi b",
                "   Khoảng   Trắng   ,    Khoang   Trang   ",
                "Cà fê sữa đá, Ca fe sua da",
                "Thứ 2 đến Thứ 7, Thu 2 den Thu 7",
                "Mưa rào nhẹ, Mua rao nhe",
                "Bão số 1, Bao so 1",
                "Đồng tiền xương máu, Dong tien xuong mau",
                "Ăn quả nhớ kẻ trồng cây, An qua nho ke trong cay"
        })
        void testRemoveAccents(String input, String expected) {
            // Trim để xử lý các case có khoảng trắng nếu cần,
            // ở đây giữ nguyên theo input đầu vào
            assertEquals(expected, VNStringUtil.removeAccents(input));
        }

        @Test
        @DisplayName("Kiểm tra với đầu vào null")
        void testRemoveAccentsNull() {
            assertEquals("", VNStringUtil.removeAccents(null));
        }

        @Test
        @DisplayName("Kiểm tra với chuỗi chỉ toàn dấu")
        void testOnlyAccents() {
            // Các dấu đơn lẻ khi không có chữ cái đi kèm (tùy thuộc Normalizer)
            // Thường trả về chính nó hoặc rỗng
            String result = VNStringUtil.removeAccents("´ ` ̉ ̃ ̣");
            assertNotNull(result);
        }

    }


    // ============================================
    // PHẦN 3: TEST findKeywordIndex()
    // ============================================

    @ParameterizedTest
    @CsvSource({
            // Test null/empty keyword (56-57)
            "ABC123, Test, , -1",
            // Test tìm thấy ở các vị trí code (58-60)
            "ABC123, Test Name, abc, 0",
            "HNBC123, Test, bc, 2",
            "ABC123, Test, 123, 4",
            // Test ưu tiên code trước name (61-62)
            "ABC123, Test Name, test, 0",
            "TEST123, Test Name, test, 0",
            // Test không tìm thấy (63)
            "ABC123, Test Name, xyz, -1",
            // Test keyword đặc biệt (76-79)
            "ABC, Ha Noi, ha noi, 0",
            "ABC123, Test, 123, 3",
            "ABC, test, abc, 0",
            "ABC-123, Test, -, 3",
            // Test code và name giống nhau (80)
            "TEST, TEST, test, 0"
    })
    @DisplayName("Test 56-63, 76-80: Tìm keyword trong code và name")
    void testFindKeywordIndex_BasicCases(String code, String name, String keyword, int expected) {
        assertEquals(expected, VNStringUtil.findKeywordIndex(code, name, keyword));
    }

    @ParameterizedTest
    @CsvSource({
            "HN001, Ha Noi Store, hn, 0",
            "SG002, Sai Gon Store, sg, 0",
            "DN003, Da Nang Store, dn, 0",
            "HCM004, Ho Chi Minh, hcm, 0",
            "CT005, Can Tho, ct, 0"
    })
    @DisplayName("Test 66-70: Tìm mã thành phố")
    void testFindKeywordIndex_CityCode(String code, String name, String keyword, int expected) {
        assertEquals(expected, VNStringUtil.findKeywordIndex(code, name, keyword));
    }

    @ParameterizedTest
    @CsvSource({
            "ABC001, nguyễn Van A, nguyen, 0",
            "ABC002, Trần Thi B, tran, 0",
            "ABC003, Le Vần C, van, 3",
            "ABC004, Pham Minh D, minh, 5",
            "ABC005, Hoang Thỉ E, thi, 6"
    })
    @DisplayName("Test 71-75: Tìm tên người")
    void testFindKeywordIndex_PersonName(String code, String name, String keyword, int expected) {
        assertEquals(expected, VNStringUtil.findKeywordIndex(code, name, keyword));
    }

    @Test
    @DisplayName("Test 56, 64-65: Xử lý null")
    void testFindKeywordIndex_NullHandling() {
        assertEquals(-1, VNStringUtil.findKeywordIndex("ABC123", "Test", null));
        assertEquals(0, VNStringUtil.findKeywordIndex(null, "Test Name", "test"));
        assertEquals(0, VNStringUtil.findKeywordIndex("ABC123", null, "abc"));
    }
}