package gui.nguyenvong;

import bus.NguyenVongBUS;
import gui.component.CustomButton;
import gui.style.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class XetTuyenDialog extends JDialog {

    private final NguyenVongBUS bus;
    private boolean completed = false;
    private String  summary   = "";

    private JProgressBar progressBar;
    private JLabel       lblStatus;
    private CustomButton btnRun, btnClose;

    public XetTuyenDialog(Window parent, NguyenVongBUS bus) {
        super(parent, "Chạy xét tuyển", ModalityType.APPLICATION_MODAL);
        this.bus = bus;
        setSize(480, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UIConstants.BACKGROUND_COLOR);
        root.setBorder(new EmptyBorder(20, 24, 16, 24));
        setContentPane(root);

        // ── Cảnh báo ──
        JPanel pnlWarn = new JPanel(new BorderLayout(10, 0));
        pnlWarn.setOpaque(false);
        pnlWarn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 158, 11)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        pnlWarn.setBackground(new Color(254, 243, 199));
        pnlWarn.setOpaque(true);

        JLabel lblWarnText = new JLabel("<html><b>Cảnh báo:</b> Thao tác này sẽ tính lại toàn bộ điểm và kết quả<br>"
                + "cho tất cả nguyện vọng. Dữ liệu cũ sẽ bị ghi đè. Tiếp tục?</html>");
        lblWarnText.setFont(UIConstants.FONT_NORMAL);
        lblWarnText.setForeground(new Color(92, 46, 0));
        pnlWarn.add(lblWarnText, BorderLayout.CENTER);
        root.add(pnlWarn, BorderLayout.NORTH);

        // ── Progress ──
        JPanel pnlProgress = new JPanel(new BorderLayout(0, 8));
        pnlProgress.setOpaque(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Sẵn sàng...");
        progressBar.setPreferredSize(new Dimension(0, 28));
        progressBar.setForeground(UIConstants.SUCCESS_COLOR);

        lblStatus = new JLabel("Nhấn \"Chạy xét tuyển\" để bắt đầu.", SwingConstants.CENTER);
        lblStatus.setFont(UIConstants.FONT_NORMAL);
        lblStatus.setForeground(UIConstants.TABLE_HEADER_COLOR);

        pnlProgress.add(progressBar, BorderLayout.NORTH);
        pnlProgress.add(lblStatus,   BorderLayout.CENTER);
        root.add(pnlProgress, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlBtn.setOpaque(false);
        btnRun   = new CustomButton("Chạy xét tuyển", UIConstants.SUCCESS_COLOR);
        btnClose = new CustomButton("Đóng",           UIConstants.GRAY_COLOR);
        btnRun.setPreferredSize(new Dimension(160, 38));
        btnClose.setPreferredSize(new Dimension(100, 38));
        btnRun.addActionListener(e -> startXetTuyen());
        btnClose.addActionListener(e -> dispose());
        pnlBtn.add(btnRun); pnlBtn.add(btnClose);
        root.add(pnlBtn, BorderLayout.SOUTH);
    }

    private void startXetTuyen() {
        btnRun.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("Đang xử lý...");
        lblStatus.setText("Đang tính điểm...");

        SwingWorker<String, String[]> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return bus.runXetTuyen((done, total, phase) ->
                        publish(new String[]{String.valueOf(done), String.valueOf(total), phase}));
            }

            @Override
            protected void process(java.util.List<String[]> chunks) {
                String[] last = chunks.getLast();
                int done = Integer.parseInt(last[0]);
                int total = Integer.parseInt(last[1]);
                String phase = last[2];
                int pct = total == 0 ? 0 : (int) (done * 100.0 / total);
                progressBar.setValue(pct);
                progressBar.setString(pct + "%");
                lblStatus.setText(phase + " — " + done + " / " + total + " nguyện vọng");
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    if (result.startsWith("Success")) {
                        summary = result.substring(8); // bỏ "Success|"
                        progressBar.setValue(100);
                        progressBar.setString("Hoàn thành!");
                        lblStatus.setText("<html><b>XÉT TUYỂN HOÀN TẤT</b><br>" + summary + "</html>");
                        lblStatus.setForeground(UIConstants.SUCCESS_COLOR);
                        completed = true;
                    } else {
                        lblStatus.setText(result);
                        lblStatus.setForeground(UIConstants.DANGER_COLOR);
                        btnRun.setEnabled(true);
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Lỗi: " + ex.getMessage());
                    lblStatus.setForeground(UIConstants.DANGER_COLOR);
                    btnRun.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    public boolean isCompleted() { return completed; }
}
