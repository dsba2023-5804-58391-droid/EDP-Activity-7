package library;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * BorrowTransactionForm — Transaction 1: Book Borrowing.
 * Top: input form. Bottom: data grid of all borrow records.
 */
public class BorrowTransactionForm extends JPanel {

    private final BorrowTransactionDAO dao = new BorrowTransactionDAO();
    private final Account currentUser;

    // ── Form fields ──────────────────────────────────────────────────
    private JTextField txtMember, txtBook, txtBorrowDate, txtDueDate;

    // ── Table ────────────────────────────────────────────────────────
    private JTable           table;
    private DefaultTableModel model;

    private static final String[] COLS = {
        "ID","Member Name","Book Title","Borrow Date","Due Date","Return Date","Status","Processed By","Created"
    };

    public BorrowTransactionForm(Account user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 0));
        setBackground(LoginForm.AMBER_50);
        buildUI();
        loadTable();
    }

    private void buildUI() {
        // ── Page header ──────────────────────────────────────────────
        JPanel hdr = pageHeader("📖  Book Borrowing Transaction",
                "Record a new book borrow. Fill in member name, book title, and dates.");
        add(hdr, BorderLayout.NORTH);

        // ── Split: form top, grid bottom ─────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildForm(), buildGrid());
        split.setDividerLocation(230);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        split.setBackground(LoginForm.AMBER_50);
        add(split, BorderLayout.CENTER);
    }

    // ── Form ─────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xFD, 0xE6, 0x8A)),
            new EmptyBorder(20, 28, 20, 28)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Row 0
        g.gridx=0; g.gridy=0; g.weightx=0.25; card.add(formLabel("Member Name *"), g);
        g.gridx=1; g.weightx=0.75;
        txtMember = styledField("e.g. Maria Santos");
        card.add(txtMember, g);

        g.gridx=2; g.weightx=0.25; card.add(formLabel("Book Title *"), g);
        g.gridx=3; g.weightx=0.75;
        txtBook = styledField("e.g. The Great Gatsby");
        card.add(txtBook, g);

        // Row 1
        g.gridx=0; g.gridy=1; g.weightx=0.25; card.add(formLabel("Borrow Date *"), g);
        g.gridx=1; g.weightx=0.75;
        txtBorrowDate = styledField(LocalDate.now().toString());
        txtBorrowDate.setText(LocalDate.now().toString());
        card.add(txtBorrowDate, g);

        g.gridx=2; g.weightx=0.25; card.add(formLabel("Due Date *"), g);
        g.gridx=3; g.weightx=0.75;
        txtDueDate = styledField(LocalDate.now().plusDays(14).toString());
        txtDueDate.setText(LocalDate.now().plusDays(14).toString());
        card.add(txtDueDate, g);

        // Row 2 — buttons
        g.gridx=0; g.gridy=2; g.gridwidth=2; g.weightx=0.5;
        JButton btnSave = actionBtn("💾  Save Borrow Transaction", LoginForm.AMBER_700);
        btnSave.addActionListener(e -> doSave());
        card.add(btnSave, g);

        g.gridx=2; g.gridwidth=2; g.weightx=0.5;
        JButton btnClear = actionBtn("✕  Clear Fields", new Color(0x64, 0x74, 0x8B));
        btnClear.addActionListener(e -> clearFields());
        card.add(btnClear, g);

        return card;
    }

    // ── Data Grid ─────────────────────────────────────────────────────
    private JPanel buildGrid() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LoginForm.AMBER_50);
        p.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel lbl = new JLabel("📋  Borrow Records");
        lbl.setFont(new Font("Serif", Font.BOLD, 15));
        lbl.setForeground(LoginForm.AMBER_900);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = styledTable(model);

        int[] widths = {40,130,160,90,90,90,80,100,130};
        for (int i=0; i<widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        statusRenderer(table, 6);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)));

        p.add(lbl,    BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Actions ──────────────────────────────────────────────────────
    private void doSave() {
        String member = txtMember.getText().trim();
        String book   = txtBook.getText().trim();
        String bdate  = txtBorrowDate.getText().trim();
        String ddate  = txtDueDate.getText().trim();

        if (member.isEmpty() || book.isEmpty() || bdate.isEmpty() || ddate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dao.addBorrow(member, book, bdate, ddate, currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "✅ Borrow transaction saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtMember.setText("");
        txtBook.setText("");
        txtBorrowDate.setText(LocalDate.now().toString());
        txtDueDate.setText(LocalDate.now().plusDays(14).toString());
    }

    public void loadTable() {
        model.setRowCount(0);
        for (Object[] row : dao.getAll()) model.addRow(row);
    }

    // ── Shared helpers (also used by other form classes) ─────────────
    static JPanel pageHeader(String title, String sub) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(LoginForm.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xFD,0xE6,0x8A)),
            new EmptyBorder(16, 22, 14, 22)
        ));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Serif", Font.BOLD, 20));
        t.setForeground(LoginForm.AMBER_900);
        JLabel s = new JLabel(sub);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        s.setForeground(LoginForm.AMBER_700);
        p.add(t); p.add(Box.createVerticalStrut(3)); p.add(s);
        return p;
    }

    static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LoginForm.SANS_BOLD);
        l.setForeground(LoginForm.AMBER_900);
        return l;
    }

    static JTextField styledField(String tip) {
        JTextField f = new JTextField();
        f.setFont(LoginForm.SANS);
        f.setToolTipText(tip);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFC,0xD3,0x4D)),
            new EmptyBorder(5,8,5,8)
        ));
        f.setPreferredSize(new Dimension(160, 32));
        return f;
    }

    static JButton actionBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(LoginForm.SANS_BOLD);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8,16,8,16));
        return b;
    }

    static JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(LoginForm.SANS);
        t.setRowHeight(28);
        t.setGridColor(new Color(0xFE,0xF3,0xC7));
        t.setShowHorizontalLines(true);
        t.setShowVerticalLines(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        JTableHeader h = t.getTableHeader();
        h.setFont(LoginForm.SANS_BOLD);
        h.setBackground(LoginForm.AMBER_100);
        h.setForeground(LoginForm.AMBER_900);
        h.setReorderingAllowed(false);
        return t;
    }

    static void statusRenderer(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                cell.setHorizontalAlignment(CENTER);
                String val = v == null ? "" : v.toString();
                if (!sel) {
                    switch (val) {
                        case "Borrowed": cell.setBackground(new Color(0xE6,0xF1,0xFB)); cell.setForeground(new Color(0x0C,0x44,0x7C)); break;
                        case "Returned": cell.setBackground(new Color(0xDC,0xFC,0xE7)); cell.setForeground(new Color(0x16,0x6B,0x39)); break;
                        case "Overdue":  cell.setBackground(new Color(0xFC,0xEB,0xEB)); cell.setForeground(new Color(0xA3,0x2D,0x2D)); break;
                        case "Paid":     cell.setBackground(new Color(0xDC,0xFC,0xE7)); cell.setForeground(new Color(0x16,0x6B,0x39)); break;
                        case "Unpaid":   cell.setBackground(new Color(0xFC,0xEB,0xEB)); cell.setForeground(new Color(0xA3,0x2D,0x2D)); break;
                        case "Waived":   cell.setBackground(new Color(0xF3,0xF4,0xF6)); cell.setForeground(new Color(0x6B,0x72,0x80)); break;
                        default:         cell.setBackground(Color.WHITE); cell.setForeground(LoginForm.AMBER_900);
                    }
                }
                cell.setFont(new Font("SansSerif", Font.BOLD, 11));
                return cell;
            }
        });
    }
}
