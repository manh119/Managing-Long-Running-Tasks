import com.example.demo.service.installment.InstallmentServiceImpl;
import com.example.demo.service.installment.Location;
import com.example.demo.service.installment.SearchSipRes;
import com.example.demo.service.installment.SipPosRes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InstallmentServiceImplTest {
    @Nested
    @DisplayName("Tests cho hàm filterBySearchKey(searchKey, posResList)")
    class FilterBySearchKeyTest {
        @ParameterizedTest(
                name = "{index} => searchKey={0} -> expectedFirstCode={1}, expectedOrder={2}, expectedNumberResult={3}"
        )
        @CsvSource({
                "POS80003, POS800030, 0, 4",
                "80003, POS800030, 3, 4",
                "TRONG, TRONGTH111, 0, 2",
                "t24, TESTT24, 4, 2",
                "02, test02, 4, 4",
                "cấn, POS800030, 0, 2"
        })
        void should_filter_by_code_and_sort_by_index(
                String searchKey,
                String expectedFirstCode,
                int expectedOrder,
                int expectedNumberResult
        ) {
            var result = InstallmentServiceImpl.filterBySearchKey(
                    searchKey,
                    buildPosList()
            );

            assertEquals(expectedNumberResult, result.size());
            assertEquals(expectedFirstCode, result.get(0).getCode());
            assertEquals(expectedOrder, result.get(0).getOrder());
        }

    }

    private List<SipPosRes> buildPosList() {
        return List.of(
                sip("TRONGTH111", "TRONGTH111", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("TRONGTH122134", "TRONGTH122134", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS010121", "834", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS010122", "79932323", "Phường Trúc Bạch, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS010123", "qqqqq", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS010124", "aaaaa", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS010125", "erttrte", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS800027", "0369985319", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("test02", "test02", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("POS800028", "SIPTEST27", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("POS800029", "siploi", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("POS800030", "cản testsipcantru", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("POS800031", "testsipkcantru", "Phường 2, Thành phố Tân An, Tỉnh Long An"),
                sip("POS800032", "testsip03", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("POS800033", "testsip04", "Phường 5, Thành phố Tân An, Tỉnh Long An"),
                sip("POS010126", "testabc", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("TESTT24", "TESTT24", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội"),
                sip("POS010127", "QTEST_T24", "Phường Phúc Xá, Quận Ba Đình, Thành phố Hà Nội")
        );
    }

    private SipPosRes sip(
            String code,
            String name,
            String location
    ) {
        return SipPosRes.builder()
                .code(code)
                .name(name)
                .location(
                        Location.builder()
                                .name(location)
                                .build()
                )
                .build();
    }

    // ============================================
    // PHẦN 4: TEST filterBySearchKey() - 20+ test cases
    // ============================================

    @Test
    @DisplayName("Test 81: Filter với null searchKey")
    void testFilterBySearchKey_NullSearchKey() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey(null, posList);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 82: Filter với empty searchKey")
    void testFilterBySearchKey_EmptySearchKey() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("", posList);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 83: Filter với null posList")
    void testFilterBySearchKey_NullPosList() {
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("test", null);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 84: Filter với empty posList")
    void testFilterBySearchKey_EmptyPosList() {
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("test", Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 85: Tìm theo code - tìm thấy 1 kết quả")
    void testFilterBySearchKey_FindByCode() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("HN001", posList);

        assertEquals(1, result.size());
        assertEquals("HN001", result.get(0).getCode());
    }

    @Test
    @DisplayName("Test 86: Tìm theo name - tìm thấy nhiều kết quả")
    void testFilterBySearchKey_FindByName() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("Ha Noi", posList);

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getName().toLowerCase().contains("ha noi"));
    }

    @Test
    @DisplayName("Test 87: Sắp xếp theo order đúng")
    void testFilterBySearchKey_CorrectOrdering() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("ABC123", "Test ABC", "Address 1"),
                createPos("DEF456", "ABC Test", "Address 2"),
                createPos("GHI789", "Testing", "Address 3")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("ABC", posList);

        assertEquals(2, result.size());
        assertEquals("ABC123", result.get(0).getCode()); // ABC ở vị trí 0
        assertEquals("DEF456", result.get(1).getCode()); // ABC ở vị trí 0 của name
    }

    @Test
    @DisplayName("Test 88: Không tìm thấy kết quả")
    void testFilterBySearchKey_NoResults() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("NOTFOUND", posList);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 89: Tìm với tiếng Việt có dấu")
    void testFilterBySearchKey_VietnameseWithAccents() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("HN001", "Hà Nội", "123 Hà Nội"),
                createPos("SG001", "Sài Gòn", "456 Sài Gòn")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("Hà Nội", posList);

        assertEquals(1, result.size());
        assertEquals("HN001", result.get(0).getCode());
    }

    @Test
    @DisplayName("Test 90: Tìm với tiếng Việt không dấu")
    void testFilterBySearchKey_VietnameseWithoutAccents() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("HN001", "Hà Nội", "123 Hà Nội"),
                createPos("SG001", "Sài Gòn", "456 Sài Gòn")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("Ha Noi", posList);

        assertEquals(1, result.size());
        assertEquals("HN001", result.get(0).getCode());
    }

    @Test
    @DisplayName("Test 91: Case insensitive search")
    void testFilterBySearchKey_CaseInsensitive() {
        List<SipPosRes> posList = createSamplePosList();

        List<SearchSipRes> result1 = InstallmentServiceImpl.filterBySearchKey("hn", posList);
        List<SearchSipRes> result2 = InstallmentServiceImpl.filterBySearchKey("HN", posList);
        List<SearchSipRes> result3 = InstallmentServiceImpl.filterBySearchKey("Hn", posList);

        assertEquals(result1.size(), result2.size());
        assertEquals(result2.size(), result3.size());
    }

    @Test
    @DisplayName("Test 92: Tìm với khoảng trắng thừa")
    void testFilterBySearchKey_ExtraSpaces() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("  HN001  ", posList);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test 93: Order của kết quả đầu tiên là nhỏ nhất")
    void testFilterBySearchKey_FirstResultHasSmallestOrder() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("HN", posList);

        if (result.size() > 1) {
            for (int i = 1; i < result.size(); i++) {
                assertTrue(result.get(0).getOrder() <= result.get(i).getOrder());
            }
        }
    }

    @Test
    @DisplayName("Test 94: Address được map đúng")
    void testFilterBySearchKey_AddressMapped() {
        List<SipPosRes> posList = createSamplePosList();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("HN001", posList);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getAddress());
    }

    @Test
    @DisplayName("Test 95: Location null không gây lỗi")
    void testFilterBySearchKey_NullLocation() {
        List<SipPosRes> posList = Arrays.asList(
                SipPosRes.builder()
                        .code("TEST001")
                        .name("Test")
                        .location(null)
                        .build()
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("TEST", posList);

        assertEquals(1, result.size());
        assertEquals("", result.get(0).getAddress());
    }

    @Test
    @DisplayName("Test 96: Tìm partial match")
    void testFilterBySearchKey_PartialMatch() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("HANOI001", "Ha Noi Store", "Address 1")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("HA", posList);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test 97: Nhiều kết quả sắp xếp đúng thứ tự")
    void testFilterBySearchKey_MultipleResultsOrdered() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("TEST001", "Another Store", "Address 1"),
                createPos("ABC002", "TEST Store", "Address 2"),
                createPos("XYZ003", "Store TEST", "Address 3")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("TEST", posList);

        assertEquals(3, result.size());
        assertEquals("TEST001", result.get(0).getCode()); // Match ở code position 0
        assertEquals("ABC002", result.get(1).getCode());  // Match ở name position 0
        assertEquals("XYZ003", result.get(2).getCode());  // Match ở name position 6
    }

    @Test
    @DisplayName("Test 98: Tìm với ký tự đặc biệt")
    void testFilterBySearchKey_SpecialCharacters() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("ABC-123", "Test Store", "Address 1")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("ABC-", posList);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test 99: Tìm với số")
    void testFilterBySearchKey_Numbers() {
        List<SipPosRes> posList = Arrays.asList(
                createPos("HN001", "Store 001", "Address 1"),
                createPos("HN002", "Store 002", "Address 2")
        );

        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("001", posList);

        assertEquals(1, result.size());
        assertEquals("HN001", result.get(0).getCode());
    }

    @Test
    @DisplayName("Test 100: Large dataset performance")
    void testFilterBySearchKey_LargeDataset() {
        List<SipPosRes> posList = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            posList.add(createPos("CODE" + i, "Store " + i, "Address " + i));
        }
        posList.add(createPos("SPECIAL", "Special Store", "Special Address"));

        long startTime = System.currentTimeMillis();
        List<SearchSipRes> result = InstallmentServiceImpl.filterBySearchKey("SPECIAL", posList);
        long endTime = System.currentTimeMillis();

        assertEquals(1, result.size());
        assertTrue((endTime - startTime) < 500, "Nên xử lý trong vòng 0.5 giây");
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private List<SipPosRes> createSamplePosList() {
        return Arrays.asList(
                createPos("HN001", "Ha Noi Store", "123 Hai Ba Trung, Ha Noi"),
                createPos("SG001", "Sai Gon Store", "456 Nguyen Hue, Sai Gon"),
                createPos("DN001", "Da Nang Store", "789 Bach Dang, Da Nang"),
                createPos("HCM001", "Ho Chi Minh Store", "321 Le Loi, HCM"),
                createPos("CT001", "Can Tho Store", "654 Tran Hung Dao, Can Tho")
        );
    }

    private SipPosRes createPos(String code, String name, String address) {
        return SipPosRes.builder()
                .code(code)
                .name(name)
                .location(Location.builder()
                        .name(address)
                        .build())
                .build();
    }
}
