package util;

import entity.Nganh;
import entity.ThiSinh;
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

    /**
     * Đọc file Excel danh sách ngành.
     * Hỗ trợ 2 định dạng phổ biến:
     *  - "Chi tieu 2025.xlsx": bỏ 2 dòng đầu (tiêu đề + header), cột [0]=STT [1]=Mã [2]=Tên [3]=ChiTieu
     *  - "Nguong dau vao 2025.xlsx": bỏ 1 dòng header, cột [0]=STT [1]=Mã [2]=Tên [3]=NgưỡngĐiểm
     * Hàm tự nhận biết dựa vào nội dung dòng đầu tiên.
     */
    public static List<Nganh> readNganhExcel(File file) {
        List<Nganh> list = new ArrayList<>();
        DataFormatter fmt = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            // Xác định dòng bắt đầu dữ liệu:
            // Nếu ô [0][0] là null hoặc không phải số → có dòng tiêu đề (bắt đầu từ row 2)
            Row firstRow = sheet.getRow(0);
            boolean hasTitleRow = (firstRow == null)
                    || (firstRow.getCell(0) == null)
                    || fmt.formatCellValue(firstRow.getCell(0)).trim().isEmpty()
                    || !isNumeric(fmt.formatCellValue(firstRow.getCell(0)).trim());
            int startRow = hasTitleRow ? 2 : 1; // row 2 = bỏ tiêu đề + header; row 1 = chỉ bỏ header

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String ma  = fmt.formatCellValue(row.getCell(1)).trim();
                String ten = fmt.formatCellValue(row.getCell(2)).trim();
                if (ma.isEmpty() || ten.isEmpty()) continue;

                String chiTieuStr = fmt.formatCellValue(row.getCell(3)).trim();
                int chiTieu = 0;
                try { chiTieu = (int) Double.parseDouble(chiTieuStr); } catch (Exception ignored) {}

                Nganh n = new Nganh();
                n.setMaNganh(ma);
                n.setTenNganh(ten);
                n.setChiTieu(chiTieu);
                list.add(n);
            }

            System.out.println("readNganhExcel: đọc được " + list.size() + " ngành.");
        } catch (Exception e) {
            System.err.println("Lỗi đọc file Ngành: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private static boolean isNumeric(String s) {
        try { Double.parseDouble(s); return true; } catch (Exception e) { return false; }
    }
}
