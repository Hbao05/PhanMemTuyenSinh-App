package util;

import entity.*;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExcelUtil {

    /** Số dòng dữ liệu tối đa đọc mỗi lần khi import thí sinh (bỏ qua dòng tiêu đề). */
    public static final int CANDIDATE_IMPORT_BATCH_SIZE = 1000;

    public static void forEachCandidateExcelBatch(File file, int batchSize, Consumer<List<ThiSinh>> onBatch) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize phải >= 1");
        }
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            DataFormatter dataFormatter = new DataFormatter();

            for (int start = 1; start <= lastRowNum; start += batchSize) {
                int end = Math.min(start + batchSize - 1, lastRowNum);
                List<ThiSinh> batch = readCandidateRows(sheet, dataFormatter, start, end);
                onBatch.accept(batch);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel thí sinh: " + e.getMessage(), e);
        }
    }

    private static List<ThiSinh> readCandidateRows(Sheet sheet, DataFormatter dataFormatter,
                                                   int fromRowInclusive, int toRowInclusive) {
        List<ThiSinh> list = new ArrayList<>();
        for (int i = fromRowInclusive; i <= toRowInclusive; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            ThiSinh ts = rowToThiSinh(row, dataFormatter);
            if (ts != null) {
                list.add(ts);
            }
        }
        return list;
    }

    /**
     * Một dòng Excel → ThiSinh, hoặc {@code null} nếu bỏ qua (không có CCCD).
     * Thứ tự cột: cccd, sbd, họ, tên, ngày sinh, điện thoại, giới tính, email, nơi sinh, đối tượng, khu vực.
     */
    private static ThiSinh rowToThiSinh(Row row, DataFormatter dataFormatter) {
        String cccd = dataFormatter.formatCellValue(row.getCell(0)).trim();
        if (cccd.isEmpty()) {
            return null;
        }
        String soBaoDanh = dataFormatter.formatCellValue(row.getCell(1)).trim();
        String ho = dataFormatter.formatCellValue(row.getCell(2)).trim();
        String ten = dataFormatter.formatCellValue(row.getCell(3)).trim();
        String dob = dataFormatter.formatCellValue(row.getCell(4)).trim();
        String phone = dataFormatter.formatCellValue(row.getCell(5)).trim();
        String gender = dataFormatter.formatCellValue(row.getCell(6)).trim();
        String email = dataFormatter.formatCellValue(row.getCell(7)).trim();
        String birthPlace = dataFormatter.formatCellValue(row.getCell(8)).trim();
        String priorityObj = dataFormatter.formatCellValue(row.getCell(9)).trim();
        String priorityZone = dataFormatter.formatCellValue(row.getCell(10)).trim();

        ThiSinh ts = new ThiSinh();
        ts.setCccd(cccd);
        ts.setSoBaoDanh(soBaoDanh);
        ts.setHo(ho);
        ts.setTen(ten);
        ts.setNgaySinh(dob);
        ts.setDienThoai(phone);
        ts.setGioiTinh(gender);
        ts.setEmail(email);
        ts.setDoiTuong(priorityObj);
        ts.setKhuVuc(priorityZone);
        ts.setNoiSinh(birthPlace);
        return ts;
    }

    /**
     * Hàm đọc file Excel Thí sinh và chỉ lấy các cột thông tin lý lịch cần thiết.
     * @param file File Excel người dùng chọn từ giao diện
     * @return Danh sách các đối tượng ThiSinh
     */
    public static List<ThiSinh> readCandidateExcel(File file) {
        List<ThiSinh> candidateList = new ArrayList<>();
        DataFormatter dataFormatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum >= 1) {
                candidateList = readCandidateRows(sheet, dataFormatter, 1, lastRowNum);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file Excel: " + e.getMessage());
            e.printStackTrace();
        }

        return candidateList;
    }

    public static List<Nganh> readNganhExcel(File file) {
        List<Nganh> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            // Xác định dòng bắt đầu dữ liệu và định dạng
            Row firstRow = sheet.getRow(0);
            boolean hasTitleRow = false;
            boolean isNewFormat = false;

            if (firstRow != null && firstRow.getCell(0) != null) {
                String firstColStr = fmt.formatCellValue(firstRow.getCell(0)).trim();
                if (firstColStr.equalsIgnoreCase("Mã Ngành")) {
                    isNewFormat = true;
                } else if (firstColStr.isEmpty() || !isNumeric(firstColStr)) {
                    hasTitleRow = true;
                }
            } else {
                hasTitleRow = true;
            }

            if (isNewFormat) {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String ma = fmt.formatCellValue(row.getCell(0)).trim();
                    if (ma.isEmpty()) continue;

                    String ten = fmt.formatCellValue(row.getCell(1)).trim();
                    String toHop = fmt.formatCellValue(row.getCell(2)).trim();
                    if (toHop.contains("(")) {
                        toHop = toHop.substring(0, toHop.indexOf("(")).trim();
                    }
                    
                    int chiTieu = parseIntSafe(fmt.formatCellValue(row.getCell(4)).trim());
                    Double diemSan = parseDoubleSafe(fmt.formatCellValue(row.getCell(5)).trim());
                    Double diemTrungTuyen = parseDoubleSafe(fmt.formatCellValue(row.getCell(6)).trim());
                    
                    String xtt = parseFlag(fmt.formatCellValue(row.getCell(7)).trim());
                    String dgnl = parseFlag(fmt.formatCellValue(row.getCell(8)).trim());
                    String thpt = parseFlag(fmt.formatCellValue(row.getCell(9)).trim());
                    String vsat = parseFlag(fmt.formatCellValue(row.getCell(10)).trim());
                    
                    Integer slXtt = parseIntegerSafe(fmt.formatCellValue(row.getCell(11)).trim());
                    Integer slDgnl = parseIntegerSafe(fmt.formatCellValue(row.getCell(12)).trim());
                    Integer slVsat = parseIntegerSafe(fmt.formatCellValue(row.getCell(13)).trim());
                    Integer slThpt = parseIntegerSafe(fmt.formatCellValue(row.getCell(14)).trim());

                    Nganh n = new Nganh();
                    n.setMaNganh(ma);
                    n.setTenNganh(ten);
                    n.setToHopGoc(toHop.isEmpty() ? null : toHop);
                    n.setChiTieu(chiTieu);
                    n.setDiemSan(diemSan);
                    n.setDiemTrungTuyen(diemTrungTuyen);
                    n.setTuyenThang(xtt);
                    n.setDgnl(dgnl);
                    n.setThpt(thpt);
                    n.setVsat(vsat);
                    n.setSlXtt(slXtt);
                    n.setSlDgnl(slDgnl);
                    n.setSlVsat(slVsat);
                    n.setSlThpt(slThpt);
                    list.add(n);
                }
            } else {
                // Kiểm tra xem có phải file ngành cũ không
                if (firstRow != null && firstRow.getCell(1) != null) {
                    String col1 = fmt.formatCellValue(firstRow.getCell(1)).trim();
                    if (!col1.equalsIgnoreCase("Mã ngành") && !col1.equalsIgnoreCase("Ma nganh") && !col1.isEmpty()) {
                        if (!isNumeric(col1)) {
                            // Cột 1 không phải Mã ngành mà là chữ -> Khả năng cao sai file
                            throw new IllegalArgumentException("File sai định dạng! Vui lòng chọn đúng file danh sách Ngành.");
                        }
                    }
                }
                
                // Old format fallback
                int startRow = hasTitleRow ? 2 : 1; 
                for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String ma  = fmt.formatCellValue(row.getCell(1)).trim();
                    String ten = fmt.formatCellValue(row.getCell(2)).trim();
                    if (ma.isEmpty() || ten.isEmpty()) continue;

                    String chiTieuStr = fmt.formatCellValue(row.getCell(3)).trim();
                    int chiTieu = parseIntSafe(chiTieuStr);

                    Nganh n = new Nganh();
                    n.setMaNganh(ma);
                    n.setTenNganh(ten);
                    n.setChiTieu(chiTieu);
                    list.add(n);
                }
            }

            System.out.println("readNganhExcel: đọc được " + list.size() + " ngành.");
        } catch (IllegalArgumentException e) {
            throw e; // Ném tiếp lỗi định dạng
        } catch (Exception e) {
            System.err.println("Lỗi đọc file Ngành: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
        return list;
    }

    public static List<ToHopMonThi> readToHopExcel(File file) {
        List<ToHopMonThi> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            Row firstRow = sheet.getRow(0);
            if (firstRow == null || firstRow.getCell(0) == null) {
                throw new IllegalArgumentException("File trống hoặc không có tiêu đề.");
            }
            String col0 = fmt.formatCellValue(firstRow.getCell(0)).trim();
            
            // Hỗ trợ đọc trực tiếp từ file docs/tohopmon.xlsx gốc
            if (col0.equalsIgnoreCase("STT") && fmt.formatCellValue(firstRow.getCell(1)).trim().equalsIgnoreCase("MANGANH")) {
                return processLegacyToHopExcel(sheet, fmt);
            }
            
            if (!col0.equalsIgnoreCase("Mã tổ hợp") && !col0.equalsIgnoreCase("Ma to hop")) {
                throw new IllegalArgumentException("File sai định dạng! Cột đầu tiên phải là 'Mã tổ hợp'. Vui lòng chọn đúng file Tổ hợp môn.");
            }

            // Bỏ qua dòng tiêu đề
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Các cột: 0: Mã tổ hợp, 1: Môn 1, 2: Môn 2, 3: Môn 3, 4: Tên tổ hợp
                String maToHop = fmt.formatCellValue(row.getCell(0)).trim();
                if (maToHop.isEmpty()) continue;

                String mon1 = fmt.formatCellValue(row.getCell(1)).trim();
                String mon2 = fmt.formatCellValue(row.getCell(2)).trim();
                String mon3 = fmt.formatCellValue(row.getCell(3)).trim();
                String tenToHop = fmt.formatCellValue(row.getCell(4)).trim();

                ToHopMonThi t = new ToHopMonThi();
                t.setMaToHop(maToHop);
                t.setMon1(mon1);
                t.setMon2(mon2);
                t.setMon3(mon3);
                t.setTenToHop(tenToHop.isEmpty() ? null : tenToHop);

                list.add(t);
            }

            System.out.println("readToHopExcel: đọc được " + list.size() + " tổ hợp.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi đọc file Tổ hợp môn: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
        return list;
    }

    public static List<ToHopMonThi> processLegacyToHopExcel(Sheet sheet, DataFormatter fmt) {
        java.util.Map<String, ToHopMonThi> map = new java.util.HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            
            // Trong file gốc, MA_TO_HOP nằm ở cột 3 (index 3)
            String maToHopRaw = fmt.formatCellValue(row.getCell(3)).trim();
            if (maToHopRaw.isEmpty()) continue;
            
            int openIdx = maToHopRaw.indexOf("(");
            int closeIdx = maToHopRaw.indexOf(")");
            if (openIdx == -1 || closeIdx == -1) continue;
            
            String ma = maToHopRaw.substring(0, openIdx).trim();
            if (map.containsKey(ma)) continue; // Lọc trùng lặp
            
            String inside = maToHopRaw.substring(openIdx + 1, closeIdx);
            String[] parts = inside.split(",");
            if (parts.length >= 3) {
                ToHopMonThi t = new ToHopMonThi();
                t.setMaToHop(ma);
                t.setMon1(mapSubjectName(parts[0].split("-")[0].trim()));
                t.setMon2(mapSubjectName(parts[1].split("-")[0].trim()));
                t.setMon3(mapSubjectName(parts[2].split("-")[0].trim()));
                t.setTenToHop(t.getMon1() + "," + t.getMon2() + "," + t.getMon3());
                map.put(ma, t);
            }
        }
        System.out.println("readToHopExcel (legacy): đọc được " + map.size() + " tổ hợp.");
        return new java.util.ArrayList<>(map.values());
    }

    private static String mapSubjectName(String code) {
        switch (code.toUpperCase()) {
            case "TO": return "Toán";
            case "VA": return "Ngữ văn";
            case "LI": return "Vật lí";
            case "HO": return "Hóa học";
            case "SI": return "Sinh học";
            case "SU": return "Lịch sử";
            case "DI": return "Địa lí";
            case "N1": 
            case "TI": return "Tiếng Anh";
            case "KTPL": return "Giáo dục KTPL";
            case "CNCN": return "Công nghệ (Công nghiệp)";
            case "CNNN": return "Công nghệ (Nông nghiệp)";
            case "NK1": return "Năng khiếu 1";
            case "NK2": return "Năng khiếu 2";
            case "NK3": return "Năng khiếu 3";
            case "NK4": return "Năng khiếu 4";
            case "NK5": return "Năng khiếu 5";
            case "NK6": return "Năng khiếu 6";
            default: return code;
        }
    }

    private static boolean isNumeric(String s) {
        try { Double.parseDouble(s); return true; } catch (Exception e) { return false; }
    }

    private static String parseFlag(String s) {
        if (s == null || s.trim().isEmpty()) return "N";
        s = s.trim().toLowerCase();
        if (s.equals("có") || s.equals("co") || s.equals("y") || s.equals("1") || s.equals("yes")) return "Y";
        return "N";
    }

    private static int parseIntSafe(String s) {
        try { return (int) Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private static Integer parseIntegerSafe(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return (int) Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ĐỌC FILE NGÀNH - TỔ HỢP (docs/tohopmon.xlsx)
    // ═══════════════════════════════════════════════════════════════════
    public static List<NganhToHop> readNganhToHopExcel(File file) {
        List<NganhToHop> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            Row firstRow = sheet.getRow(0);

            if (firstRow == null || firstRow.getCell(0) == null) {
                throw new IllegalArgumentException("File trống hoặc không có tiêu đề.");
            }

            // Kiểm tra format: Cột 0 = STT, Cột 1 = MANGANH
            String col0 = fmt.formatCellValue(firstRow.getCell(0)).trim();
            String col1 = fmt.formatCellValue(firstRow.getCell(1)).trim();
            if (!col0.equalsIgnoreCase("STT") || !col1.equalsIgnoreCase("MANGANH")) {
                throw new IllegalArgumentException(
                    "File sai định dạng! File Ngành-Tổ hợp phải có cột STT, MANGANH.\n" +
                    "Vui lòng chọn đúng file tohopmon.xlsx.");
            }

            // Duyệt từng dòng (bỏ qua tiêu đề)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Cột 1: MANGANH
                String maNganh = fmt.formatCellValue(row.getCell(1)).trim();
                if (maNganh.isEmpty()) continue;

                // Cột 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)"
                String maToHopRaw = fmt.formatCellValue(row.getCell(3)).trim();
                if (maToHopRaw.isEmpty()) continue;

                // Cột 4: tb_keys dạng "7140114_B03"
                String tbKeys = fmt.formatCellValue(row.getCell(4)).trim();

                // Cột 5: TEN_TO_HOP (mã tổ hợp thuần, ví dụ: B03)
                String maToHop = fmt.formatCellValue(row.getCell(5)).trim();

                // Cột 6: Gốc
                // (không cần lưu vào entity, bỏ qua)

                // Cột 7: Độ lệch
                Double doLech = parseDoubleSafe(fmt.formatCellValue(row.getCell(7)).trim());

                // Bóc tách thông tin môn học và hệ số từ MA_TO_HOP
                NganhToHop item = new NganhToHop();
                item.setMaNganh(maNganh);
                item.setMaToHop(maToHop.isEmpty() ? extractMaToHop(maToHopRaw) : maToHop);
                item.setTbKeys(tbKeys.isEmpty() ? maNganh + "_" + item.getMaToHop() : tbKeys);
                item.setDoLech(doLech);

                // Parse phần trong ngoặc: (TO-3,VA-3,SI-1)
                parseSubjectCoefficients(maToHopRaw, item);

                list.add(item);
            }

            System.out.println("readNganhToHopExcel: đọc được " + list.size() + " liên kết Ngành-Tổ hợp.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Lỗi đọc file Ngành-Tổ hợp: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi đọc file Excel: " + e.getMessage());
        }
        return list;
    }

    /**
     * Trích xuất mã tổ hợp thuần từ chuỗi dạng "B03(TO-3,VA-3,SI-1)" → "B03"
     */
    private static String extractMaToHop(String raw) {
        int idx = raw.indexOf("(");
        return idx != -1 ? raw.substring(0, idx).trim() : raw.trim();
    }

    /**
     * Phân tích chuỗi "B03(TO-3,VA-3,SI-1)" để gán:
     *   - thMon1/hsMon1, thMon2/hsMon2, thMon3/hsMon3
     *   - Các cột điểm theo mã môn: to, va, si, li, ho, su, di, n1, ti, ktpl, khac
     */
    private static void parseSubjectCoefficients(String maToHopRaw, NganhToHop item) {
        int openIdx = maToHopRaw.indexOf("(");
        int closeIdx = maToHopRaw.indexOf(")");
        if (openIdx == -1 || closeIdx == -1) return;

        String inside = maToHopRaw.substring(openIdx + 1, closeIdx);
        String[] parts = inside.split(",");

        for (int p = 0; p < parts.length; p++) {
            String[] codeAndCoeff = parts[p].trim().split("-");
            String code = codeAndCoeff[0].trim().toUpperCase();
            int coeff = codeAndCoeff.length > 1 ? parseIntSafe(codeAndCoeff[1].trim()) : 1;

            // Gán thMon / hsMon
            if (p == 0) { item.setThMon1(code); item.setHsMon1(coeff); }
            else if (p == 1) { item.setThMon2(code); item.setHsMon2(coeff); }
            else if (p == 2) { item.setThMon3(code); item.setHsMon3(coeff); }

            // Gán cột điểm theo mã môn
            switch (code) {
                case "TO": item.setTo(coeff); break;
                case "VA": item.setVa(coeff); break;
                case "LI": item.setLi(coeff); break;
                case "HO": item.setHo(coeff); break;
                case "SI": item.setSi(coeff); break;
                case "SU": item.setSu(coeff); break;
                case "DI": item.setDi(coeff); break;
                case "N1": item.setN1(coeff); break;
                case "TI": item.setTi(coeff); break;
                case "KTPL": item.setKtpl(coeff); break;
                default: item.setKhac(coeff); break;
            }
        }
    }
}
