package util;

import entity.Nganh;
import entity.ThiSinh;
import entity.ToHopMonThi;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtil {
    /**
     * Hàm đọc file Excel Thí sinh và chỉ lấy các cột thông tin lý lịch cần thiết.
     * @param file File Excel người dùng chọn từ giao diện
     * @return Danh sách các đối tượng ThiSinh
     */
    public static List<ThiSinh> readCandidateExcel(File file) {
        List<ThiSinh> candidateList = new ArrayList<>();
        // DataFormatter giúp đọc mọi kiểu dữ liệu trong Excel (số, ngày, chữ) thành String chuẩn
        DataFormatter dataFormatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Lấy Sheet đầu tiên (Sheet 0)
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // Bỏ qua dòng 0 (dòng tiêu đề STT, CCCD...), bắt đầu từ dòng 1
            int importLimit = 200; // Giới hạn test, đổi thành lastRowNum để import toàn bộ
            for (int i = 1; i <= Math.min(lastRowNum, importLimit); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // Bỏ qua dòng trống

                // 1. Đọc dữ liệu thô từ các cột (Chỉ lấy đúng 7 cột đầu và cột Nơi sinh)
                String cccd = dataFormatter.formatCellValue(row.getCell(1)).trim();
                String fullName = dataFormatter.formatCellValue(row.getCell(2)).trim();

                // Nếu không có CCCD thì coi như dòng đó rác, bỏ qua luôn để tiết kiệm bộ nhớ
                if (cccd.isEmpty()) continue;

                String dob = dataFormatter.formatCellValue(row.getCell(3)).trim();
                String gender = dataFormatter.formatCellValue(row.getCell(4)).trim();
                String priorityObj = dataFormatter.formatCellValue(row.getCell(5)).trim();
                String priorityZone = dataFormatter.formatCellValue(row.getCell(6)).trim();
                String birthPlace = dataFormatter.formatCellValue(row.getCell(35)).trim(); // Cột 35 là Nơi sinh

                // 2. Thuật toán tách Họ và Tên
                String ho = "";
                String ten = "";
                if (!fullName.isEmpty()) {
                    int lastSpaceIndex = fullName.lastIndexOf(" ");
                    if (lastSpaceIndex == -1) {
                        // Trường hợp dữ liệu test như "TS_0001" (Không có dấu cách)
                        ten = fullName;
                    } else {
                        // Cắt từ đầu đến khoảng trắng cuối cùng làm Họ
                        ho = fullName.substring(0, lastSpaceIndex).trim();
                        // Cắt từ sau khoảng trắng cuối cùng đến hết làm Tên
                        ten = fullName.substring(lastSpaceIndex + 1).trim();
                    }
                }

                // 3. Đóng gói vào đối tượng ThiSinh
                ThiSinh ts = new ThiSinh();
                ts.setCccd(cccd);
                ts.setHo(ho);
                ts.setTen(ten);
                ts.setNgaySinh(dob);
                ts.setGioiTinh(gender);
                ts.setDoiTuong(priorityObj);
                ts.setKhuVuc(priorityZone);
                ts.setNoiSinh(birthPlace);

                // 4. Thêm vào danh sách (RAM)
                candidateList.add(ts);
            }

            System.out.println("Đã đọc thành công " + candidateList.size() + " dòng từ Excel vào RAM.");

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
}
