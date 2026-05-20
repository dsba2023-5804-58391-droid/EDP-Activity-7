package library;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * FinePaymentForm — Transaction 3: Fine Payment.
 */
public class FinePaymentForm extends JPanel {

    private final FinePaymentDAO dao = new FinePaymentDAO();
    private final Account currentUser;

    // ── Table ────────────────────────────────────────────────────────
    private JTable           table;
    private DefaultTableModel model;

    private static final String[] COLS = {
        "Fine ID","Borrow ID","Member Name","Book Title","Days Overdue","Fine Amount","Paid Amount","Status","Processed By","Created"
    };

    public FinePaymentForm(Account user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(LoginForm.AMBER_50);
        buildUI();
        generateFines();
        loadTable();
    }

    private void buildUI() {
        add(BorrowTransactionForm.pageHeader(
            "💰  Fine Payment Transaction",
            "Overdue fines are generated automatically. Select a record to process payment."), BorderLayout.NORTH);

        add(buildToolbar(), BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(LoginForm.AMBER_50);
        wrap.setBorder(new EmptyBorder(10,16,10,16));

        // ── Action row ───────────────────────────────────────────────
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionRow.setBackground(LoginForm.WHITE);
        actionRow.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)),
            new EmptyBorder(10,16,10,16)
        ));

        JLabel lbl = new JLabel("Select a row then click an action:");
        lbl.setFont(LoginForm.SANS_BOLD);
        lbl.setForeground(LoginForm.AMBER_900);

        JButton btnPay   = BorrowTransactionForm.actionBtn("💵  Process Payment", LoginForm.AMBER_700);
        JButton btnWaive = BorrowTransactionForm.actionBtn("✋  Waive Fine", new Color(0x05,0x96,0x69));
        JButton btnRef   = BorrowTransactionForm.actionBtn("🔄  Refresh", new Color(0x64,0x74,0x8B));

        btnPay.addActionListener(e   -> doPayment());
        btnWaive.addActionListener(e -> doWaive());
        btnRef.addActionListener(e   -> { generateFines(); loadTable(); });

        actionRow.add(lbl);
        actionRow.add(Box.createHorizontalStrut(10));
        actionRow.add(btnPay);
        actionRow.add(btnWaive);
        actionRow.add(btnRef);

        // ── Summary chips ────────────────────────────────────────────
        JPanel summaryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        summaryRow.setBackground(LoginForm.AMBER_50);
        summaryRow.setBorder(new EmptyBorder(8,0,4,0));
        updateSummaryChips(summaryRow);

        // ── Data grid ────────────────────────────────────────────────
        JLabel gridLbl = new JLabel("📋  Fine Records");
        gridLbl.setFont(new Font("Serif", Font.BOLD, 15));
        gridLbl.setForeground(LoginForm.AMBER_900);
        gridLbl.setBorder(new EmptyBorder(6,0,8,0));

        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = BorrowTransactionForm.styledTable(model);

        int[] widths = {60,70,130,160,90,90,90,70,100,130};
        for (int i=0; i<widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        BorrowTransactionForm.statusRenderer(table, 7);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)));

        JPanel grid = new JPanel(new BorderLayout());
        grid.setBackground(LoginForm.AMBER_50);
        grid.add(gridLbl, BorderLayout.NORTH);
        grid.add(scroll,  BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(LoginForm.AMBER_50);
        top.add(actionRow,  BorderLayout.NORTH);
        top.add(summaryRow, BorderLayout.CENTER);

        wrap.add(top,  BorderLayout.NORTH);
        wrap.add(grid, BorderLayout.CENTER);
        return wrap;
    }

    private void updateSummaryChips(JPanel p) {
        p.removeAll();
        int[] s = dao.getStatusSummary();
        p.add(chip("✅ Paid: "    + s[0], new Color(0xDC,0xFC,0xE7), new Color(0x16,0x6B,0x39)));
        p.add(chip("❌ Unpaid: "  + s[1], new Color(0xFC,0xEB,0xEB), new Color(0xA3,0x2D,0x2D)));
        p.add(chip("🙌 Waived: "  + s[2], new Color(0xF3,0xF4,0xF6), new Color(0x6B,0x72,0x80)));
        p.revalidate(); p.repaint();
    }

    private JLabel chip(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(4,10,4,10));
        return l;
    }

    private void generateFines() {
        dao.generateOverdueFines(currentUser.getUsername());
    }

    private void doPayment() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a fine record.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        String status = model.getValueAt(row, 7).toString();
        if (!"Unpaid".equals(status)) { JOptionPane.showMessageDialog(this, "This fine is already " + status + ".", "Info", JOptionPane.INFORMATION_MESSAGE); return; }

        int    fineId = (int) model.getValueAt(row, 0);
        String amount = model.getValueAt(row, 5).toString().replace("₱","");

        String input = JOptionPane.showInputDialog(this,
            "Fine amount: " + model.getValueAt(row, 5) + "\nEnter paid amount (₱):",
            amount);
        if (input == null || input.trim().isEmpty()) return;

        try {
            double paid = Double.parseDouble(input.trim());
            if (dao.processPayment(fineId, paid)) {
                JOptionPane.showMessageDialog(this, "✅ Payment processed!", "Success", JOptionPane.INFORMATION_MESSAGE);
                generateFines(); loadTable();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doWaive() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a fine record.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        int fineId = (int) model.getValueAt(row, 0);
        int c = JOptionPane.showConfirmDialog(this, "Waive this fine?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION && dao.waiveFine(fineId)) {
            JOptionPane.showMessageDialog(this, "Fine waived.", "Done", JOptionPane.INFORMATION_MESSAGE);
            generateFines(); loadTable();
        }
    }

    public void loadTable() {
        model.setRowCount(0);
        for (Object[] row : dao.getAll()) model.addRow(row);
    }
}
