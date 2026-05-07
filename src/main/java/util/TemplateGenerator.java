package util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Tạo file Excel Template chuẩn cho từng chức năng Import.
 * Sử dụng Apache POI để sinh file có style (màu sắc, chú thích).
 *
 * Mỗi template gồm 3 phần:
 *   Row 0 : Tiêu đề (màu xanh đậm, chữ trắng, in đậm)
 *   Row 1 : Header cột  (màu xanh nhạt, chữ đen, in đậm)
 *   Row 2+: Dữ liệu mẫu (italic, màu chữ xám – để phân biệt với dữ liệu thật)
 */
public class TemplateGenerator {

    // ── Màu sắc ──────────────────────────────────────────
    private static final byte[] COLOR_HEADER_BG  = hexToRgb("1A3C5E"); // Xanh đậm
    private static final byte[] COLOR_SUBHDR_BG  = hexToRgb("D0E4F5"); // Xanh nhạt
    private static final byte[] COLOR_REQUIRED   = hexToRgb("C0392B"); // Đỏ = bắt buộc
    private static final byte[] COLOR_SAMPLE_FG  = hexToRgb("7F8C8D"); // Xám = dữ liệu mẫu
    private static final byte[] COLOR_NOTE_BG    = hexToRgb("FFFDE7"); // Vàng nhạt = ghi chú

    // =====================================================
    //  PUBLIC API
    // =====================================================

