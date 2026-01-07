import com.example.demo.Utils.VNStringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

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

    @Nested
    @DisplayName("Tests cho hàm smartContains (Tìm kiếm thông minh)")
    class SmartContainsTests {

        @ParameterizedTest(name = "Source: {0} | Keyword: {1} -> Kết quả: {2}")
        @CsvSource({
            // 1. Accent Insensitive (Không phân biệt dấu)
            "Đại lý 1, dai ly, true",
            "Đại lý 1, ĐÀI LÝ, true",
            "mã địa lý, dia ly, true",
            
            // 2. Case Insensitive (Không phân biệt hoa thường)
            "ABC 123, abc, true",
            "siêu thị mini, MINI, true",
            
            // 3. Chữ Đ và D
            "Đại Phát, dai phat, true",
            "Dòng sông, đong, true",
            
            // 4. Partial Match (Tìm kiếm tương đối)
            "Cửa hàng Đại Phát, Phat, true",
            "Cửa hàng Đại Phát, cua hang, true",
            "ABC 123, 123, true",
            "ABC 123, BC, true",
            
            // 5. Negative Cases (Trường hợp không khớp)
            "Đại lý 1, dai ly 2, false",
            "Cửa hàng, siêu thị, false",
            "ABC, XYZ, false"
        })
        void testSmartContains(String source, String keyword, boolean expected) {
            assertEquals(expected, VNStringUtil.smartContains(source, keyword));
        }

        @Test
        @DisplayName("Kiểm tra đầu vào null hoặc rỗng")
        void testSmartContainsEdgeCases() {
            assertAll("Edge cases",
                () -> assertFalse(VNStringUtil.smartContains(null, "src/test")),
                () -> assertFalse(VNStringUtil.smartContains("src/test", null)),
                () -> assertFalse(VNStringUtil.smartContains(null, null)),
                // Chú ý: keyword rỗng thường trả về true trong Java String.contains
                () -> assertTrue(VNStringUtil.smartContains("Đại lý", ""))
            );
        }
    }

    @Nested
    @DisplayName("Tests cho hàm smartContains (Tìm kiếm thông minh) - 100 Cases")
    class SmartContainsTests1 {

        @ParameterizedTest(name = "{index} => Source: {0} | Key: {1} -> Trả về: {2}")
        @CsvSource({
                // 1. Tìm kiếm không dấu (Accent Insensitive) - 15 cases
                "Đại lý ABC, dai ly, true",
                "Hà Nội Việt Nam, ha noi, true",
                "Sửa chữa máy tính, sua chua, true",
                "Quản trị viên, quan tri, true",
                "Kỹ thuật viên, ky thuat, true",
                "Phần mềm kế toán, phan mem, true",
                "Hệ thống nhúng, he thong, true",
                "Mạng máy tính, mang may tinh, true",
                "Cơ sở dữ liệu, co so du lieu, true",
                "Trí tuệ nhân tạo, tri tue nhan tao, true",
                "Phát triển ứng dụng, phat trien, true",
                "An toàn thông tin, an toan, true",
                "Điện toán đám mây, dien toan, true",
                "Xử lý ảnh, xu ly, true",
                "Thị giác máy tính, thi giac, true",

                // 2. Không phân biệt hoa thường (Case Insensitive) - 15 cases
                "ĐẠI LÝ 1, đại lý, true",
                "dai ly 2, DAI LY, true",
                "Siêu Thị Co.op, SIÊU THỊ, true",
                "Cửa hàng Mini, mini, true",
                "Apple Store, APPLE, true",
                "Samsung Vina, samsung, true",
                "Sony Center, SONY, true",
                "Thế Giới Di Động, thế giới, true",
                "Điện Máy Xanh, ĐIỆN MÁY, true",
                "FPT Shop, fpt shop, true",
                "Viettel Store, VIETTEL, true",
                "Bách Hóa Xanh, bách hóa, true",
                "VinMart, vinmart, true",
                "Lotte Mart, LOTTE, true",
                "Big C Việt Nam, big c, true",

                // 3. Xử lý chữ Đ và D (Đặc thù tiếng Việt) - 10 cases
                "Đồng Nai, dong nai, true",
                "Bình Dương, binh duong, true",
                "Đà Lạt, da lat, true",
                "Hà Đông, ha đong, true",
                "Dòng sông, đong song, true",
                "Đường sắt, duong sat, true",
                "Điện lực, dien luc, true",
                "Duy nhất, đuy nhat, true",
                "Đặc điểm, dac diem, true",
                "Dung lượng, đung luong, true",

                // 4. Tìm kiếm tương đối (Partial Match - Vị trí đầu, giữa, cuối) - 20 cases
                "Đại lý cấp 1, Đại, true",           // Đầu
                "Đại lý cấp 1, cấp 1, true",        // Cuối
                "Đại lý cấp 1, lý cấp, true",       // Giữa
                "Mã số: ABC-123-XYZ, ABC, true",
                "Mã số: ABC-123-XYZ, 123, true",
                "Mã số: ABC-123-XYZ, XYZ, true",
                "Nguyễn Văn A, Văn, true",
                "Nguyễn Văn A, Nguyễn, true",
                "Nguyễn Văn A, A, true",
                "0901234567, 090, true",
                "0901234567, 567, true",
                "0901234567, 1234, true",
                "Hanoi_Vietnam_2024, 2024, true",
                "Hanoi_Vietnam_2024, Vietnam, true",
                "Hanoi_Vietnam_2024, Hanoi, true",
                "Sản phẩm mới 100%, 100%, true",
                "Sản phẩm mới 100%, mới, true",
                "agent_code_001, code, true",
                "agent_code_001, 001, true",
                "agent_code_001, agent, true",

                // 5. Kết hợp phức tạp (Dấu + Hoa thường + Space) - 20 cases
                "tên ĐẠI LÝ 1, dai ly 1, true",
                "mã ĐỊA LÝ 2, dia ly 2, true",
                "CỬA HÀNG đa nang, da nang, true",
                "ĐIỆN thoại Di Động, dien thoai di dong, true",
                "Máy Tính Xách Tay, may tinh xach tay, true",
                "Đồ Gia Dụng, do gia dung, true",
                "Thời Trang Nam, thoi trang nam, true",
                "Mỹ Phẩm Cao Cấp, my pham cao cap, true",
                "Thực Phẩm Sạch, thuc pham sach, true",
                "Nội Thất Văn Phòng, noi that van phong, true",
                "Dụng Cụ Học Tập, dung cu hoc tap, true",
                "Thiết Bị Điện Tử, thiet bi dien tu, true",
                "Phụ Kiện Ô Tô, phu kien o to, true",
                "Dịch Vụ Du Lịch, dich vu du lich, true",
                "Bất Động Sản, bat dong san, true",
                "Tài Chính Ngân Hàng, tai chinh ngan hang, true",
                "Giáo Dục Đào Tạo, giao duc dao tao, true",
                "Y Tế Sức Khỏe, y te suc khoe, true",
                "Năng Lượng Sạch, nang luong sach, true",
                "Môi Trường Đô Thị, moi truong do thi, true",

                // 6. Trường hợp không khớp (Negative cases) - 15 cases
                "Đại lý 1, dai ly 2, false",
                "Hà Nội, Sài Gòn, false",
                "Apple, Samsung, false",
                "12345, 6789, false",
                "Mã số thuế, số chứng minh, false",
                "Cửa hàng, siêu thị, false",
                "Đại lý, tổng kho, false",
                "Admin, user, false",
                "Phần mềm, phần cứng, false",
                "Đã thanh toán, chưa thanh toán, false",
                "Hoàn thành, đang chờ, false",
                "Thành công, thất bại, false",
                "Đúng, Sai, false",
                "abc, def, false",
                "Việt Nam, Lào, false",

                // 7. Ký tự đặc biệt và biên - 5 cases
                "Đại lý @123, @123, true",
                "Đại lý (Quận 1), (quan 1), true",
                "Đại lý [VIP], [vip], true",
                "Đại lý & Cửa hàng, &, true",
                "Đại lý #001, #001, true"
        })
        void testSmartContains(String source, String keyword, boolean expected) {
            assertEquals(expected, VNStringUtil.smartContains(source, keyword));
        }

        @Test
        @DisplayName("Kiểm tra các trường hợp Null/Empty")
        void testSmartContainsEdgeCases() {
            assertAll("Edge cases",
                    () -> assertFalse(VNStringUtil.smartContains(null, "search")),
                    () -> assertFalse(VNStringUtil.smartContains("source", null)),
                    () -> assertFalse(VNStringUtil.smartContains(null, null)),
                    () -> assertTrue(VNStringUtil.smartContains("Bất kỳ", ""), "Keyword rỗng phải trả về true"),
                    () -> assertTrue(VNStringUtil.smartContains("   ", " "), "Space phải khớp với Space")
            );
        }
    }
}