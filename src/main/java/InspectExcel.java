import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;

public class InspectExcel {
    public static void main(String[] args) throws Exception {
        File file = new File("src/main/resources/data_import/thisinh_import.xlsx");
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            DataFormatter formatter = new DataFormatter();
            System.out.println("Headers:");
            for (int i = 0; i < header.getLastCellNum(); i++) {
                System.out.println("Cell " + i + ": " + formatter.formatCellValue(header.getCell(i)));
            }
            System.out.println("\nRow 1:");
            Row row1 = sheet.getRow(1);
            if (row1 != null) {
                for (int i = 0; i < row1.getLastCellNum(); i++) {
                    System.out.println("Cell " + i + ": " + formatter.formatCellValue(row1.getCell(i)));
                }
            }
        }
    }
}
