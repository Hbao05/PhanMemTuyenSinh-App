package util;

import entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ExcelUtil {

    /**
     * Số dòng dữ liệu tối đa đọc mỗi lần khi import thí sinh (bỏ qua dòng tiêu đề).
     */
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
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            for (int start = 1; start <= lastRowNum; start += batchSize) {
                int end = Math.min(start + batchSize - 1, lastRowNum);
                List<ThiSinh> batch = readCandidateRows(sheet, dataFormatter, evaluator, start, end);
                onBatch.accept(batch);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc file Excel thí sinh: " + e.getMessage(), e);
        }
    }

    private static List<ThiSinh> readCandidateRows(Sheet sheet, DataFormatter dataFormatter,
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator,
            int fromRowInclusive, int toRowInclusive) {
        List<ThiSinh> list = new ArrayList<>();
        for (int i = fromRowInclusive; i <= toRowInclusive; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            ThiSinh ts = rowToThiSinh(row, dataFormatter, evaluator);
            if (ts != null) {
                list.add(ts);
            }
        }
        return list;
    }

    /**
     * Một dòng Excel → ThiSinh, hoặc {@code null} nếu bỏ qua (không có CCCD).
     * Thứ tự cột: cccd, sbd, họ, tên, ngày sinh, điện thoại, giới tính, email, nơi
     * sinh, đối tượng, khu vực.
     */
    private static ThiSinh rowToThiSinh(Row row, DataFormatter dataFormatter, org.apache.poi.ss.usermodel.FormulaEvaluator evaluator) {
        String cccd = dataFormatter.formatCellValue(row.getCell(0), evaluator).trim();
        if (cccd.isEmpty()) {
            return null;
        }
        String soBaoDanh = dataFormatter.formatCellValue(row.getCell(1), evaluator).trim();
        String ho = dataFormatter.formatCellValue(row.getCell(2), evaluator).trim();
        String ten = dataFormatter.formatCellValue(row.getCell(3), evaluator).trim();
        String dob = dataFormatter.formatCellValue(row.getCell(4), evaluator).trim();
        String phone = dataFormatter.formatCellValue(row.getCell(5), evaluator).trim();
        String gender = dataFormatter.formatCellValue(row.getCell(6), evaluator).trim();
        String email = dataFormatter.formatCellValue(row.getCell(7), evaluator).trim();
        String birthPlace = dataFormatter.formatCellValue(row.getCell(8), evaluator).trim();
        String priorityObj = dataFormatter.formatCellValue(row.getCell(9), evaluator).trim();
        String priorityZone = dataFormatter.formatCellValue(row.getCell(10), evaluator).trim();

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
     * 
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
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            if (lastRowNum >= 1) {
                candidateList = readCandidateRows(sheet, dataFormatter, evaluator, 1, lastRowNum);
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
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            // Xác định dòng bắt đầu dữ liệu và định dạng
            Row firstRow = sheet.getRow(0);
            boolean hasTitleRow = false;
            boolean isNewFormat = false;

            if (firstRow != null && firstRow.getCell(0) != null) {
                String firstColStr = fmt.formatCellValue(firstRow.getCell(0), evaluator).trim();
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
                    if (row == null)
                        continue;

                    String ma = fmt.formatCellValue(row.getCell(0), evaluator).trim();
                    if (ma.isEmpty())
                        continue;

                    String ten = fmt.formatCellValue(row.getCell(1), evaluator).trim();
                    String toHop = fmt.formatCellValue(row.getCell(2), evaluator).trim();
                    if (toHop.contains("(")) {
                        toHop = toHop.substring(0, toHop.indexOf("(")).trim();
                    }

                    int chiTieu = parseIntSafe(fmt.formatCellValue(row.getCell(4), evaluator).trim());
                    Double diemSan = parseDoubleSafe(fmt.formatCellValue(row.getCell(5), evaluator).trim());
                    Double diemTrungTuyen = parseDoubleSafe(fmt.formatCellValue(row.getCell(6), evaluator).trim());

                    String xtt = parseFlag(fmt.formatCellValue(row.getCell(7), evaluator).trim());
                    String dgnl = parseFlag(fmt.formatCellValue(row.getCell(8), evaluator).trim());
                    String thpt = parseFlag(fmt.formatCellValue(row.getCell(9), evaluator).trim());
                    String vsat = parseFlag(fmt.formatCellValue(row.getCell(10), evaluator).trim());

                    Integer slXtt = parseIntegerSafe(fmt.formatCellValue(row.getCell(11), evaluator).trim());
                    Integer slDgnl = parseIntegerSafe(fmt.formatCellValue(row.getCell(12), evaluator).trim());
                    Integer slVsat = parseIntegerSafe(fmt.formatCellValue(row.getCell(13), evaluator).trim());
                    Integer slThpt = parseIntegerSafe(fmt.formatCellValue(row.getCell(14), evaluator).trim());

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
                    String col1 = fmt.formatCellValue(firstRow.getCell(1), evaluator).trim();
                    if (!col1.equalsIgnoreCase("Mã ngành") && !col1.equalsIgnoreCase("Ma nganh") && !col1.isEmpty()) {
                        if (!isNumeric(col1)) {
                            // Cột 1 không phải Mã ngành mà là chữ -> Khả năng cao sai file
                            throw new IllegalArgumentException(
                                    "File sai định dạng! Vui lòng chọn đúng file danh sách Ngành.");
                        }
                    }
                }

                // Old format fallback
                int startRow = hasTitleRow ? 2 : 1;
                for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    String ma = fmt.formatCellValue(row.getCell(1), evaluator).trim();
                    String ten = fmt.formatCellValue(row.getCell(2), evaluator).trim();
                    if (ma.isEmpty() || ten.isEmpty())
                        continue;

                    String chiTieuStr = fmt.formatCellValue(row.getCell(3), evaluator).trim();
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
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            Row firstRow = sheet.getRow(0);
            if (firstRow == null || firstRow.getCell(0) == null) {
                throw new IllegalArgumentException("File trống hoặc không có tiêu đề.");
            }
            String col0 = fmt.formatCellValue(firstRow.getCell(0)).trim();

            // Hỗ trợ đọc trực tiếp từ file docs/tohopmon.xlsx gốc
            if (col0.equalsIgnoreCase("STT")
                    && fmt.formatCellValue(firstRow.getCell(1), evaluator).trim().equalsIgnoreCase("MANGANH")) {
                return processLegacyToHopExcel(sheet, fmt, evaluator);
            }

            if (!col0.equalsIgnoreCase("Mã tổ hợp") && !col0.equalsIgnoreCase("Ma to hop")) {
                throw new IllegalArgumentException(
                        "File sai định dạng! Cột đầu tiên phải là 'Mã tổ hợp'. Vui lòng chọn đúng file Tổ hợp môn.");
            }

            // Bỏ qua dòng tiêu đề
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                // Các cột: 0: Mã tổ hợp, 1: Môn 1, 2: Môn 2, 3: Môn 3, 4: Tên tổ hợp
                String maToHop = fmt.formatCellValue(row.getCell(0), evaluator).trim();
                if (maToHop.isEmpty())
                    continue;

                String mon1 = fmt.formatCellValue(row.getCell(1), evaluator).trim().toUpperCase();
                String mon2 = fmt.formatCellValue(row.getCell(2), evaluator).trim().toUpperCase();
                String mon3 = fmt.formatCellValue(row.getCell(3), evaluator).trim().toUpperCase();
                String tenToHop = fmt.formatCellValue(row.getCell(4), evaluator).trim();

                ToHopMonThi t = new ToHopMonThi();
                t.setMaToHop(maToHop);
                t.setMon1(mon1);
                t.setMon2(mon2);
                t.setMon3(mon3);
                
                if (tenToHop.isEmpty()) {
                    tenToHop = SubjectUtil.getSubjectName(mon1) + " - " + 
                               SubjectUtil.getSubjectName(mon2) + " - " + 
                               SubjectUtil.getSubjectName(mon3);
                }
                t.setTenToHop(tenToHop);

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

    public static List<ToHopMonThi> processLegacyToHopExcel(Sheet sheet, DataFormatter fmt, org.apache.poi.ss.usermodel.FormulaEvaluator evaluator) {
        java.util.Map<String, ToHopMonThi> map = new java.util.HashMap<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null)
                continue;

            // Trong file gốc, MA_TO_HOP nằm ở cột 3 (index 3)
            String maToHopRaw = fmt.formatCellValue(row.getCell(3), evaluator).trim();
            if (maToHopRaw.isEmpty())
                continue;

            int openIdx = maToHopRaw.indexOf("(");
            int closeIdx = maToHopRaw.indexOf(")");
            if (openIdx == -1 || closeIdx == -1)
                continue;

            String ma = maToHopRaw.substring(0, openIdx).trim();
            if (map.containsKey(ma))
                continue; // Lọc trùng lặp

            String inside = maToHopRaw.substring(openIdx + 1, closeIdx);
            String[] parts = inside.split(",");
            if (parts.length >= 3) {
                String m1 = parts[0].split("-")[0].trim().toUpperCase();
                String m2 = parts[1].split("-")[0].trim().toUpperCase();
                String m3 = parts[2].split("-")[0].trim().toUpperCase();

                ToHopMonThi t = new ToHopMonThi();
                t.setMaToHop(ma);
                t.setMon1(m1);
                t.setMon2(m2);
                t.setMon3(m3);
                t.setTenToHop(SubjectUtil.getSubjectName(m1) + " - " + 
                              SubjectUtil.getSubjectName(m2) + " - " + 
                              SubjectUtil.getSubjectName(m3));
                map.put(ma, t);
            }
        }
        System.out.println("readToHopExcel (legacy): đọc được " + map.size() + " tổ hợp.");
        return new java.util.ArrayList<>(map.values());
    }



    private static boolean isNumeric(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String parseFlag(String s) {
        if (s == null || s.trim().isEmpty())
            return "N";
        s = s.trim().toLowerCase();
        if (s.equals("có") || s.equals("co") || s.equals("y") || s.equals("1") || s.equals("yes"))
            return "Y";
        return "N";
    }

    private static int parseIntSafe(String s) {
        try {
            return (int) Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private static Integer parseIntegerSafe(String s) {
        if (s == null || s.trim().isEmpty())
            return null;
        try {
            return (int) Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static Double parseDoubleSafe(String s) {
        if (s == null || s.trim().isEmpty())
            return null;
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ĐỌC FILE NGÀNH - TỔ HỢP (docs/tohopmon.xlsx)
    // ═══════════════════════════════════════════════════════════════════
    public static List<NganhToHop> readNganhToHopExcel(File file) {
        List<NganhToHop> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
                Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Row firstRow = sheet.getRow(0);

            if (firstRow == null || firstRow.getCell(0) == null) {
                throw new IllegalArgumentException("File trống hoặc không có tiêu đề.");
            }

            // Kiểm tra format: Cột 0 = STT, Cột 1 = MANGANH
            String col0 = fmt.formatCellValue(firstRow.getCell(0), evaluator).trim();
            String col1 = fmt.formatCellValue(firstRow.getCell(1), evaluator).trim();
            if (!col0.equalsIgnoreCase("STT") || !col1.equalsIgnoreCase("MANGANH")) {
                throw new IllegalArgumentException(
                        "File sai định dạng! File Ngành-Tổ hợp phải có cột STT, MANGANH.\n" +
                                "Vui lòng chọn đúng file tohopmon.xlsx.");
            }

            // Duyệt từng dòng (bỏ qua tiêu đề)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                // Cột 1: MANGANH
                String maNganh = fmt.formatCellValue(row.getCell(1), evaluator).trim();
                if (maNganh.isEmpty())
                    continue;

                // Cột 3: MA_TO_HOP dạng "B03(TO-3,VA-3,SI-1)"
                String maToHopRaw = fmt.formatCellValue(row.getCell(3), evaluator).trim();
                if (maToHopRaw.isEmpty())
                    continue;

                // Cột 4: tb_keys dạng "7140114_B03"
                String tbKeys = fmt.formatCellValue(row.getCell(4), evaluator).trim();

                // Cột 5: TEN_TO_HOP (mã tổ hợp thuần, ví dụ: B03)
                String maToHop = fmt.formatCellValue(row.getCell(5), evaluator).trim();

                // Cột 6: Gốc
                // (không cần lưu vào entity, bỏ qua)

                // Cột 7: Độ lệch
                Double doLech = parseDoubleSafe(fmt.formatCellValue(row.getCell(7), evaluator).trim());

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
     * Đọc file Excel xt_diemthixettuyen đã join sẵn.
     * Tự động nhận diện cột theo tên header (không phụ thuộc thứ tự cột).
     * Cột bắt buộc: cccd, d_phuongthuc
     */
    public static List<DiemThiXetTuyen> readDiemThiExcel(File file) {
        List<DiemThiXetTuyen> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            Row header  = sheet.getRow(0);
            if (header == null)
                throw new IllegalArgumentException("File không có dòng tiêu đề!");

            // Bước 1: Map tên cột → index (không phân biệt hoa thường)
            Map<String, Integer> idx = new HashMap<>();
            for (int c = 0; c < header.getLastCellNum(); c++) {
                Cell cell = header.getCell(c);
                if (cell != null) {
                    String name = fmt.formatCellValue(cell).trim().toUpperCase();
                    idx.put(name, c);
                }
            }

            // Bước 2: Kiểm tra cột bắt buộc
            if (!idx.containsKey("CCCD"))
                throw new IllegalArgumentException("File thiếu cột CCCD!");
            if (!idx.containsKey("D_PHUONGTHUC"))
                throw new IllegalArgumentException("File thiếu cột D_PHUONGTHUC!");

            // Bước 3: Đọc từng dòng dữ liệu
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String cccd = str(row, idx, "CCCD", fmt);
                if (cccd.isEmpty()) continue;  // bỏ qua dòng không có CCCD

                String pt = str(row, idx, "D_PHUONGTHUC", fmt);
                if (pt.isEmpty()) continue;    // bỏ qua dòng không có phương thức

                DiemThiXetTuyen dt = new DiemThiXetTuyen();
                dt.setCccd(cccd);
                dt.setSoBaoDanh(str(row, idx, "SOBAODANH", fmt));
                dt.setPhuongThuc(pt);          // "2", "3", hoặc "4"

                // Điểm THPT / VSAT (thang 10)
                dt.setDiemToan(    dbl(row, idx, "TO",      fmt));
                dt.setDiemLy(      dbl(row, idx, "LI",      fmt));
                dt.setDiemHoa(     dbl(row, idx, "HO",      fmt));
                dt.setDiemSinh(    dbl(row, idx, "SI",      fmt));
                dt.setDiemSu(      dbl(row, idx, "SU",      fmt));
                dt.setDiemDia(     dbl(row, idx, "DI",      fmt));
                dt.setDiemGdcd(    dbl(row, idx, "GDCD",    fmt));
                dt.setDiemVan(     dbl(row, idx, "VA",      fmt));
                dt.setN1Thi(       dbl(row, idx, "N1_THI",  fmt)); // điểm thi ngoại ngữ gốc
                dt.setN1Cc(        dbl(row, idx, "N1_CC",   fmt)); // max(N1_THI, quy đổi CC)
                dt.setCncn(        dbl(row, idx, "CNCN",    fmt));
                dt.setCnnn(        dbl(row, idx, "CNNN",    fmt));
                dt.setDiemTiengAnh(dbl(row, idx, "TI",      fmt)); // điểm chứng chỉ tiếng Anh
                dt.setDiemKtpl(    dbl(row, idx, "KTPL",    fmt));

                // Điểm ĐGNL (thang 1200)
                dt.setNl1(         dbl(row, idx, "NL1",     fmt));

                // Năng khiếu NK1→NK10
                dt.setNk1(         dbl(row, idx, "NK1",     fmt));
                dt.setNk2(         dbl(row, idx, "NK2",     fmt));
                dt.setNk3(         dbl(row, idx, "NK3",     fmt));
                dt.setNk4(         dbl(row, idx, "NK4",     fmt));
                dt.setNk5(         dbl(row, idx, "NK5",     fmt));
                dt.setNk6(         dbl(row, idx, "NK6",     fmt));
                dt.setNk7(         dbl(row, idx, "NK7",     fmt));
                dt.setNk8(         dbl(row, idx, "NK8",     fmt));
                dt.setNk9(         dbl(row, idx, "NK9",     fmt));
                dt.setNk10(        dbl(row, idx, "NK10",    fmt));

                list.add(dt);
            }

            System.out.println("readDiemThiExcel: đọc " + list.size() + " dòng.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi đọc file điểm thi: " + e.getMessage(), e);
        }
        return list;
    }

    // Helper: đọc String theo tên cột, trả về "" nếu không có
    private static String str(Row row, Map<String, Integer> idx,
                              String col, DataFormatter fmt) {
        Integer c = idx.get(col.toUpperCase());
        if (c == null) return "";
        Cell cell = row.getCell(c);
        return cell == null ? "" : fmt.formatCellValue(cell).trim();
    }

    // Helper: đọc Double theo tên cột, trả về null nếu rỗng hoặc không phải số
    private static Double dbl(Row row, Map<String, Integer> idx,
                              String col, DataFormatter fmt) {
        String s = str(row, idx, col, fmt);
        if (s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null; // bỏ qua giá trị không phải số (ví dụ "-")
        }
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
     * - thMon1/hsMon1, thMon2/hsMon2, thMon3/hsMon3
     * - Các cột điểm theo mã môn: to, va, si, li, ho, su, di, n1, ti, ktpl, khac
     */
    private static void parseSubjectCoefficients(String maToHopRaw, NganhToHop item) {
        int openIdx = maToHopRaw.indexOf("(");
        int closeIdx = maToHopRaw.indexOf(")");
        if (openIdx == -1 || closeIdx == -1)
            return;

        String inside = maToHopRaw.substring(openIdx + 1, closeIdx);
        String[] parts = inside.split(",");

        for (int p = 0; p < parts.length; p++) {
            String[] codeAndCoeff = parts[p].trim().split("-");
            String code = codeAndCoeff[0].trim().toUpperCase();
            int coeff = codeAndCoeff.length > 1 ? parseIntSafe(codeAndCoeff[1].trim()) : 1;

            // Gán thMon / hsMon
            if (p == 0) {
                item.setThMon1(code);
                item.setHsMon1(coeff);
            } else if (p == 1) {
                item.setThMon2(code);
                item.setHsMon2(coeff);
            } else if (p == 2) {
                item.setThMon3(code);
                item.setHsMon3(coeff);
            }

            // Gán cột điểm theo mã môn
            switch (code) {
                case "TO":
                    item.setTo(coeff);
                    break;
                case "VA":
                    item.setVa(coeff);
                    break;
                case "LI":
                    item.setLi(coeff);
                    break;
                case "HO":
                    item.setHo(coeff);
                    break;
                case "SI":
                    item.setSi(coeff);
                    break;
                case "SU":
                    item.setSu(coeff);
                    break;
                case "DI":
                    item.setDi(coeff);
                    break;
                case "N1":
                    item.setN1(coeff);
                    break;
                case "TI":
                    item.setTi(coeff);
                    break;
                case "KTPL":
                    item.setKtpl(coeff);
                    break;
                default:
                    item.setKhac(coeff);
                    break;
            }
        }
    }
}
