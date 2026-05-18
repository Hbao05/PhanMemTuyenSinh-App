import javax.swing.table.DefaultTableModel;

public class TestTable {
    public static void main(String[] args) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"Col1"}, 0) {
            public Class<?> getColumnClass(int col) {
                return String.class;
            }
        };
        model.addRow(new Object[]{ Thread.State.NEW });
        System.out.println("Row added!");
    }
}
