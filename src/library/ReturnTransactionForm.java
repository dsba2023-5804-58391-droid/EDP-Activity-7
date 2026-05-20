package library;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * ReturnTransactionForm — Transaction 2: Book Return.
 */
public class ReturnTransactionForm extends JPanel {

    private final ReturnTransactionDAO returnDAO = new ReturnTransactionDAO();
    private final BorrowTransactionDAO borrowDAO = new BorrowTransactionDAO();
    private final Account currentUser;

    // ── Form fields ──────────────────────────────────────────────────
    private JComboBox<String> cmbBorrow;
    private JTextField        txtReturnDate, txtCondition;
    private List<Object[]>    unreturned;

    // ── Table ────────────────────────────────────────────────────────
    private JTable           table;
    private DefaultTableModel model;

    private static final String[] COLS = {
        "Return ID","Borrow ID","Member Name","Book Title","Return Date","Condition","Processed By","Created"
    };

    public ReturnTransactionForm(Account user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(LoginForm.AMBER_50);
        buildUI();
        loadCombo();
        loadTable();
    }

    private void buildUI() {
        add(BorrowTransactionForm.pageHeader(
            "↩  Book Return Transaction",
            "Select a borrowed book and process the return."), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildForm(), buildGrid());
        split.setDividerLocation(210);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,1,0,new Color(0xFD,0xE6,0x8A)),
            new EmptyBorder(20,28,20,28)
        ));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill   = GridBagConstraints.HORIZONTAL;

        // Borrow record selector
        g.gridx=0; g.gridy=0; g.weightx=0.2; card.add(BorrowTransactionForm.formLabel("Select Borrow Record *"), g);
        g.gridx=1; g.gridwidth=3; g.weightx=0.8;
        cmbBorrow = new JComboBox<>();
        cmbBorrow.setFont(LoginForm.SANS);
        cmbBorrow.setPreferredSize(new Dimension(400,32));
        card.add(cmbBorrow, g);

        // Return date
        g.gridx=0; g.gridy=1; g.gridwidth=1; g.weightx=0.2;
        card.add(BorrowTransactionForm.formLabel("Return Date *"), g);
        g.gridx=1; g.weightx=0.3;
        txtReturnDate = BorrowTransactionForm.styledField(LocalDate.now().toString());
        txtReturnDate.setText(LocalDate.now().toString());
        card.add(txtReturnDate, g);

        // Condition note
        g.gridx=2; g.weightx=0.2;
        card.add(BorrowTransactionForm.formLabel("Book Condition"), g);
        g.gridx=3; g.weightx=0.3;
        txtCondition = BorrowTransactionForm.styledField("e.g. Good condition, slight damage...");
        card.add(txtCondition, g);

        // Buttons
        g.gridx=0; g.gridy=2; g.gridwidth=2; g.weightx=0.5;
        JButton btnSave = BorrowTransactionForm.actionBtn("💾  Process Return", LoginForm.AMBER_700);
        btnSave.addActionListener(e -> doReturn());
        card.add(btnSave, g);

        g.gridx=2; g.gridwidth=2; g.weightx=0.5;
        JButton btnRefresh = BorrowTransactionForm.actionBtn("🔄  Refresh List", new Color(0x64,0x74,0x8B));
        btnRefresh.addActionListener(e -> { loadCombo(); loadTable(); });
        card.add(btnRefresh, g);

        return card;
    }

    private JPanel buildGrid() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LoginForm.AMBER_50);
        p.setBorder(new EmptyBorder(10,16,10,16));

        JLabel lbl = new JLabel("📋  Return Records");
        lbl.setFont(new Font("Serif", Font.BOLD, 15));
        lbl.setForeground(LoginForm.AMBER_900);
        lbl.setBorder(new EmptyBorder(0,0,8,0));

        model = new DefaultTableModel(COLS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = BorrowTransactionForm.styledTable(model);

        int[] widths = {70,70,140,170,90,150,100,130};
        for (int i=0; i<widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)));

        p.add(lbl,    BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void loadCombo() {
        cmbBorrow.removeAllItems();
        unreturned = borrowDAO.getUnreturned();
        if (unreturned.isEmpty()) {
            cmbBorrow.addItem("— No unreturned books —");
        } else {
            for (Object[] row : unreturned) {
                cmbBorrow.addItem("ID:" + row[0] + " | " + row[1] + " — " + row[2] + " (Due: " + row[4] + ")");
            }
        }
    }

    private void doReturn() {
        int idx = cmbBorrow.getSelectedIndex();
        if (idx < 0 || unreturned == null || unreturned.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a borrow record.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object[] selected = unreturned.get(idx);
        int    borrowId   = (int)    selected[0];
        String memberName = (String) selected[1];
        String bookTitle  = (String) selected[2];
        String returnDate = txtReturnDate.getText().trim();
        String condition  = txtCondition.getText().trim();

        if (returnDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Return date is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean r1 = returnDAO.addReturn(borrowId, memberName, bookTitle, returnDate, condition, currentUser.getUsername());
        boolean r2 = borrowDAO.markReturned(borrowId, returnDate);

        if (r1 && r2) {
            JOptionPane.showMessageDialog(this, "✅ Return processed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtCondition.setText("");
            loadCombo();
            loadTable();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to process return.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadTable() {
        model.setRowCount(0);
        for (Object[] row : returnDAO.getAll()) model.addRow(row);
    }
}
