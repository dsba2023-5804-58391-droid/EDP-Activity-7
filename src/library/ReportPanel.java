package library;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.util.List;

/**
 * ReportPanel — Report generation module.
 * Select report type → Generate → view in data grid → Export to Excel.
 */
public class ReportPanel extends JPanel {

    private final BorrowTransactionDAO borrowDAO = new BorrowTransactionDAO();
    private final ReturnTransactionDAO returnDAO = new ReturnTransactionDAO();
    private final FinePaymentDAO       fineDAO   = new FinePaymentDAO();
    private final Account currentUser;

    // ── Controls ─────────────────────────────────────────────────────
    private JComboBox<String> cmbType;
    private JTextField        txtFrom, txtTo;
    private JLabel            lblCount;

    // ── Table ────────────────────────────────────────────────────────
    private JTable           table;
    private DefaultTableModel model;
    private List<Object[]>   currentData;
    private String           currentType = "Borrow Transactions";

    private static final String[] REPORT_TYPES = {
        "Borrow Transactions", "Return Transactions", "Fine Payments"
    };

    public ReportPanel(Account user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(LoginForm.AMBER_50);
        buildUI();
    }

    private void buildUI() {
        // Header
        add(BorrowTransactionForm.pageHeader(
            "📊  Report Generation",
            "Select a report type, set a date range, generate, then export to Excel."), BorderLayout.NORTH);

        add(buildControls(), BorderLayout.NORTH);

        // Grid
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setBackground(LoginForm.AMBER_50);
        gridWrap.setBorder(new EmptyBorder(0,16,12,16));

        lblCount = new JLabel("No data loaded yet.");
        lblCount.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblCount.setForeground(LoginForm.AMBER_700);
        lblCount.setBorder(new EmptyBorder(6,0,6,0));

        model = new DefaultTableModel(new Object[0][0], new String[0]) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = BorrowTransactionForm.styledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)));

        gridWrap.add(lblCount, BorderLayout.NORTH);
        gridWrap.add(scroll,   BorderLayout.CENTER);
        add(gridWrap, BorderLayout.CENTER);
    }

    private JPanel buildControls() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(LoginForm.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(0xFD,0xE6,0x8A)),
            new EmptyBorder(14,22,14,22)
        ));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row.setBackground(LoginForm.WHITE);

        // Report type
        row.add(BorrowTransactionForm.formLabel("Report Type:"));
        cmbType = new JComboBox<>(REPORT_TYPES);
        cmbType.setFont(LoginForm.SANS);
        cmbType.setPreferredSize(new Dimension(200,32));
        cmbType.addActionListener(e -> currentType = (String)cmbType.getSelectedItem());
        row.add(cmbType);

        // Date from
        row.add(BorrowTransactionForm.formLabel("From:"));
        txtFrom = BorrowTransactionForm.styledField("YYYY-MM-DD");
        txtFrom.setPreferredSize(new Dimension(110,32));
        txtFrom.setText(java.time.LocalDate.now().minusMonths(1).toString());
        row.add(txtFrom);

        // Date to
        row.add(BorrowTransactionForm.formLabel("To:"));
        txtTo = BorrowTransactionForm.styledField("YYYY-MM-DD");
        txtTo.setPreferredSize(new Dimension(110,32));
        txtTo.setText(java.time.LocalDate.now().toString());
        row.add(txtTo);

        // Buttons
        JButton btnGenerate = BorrowTransactionForm.actionBtn("📋  Generate Report", LoginForm.AMBER_700);
        btnGenerate.addActionListener(e -> generateReport());
        row.add(btnGenerate);

        JButton btnAll = BorrowTransactionForm.actionBtn("📄  Show All", new Color(0x05,0x96,0x69));
        btnAll.addActionListener(e -> generateAll());
        row.add(btnAll);

        JButton btnExport = BorrowTransactionForm.actionBtn("💾  Export to Excel", new Color(0x1D,0x6F,0x42));
        btnExport.addActionListener(e -> exportExcel());
        row.add(btnExport);

        wrap.add(row, BorderLayout.CENTER);
        return wrap;
    }

    // ── Generate report (date range) ─────────────────────────────────
    private void generateReport() {
        String from = txtFrom.getText().trim();
        String to   = txtTo.getText().trim();
        if (from.isEmpty() || to.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please set date range.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        switch (currentType) {
            case "Borrow Transactions": loadBorrowData(borrowDAO.getByDateRange(from, to)); break;
            case "Return Transactions": loadReturnData(returnDAO.getByDateRange(from, to)); break;
            case "Fine Payments":       loadFineData(fineDAO.getByDateRange(from, to));     break;
        }
    }

    // ── Show all records ─────────────────────────────────────────────
    private void generateAll() {
        switch (currentType) {
            case "Borrow Transactions": loadBorrowData(borrowDAO.getAll());  break;
            case "Return Transactions": loadReturnData(returnDAO.getAll());  break;
            case "Fine Payments":       loadFineData(fineDAO.getAll());      break;
        }
    }

    // ── Load data into grid ──────────────────────────────────────────
    private void loadBorrowData(List<Object[]> data) {
        currentData = data;
        String[] cols = {"ID","Member Name","Book Title","Borrow Date","Due Date","Return Date","Status","Processed By","Created"};
        resetTable(cols, data, 6);
    }

    private void loadReturnData(List<Object[]> data) {
        currentData = data;
        String[] cols = {"Return ID","Borrow ID","Member Name","Book Title","Return Date","Condition","Processed By","Created"};
        resetTable(cols, data, -1);
    }

    private void loadFineData(List<Object[]> data) {
        currentData = data;
        String[] cols = {"Fine ID","Borrow ID","Member Name","Book Title","Days Overdue","Fine Amount","Paid Amount","Status","Processed By","Created"};
        resetTable(cols, data, 7);
    }

    private void resetTable(String[] cols, List<Object[]> data, int statusCol) {
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setModel(model);
        for (Object[] row : data) model.addRow(row);
        if (statusCol >= 0) BorrowTransactionForm.statusRenderer(table, statusCol);
        lblCount.setText("📊  " + data.size() + " record(s) loaded — " + currentType);
    }

    // ── Export to Excel ──────────────────────────────────────────────
    private void exportExcel() {
        if (currentData == null || currentData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please generate a report first before exporting.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Excel Report");
        String defaultName = currentType.replace(" ","_") + "_" + java.time.LocalDate.now() + ".xlsx";
        fc.setSelectedFile(new File(defaultName));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".xlsx")) path += ".xlsx";

        try {
            switch (currentType) {
                case "Borrow Transactions":
                    ExcelExporter.exportBorrowReport(currentData, path, currentUser.getFullName());
                    break;
                case "Return Transactions":
                    ExcelExporter.exportReturnReport(currentData, path, currentUser.getFullName());
                    break;
                case "Fine Payments":
                    ExcelExporter.exportFineReport(currentData, path, currentUser.getFullName());
                    break;
            }
            JOptionPane.showMessageDialog(this,
                "✅ Excel report exported successfully!\n\nFile saved to:\n" + path,
                "Export Complete", JOptionPane.INFORMATION_MESSAGE);

// Open Excel automatically
try {

    File excelFile = new File(path);

    // Wait briefly so Windows releases the file lock
    Thread.sleep(1500);

    if (Desktop.isDesktopSupported()) {

        Desktop desktop = Desktop.getDesktop();

        if (desktop.isSupported(Desktop.Action.OPEN)) {

            desktop.open(excelFile);

        } else {

            // Windows fallback
            Runtime.getRuntime().exec(
                new String[]{
                    "cmd",
                    "/c",
                    "start",
                    "",
                    "\"" + excelFile.getAbsolutePath() + "\""
                }
            );
        }

    } else {

        // Fallback for unsupported Desktop API
        Runtime.getRuntime().exec(
            new String[]{
                "cmd",
                "/c",
                "start",
                "",
                "\"" + excelFile.getAbsolutePath() + "\""
            }
        );
    }

} catch (Exception e) {

    e.printStackTrace();

    JOptionPane.showMessageDialog(this,
        "Excel file exported successfully but could not open automatically.");
}
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + ex.getMessage()
                + "\n\nMake sure Apache POI JARs are added to your project libraries.",
                "Export Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