    /** Tạo template Ngành đào tạo → template_nganh.xlsx */
    public static File generateNganhTemplate(String saveDir) throws IOException {
        String path = saveDir + File.separator + "template_nganh.xlsx";
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            // Sheet 1: Data
            XSSFSheet sheet = wb.createSheet("DANH SACH NGANH");
            buildNganhSheet(wb, sheet);

            // Sheet 2: Hướng dẫn
            buildGuideSheet(wb, wb.createSheet("HUONG DAN"), getNganhGuide());

            autoSizeColumns(sheet, 9);
            saveWorkbook(wb, path);
        }
        return new File(path);
    }

    /** Tạo template Thí sinh → template_thisinh.xlsx */
    public static File generateThiSinhTemplate(String saveDir) throws IOException {
        String path = saveDir + File.separator + "template_thisinh.xlsx";
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFSheet sheet = wb.createSheet("DANH SACH THI SINH");
            buildThiSinhSheet(wb, sheet);

            buildGuideSheet(wb, wb.createSheet("HUONG DAN"), getThiSinhGuide());

            autoSizeColumns(sheet, 10);
            saveWorkbook(wb, path);
        }
        return new File(path);
    }

    // =====================================================
    //  BUILD SHEET: NGÀNH
    // =====================================================
    private static void buildNganhSheet(XSSFWorkbook wb, XSSFSheet sheet) {
        // Styles
        CellStyle titleStyle   = makeTitleStyle(wb);
        CellStyle headerStyle  = makeHeaderStyle(wb);
        CellStyle reqStyle     = makeRequiredHeaderStyle(wb);
        CellStyle sampleStyle  = makeSampleStyle(wb);
        CellStyle noteStyle    = makeNoteStyle(wb);

        // Row 0: Tiêu đề tổng
        Row title = sheet.createRow(0);
        title.setHeightInPoints(28);
        Cell tc = title.createCell(0);
        tc.setCellValue("TEMPLATE DANH SÁCH NGÀNH ĐÀO TẠO  |  Xóa dòng này và dòng mẫu trước khi import");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        // Row 1: Header cột  (* = bắt buộc)
        String[] headers = {
            "maNganh (*)",       // 0 - required
            "tenNganh (*)",      // 1 - required
            "toHopGoc",          // 2
            "chiTieu (*)",       // 3 - required
            "diemSan",           // 4
            "tuyenThang (Y/N)",  // 5
            "dgnl (Y/N)",        // 6
            "thpt (Y/N)",        // 7
            "vsat (Y/N)"         // 8
        };
        boolean[] required = {true, true, false, true, false, false, false, false, false};

        Row hdrRow = sheet.createRow(1);
        hdrRow.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hdrRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(required[i] ? reqStyle : headerStyle);
        }

        // Row 2: Dữ liệu mẫu
        Row sample = sheet.createRow(2);
        sample.setHeightInPoints(18);
        String[] sampleData = {
            "7480101", "Khoa học máy tính", "A00, A01, D07", "120",
            "15.0", "Y", "Y", "Y", "N"
        };
        for (int i = 0; i < sampleData.length; i++) {
            Cell c = sample.createCell(i);
            c.setCellValue(sampleData[i]);
            c.setCellStyle(sampleStyle);
        }

        // Row 3: Thêm mẫu 2
        Row sample2 = sheet.createRow(3);
        sample2.setHeightInPoints(18);
        String[] sampleData2 = {
            "7480201", "Công nghệ thông tin", "A00, A01, D01", "200",
            "16.5", "N", "Y", "Y", "Y"
        };
        for (int i = 0; i < sampleData2.length; i++) {
            Cell c = sample2.createCell(i);
            c.setCellValue(sampleData2[i]);
            c.setCellStyle(sampleStyle);
        }
    }

    // =====================================================
    //  BUILD SHEET: THÍ SINH
    // =====================================================
    private static void buildThiSinhSheet(XSSFWorkbook wb, XSSFSheet sheet) {
        CellStyle titleStyle   = makeTitleStyle(wb);
        CellStyle headerStyle  = makeHeaderStyle(wb);
        CellStyle reqStyle     = makeRequiredHeaderStyle(wb);
        CellStyle sampleStyle  = makeSampleStyle(wb);

        // Row 0: Tiêu đề
        Row title = sheet.createRow(0);
        title.setHeightInPoints(28);
        Cell tc = title.createCell(0);
        tc.setCellValue("TEMPLATE DANH SÁCH THÍ SINH  |  Xóa dòng này và dòng mẫu trước khi import");
        tc.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

        // Row 1: Header
        String[] headers = {
            "cccd (*)", "soBaoDanh", "ho", "ten (*)",
            "ngaySinh (dd/MM/yyyy)", "gioiTinh (Nam/Nữ/Khác)",
            "doiTuong", "khuVuc", "dienThoai", "email"
        };
        boolean[] required = {true, false, false, true, false, false, false, false, false, false};

        Row hdrRow = sheet.createRow(1);
        hdrRow.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hdrRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(required[i] ? reqStyle : headerStyle);
        }

        // Row 2: Mẫu
        Row sample = sheet.createRow(2);
        sample.setHeightInPoints(18);
        String[] sampleData = {
            "001234567890", "TS00001", "Nguyễn Văn", "An",
            "01/01/2007", "Nam", "01", "3", "0901234567", "an.nv@email.com"
        };
        for (int i = 0; i < sampleData.length; i++) {
            Cell c = sample.createCell(i);
            c.setCellValue(sampleData[i]);
            c.setCellStyle(makeSampleStyle(wb));
        }
    }

    // =====================================================
    //  BUILD SHEET: HƯỚNG DẪN
    // =====================================================
    private static void buildGuideSheet(XSSFWorkbook wb, XSSFSheet sheet, String[][] guide) {
        CellStyle titleStyle = makeTitleStyle(wb);
        CellStyle hdrStyle   = makeHeaderStyle(wb);

        // Tiêu đề
        Row r0 = sheet.createRow(0);
        r0.setHeightInPoints(24);
        Cell t = r0.createCell(0);
        t.setCellValue("HƯỚNG DẪN SỬ DỤNG TEMPLATE");
        t.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        // Header bảng hướng dẫn
        Row h = sheet.createRow(1);
        for (int i = 0; i < 3; i++) {
            Cell c = h.createCell(i);
            c.setCellValue(new String[]{"Cột", "Bắt buộc", "Mô tả / Định dạng"}[i]);
            c.setCellStyle(hdrStyle);
        }

        // Nội dung hướng dẫn
        XSSFCellStyle normalStyle = wb.createCellStyle();
        XSSFFont normalFont = wb.createFont();
        normalFont.setFontName("Segoe UI");
        normalFont.setFontHeightInPoints((short) 11);
        normalStyle.setFont(normalFont);
        normalStyle.setWrapText(true);
        normalStyle.setBorderBottom(BorderStyle.THIN);
        normalStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < guide.length; i++) {
            Row row = sheet.createRow(i + 2);
            row.setHeightInPoints(18);
            for (int j = 0; j < guide[i].length; j++) {
                Cell c = row.createCell(j);
                c.setCellValue(guide[i][j]);
                c.setCellStyle(normalStyle);
            }
        }

        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 3000);
        sheet.setColumnWidth(2, 15000);
    }

    // =====================================================
    //  GUIDE DATA
    // =====================================================
    private static String[][] getNganhGuide() {
        return new String[][]{
            {"maNganh",       "Có",   "Mã ngành theo quy định Bộ GD (7 ký tự số). VD: 7480101"},
            {"tenNganh",      "Có",   "Tên ngành đầy đủ. VD: Khoa học máy tính"},
            {"toHopGoc",      "Không","Các tổ hợp xét tuyển, phân cách bằng dấu phẩy. VD: A00, A01, D07"},
            {"chiTieu",       "Có",   "Số nguyên >= 0. VD: 120"},
            {"diemSan",       "Không","Điểm sàn xét tuyển (số thực). VD: 15.5. Để trống nếu chưa có"},
            {"tuyenThang",    "Không","Ngành có phương thức tuyển thẳng? Gõ Y hoặc bỏ trống"},
            {"dgnl",          "Không","Xét điểm ĐGNL? Gõ Y hoặc bỏ trống"},
            {"thpt",          "Không","Xét điểm thi THPT? Gõ Y hoặc bỏ trống"},
            {"vsat",          "Không","Xét điểm V-SAT? Gõ Y hoặc bỏ trống"},
            {"LƯU Ý",         "—",    "Xóa dòng tiêu đề (dòng 1) và các dòng mẫu trước khi import. Chỉ giữ dòng Header cột và dữ liệu thật."}
        };
    }

    private static String[][] getThiSinhGuide() {
        return new String[][]{
            {"cccd",          "Có",   "Số căn cước công dân (12 chữ số). VD: 001234567890"},
            {"soBaoDanh",     "Không","Số báo danh thi THPT. VD: TS00001"},
            {"ho",            "Không","Họ và tên đệm. VD: Nguyễn Văn"},
            {"ten",           "Có",   "Tên (chữ cuối). VD: An"},
            {"ngaySinh",      "Không","Định dạng dd/MM/yyyy. VD: 01/01/2007"},
            {"gioiTinh",      "Không","Nam / Nữ / Khác"},
            {"doiTuong",      "Không","Mã đối tượng ưu tiên. VD: 01, 02, 07"},
            {"khuVuc",        "Không","Khu vực ưu tiên. VD: 1, 2, 3, 2NT"},
            {"dienThoai",     "Không","Số điện thoại. VD: 0901234567"},
            {"email",         "Không","Địa chỉ email. VD: an@email.com"},
            {"LƯU Ý",         "—",    "Xóa dòng tiêu đề và các dòng mẫu trước khi import."}
        };
    }

    // =====================================================
    //  STYLES
    // =====================================================
    private static CellStyle makeTitleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 13);
        f.setColor(new XSSFColor(new byte[]{(byte)255, (byte)255, (byte)255}, null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_HEADER_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private static CellStyle makeHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_SUBHDR_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.MEDIUM);
        s.setBorderRight(BorderStyle.THIN);
        s.setWrapText(false);
        return s;
    }

    private static CellStyle makeRequiredHeaderStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(new XSSFColor(COLOR_REQUIRED, null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(COLOR_SUBHDR_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.MEDIUM);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static CellStyle makeSampleStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setItalic(true);
        f.setFontHeightInPoints((short) 11);
        f.setColor(new XSSFColor(COLOR_SAMPLE_FG, null));
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)249,(byte)249,(byte)249}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static CellStyle makeNoteStyle(XSSFWorkbook wb) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(COLOR_NOTE_BG, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    // =====================================================
    //  UTILS
    // =====================================================
    private static void autoSizeColumns(XSSFSheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
            // Thêm padding nhỏ
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 800);
        }
    }

    private static void saveWorkbook(XSSFWorkbook wb, String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(f)) {
            wb.write(fos);
        }
    }

    private static byte[] hexToRgb(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }
}
