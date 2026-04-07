import gui.MainFrame;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Thiết lập giao diện hệ điều hành (Look and Feel) cho đẹp hơn giao diện Java gốc
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy GUI trong luồng Event-Dispatching Thread để đảm bảo an toàn đa luồng
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
