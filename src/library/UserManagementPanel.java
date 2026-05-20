package library;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * UserManagementPanel — Full user management:
 *   • Account List with Search
 *   • Add Account
 *   • Update Account Profile
 *   • Activate / Deactivate Account
 */
public class UserManagementPanel extends JPanel {

    private final AccountDAO dao = new AccountDAO();
    private final JFrame     parent;
    private final Account    loggedIn;

    // ── Table ────────────────────────────────────────────────────────
    private JTable          table;
    private DefaultTableModel model;
    private JTextField      txtSearch;
    private JLabel          lblCount;

    // ── Column indices ───────────────────────────────────────────────
    private static final int COL_ID       = 0;
    private static final int COL_NAME     = 1;
    private static final int COL_USERNAME = 2;
    private static final int COL_EMAIL    = 3;
    private static final int COL_PHONE    = 4;
    private static final int COL_ROLE     = 5;
    private static final int COL_STATUS   = 6;
    private static final int COL_CREATED  = 7;

    private static final String[] COLUMNS = {
        "ID", "Full Name", "Username", "Email", "Phone", "Role", "Status", "Created At"
    };

    public UserManagementPanel(JFrame parent, Account loggedIn) {
        this.parent    = parent;
        this.loggedIn  = loggedIn;
        setLayout(new BorderLayout(0, 0));
        setBackground(LoginForm.AMBER_50);
        buildUI();
        loadAccounts(null);
    }

    private void buildUI() {

        // ── Top bar ──────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(LoginForm.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xFD, 0xE6, 0x8A)),
            new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel heading = new JLabel("👥  User Management");
        heading.setFont(new Font("Serif", Font.BOLD, 20));
        heading.setForeground(LoginForm.AMBER_900);

        lblCount = new JLabel("0 accounts");
        lblCount.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblCount.setForeground(LoginForm.AMBER_700);

        JPanel headingPanel = new JPanel(new BorderLayout(4,2));
        headingPanel.setBackground(LoginForm.WHITE);
        headingPanel.add(heading, BorderLayout.NORTH);
        headingPanel.add(lblCount, BorderLayout.SOUTH);
        topBar.add(headingPanel, BorderLayout.WEST);

        // ── Buttons ──────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(LoginForm.WHITE);

        JButton btnAdd    = makeBtn("➕ Add Account",   LoginForm.AMBER_700, Color.WHITE);
        JButton btnEdit   = makeBtn("✏  Edit",          new Color(0x1D, 0x4E, 0xD8), Color.WHITE);
        JButton btnToggle = makeBtn("⏺  Toggle Status", new Color(0x05, 0x96, 0x69), Color.WHITE);
        JButton btnRefresh= makeBtn("🔄 Refresh",       new Color(0x64, 0x74, 0x8B), Color.WHITE);

        btnAdd.addActionListener(e -> openAddDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnToggle.addActionListener(e -> toggleSelectedStatus());
        btnRefresh.addActionListener(e -> loadAccounts(txtSearch.getText().trim()));

        btnPanel.add(btnRefresh);
        btnPanel.add(btnToggle);
        btnPanel.add(btnEdit);
        btnPanel.add(btnAdd);
        topBar.add(btnPanel, BorderLayout.EAST);

        // ── Search bar ───────────────────────────────────────────────
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(LoginForm.AMBER_50);
        searchBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel searchLbl = new JLabel("🔍 Search:");
        searchLbl.setFont(LoginForm.SANS_BOLD);
        searchLbl.setForeground(LoginForm.AMBER_900);

        txtSearch = new JTextField();
        txtSearch.setFont(LoginForm.SANS);
        txtSearch.setToolTipText("Search by name, username, email, role, or status");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFC, 0xD3, 0x4D)),
            new EmptyBorder(5, 10, 5, 10)
        ));
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                loadAccounts(txtSearch.getText().trim());
            }
        });

        JButton btnClear = new JButton("✕");
        btnClear.setToolTipText("Clear search");
        btnClear.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnClear.setForeground(LoginForm.AMBER_700);
        btnClear.setBorderPainted(false);
        btnClear.setContentAreaFilled(false);
        btnClear.addActionListener(e -> { txtSearch.setText(""); loadAccounts(null); });

        searchBar.add(searchLbl, BorderLayout.WEST);
        searchBar.add(txtSearch, BorderLayout.CENTER);
        searchBar.add(btnClear,  BorderLayout.EAST);

        // ── Table ────────────────────────────────────────────────────
        model = new DefaultTableModel(COLUMNS, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(LoginForm.SANS);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(0xFE, 0xF3, 0xC7));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        // Column widths
        int[] widths = {40, 150, 100, 180, 110, 80, 70, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Header style
        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(LoginForm.SANS_BOLD);
        hdr.setBackground(LoginForm.AMBER_100);
        hdr.setForeground(LoginForm.AMBER_900);
        hdr.setReorderingAllowed(false);

        // Status cell renderer — color Active/Inactive
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                cell.setHorizontalAlignment(CENTER);
                String val = v == null ? "" : v.toString();
                if (!sel) {
                    if ("Active".equals(val)) {
                        cell.setBackground(new Color(0xDC, 0xFC, 0xE7));
                        cell.setForeground(new Color(0x16, 0x6B, 0x39));
                    } else {
                        cell.setBackground(new Color(0xF3, 0xF4, 0xF6));
                        cell.setForeground(new Color(0x6B, 0x72, 0x80));
                    }
                }
                cell.setFont(new Font("SansSerif", Font.BOLD, 11));
                return cell;
            }
        });

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openEditDialog();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xFD, 0xE6, 0x8A)));

        // ── Layout ───────────────────────────────────────────────────
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topBar,     BorderLayout.NORTH);
        northPanel.add(searchBar,  BorderLayout.SOUTH);

        add(northPanel,             BorderLayout.NORTH);
        add(scroll,                 BorderLayout.CENTER);
        add(buildStatusBar(),       BorderLayout.SOUTH);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        bar.setBackground(LoginForm.AMBER_100);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xFD, 0xE6, 0x8A)));
        JLabel tip = new JLabel("💡 Double-click a row to edit  |  Select a row and use the buttons above  |  Logged in as: "
                + loggedIn.getFullName() + " [" + loggedIn.getRole() + "]");
        tip.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tip.setForeground(LoginForm.AMBER_800);
        bar.add(tip);
        return bar;
    }

    // ── Data loading ─────────────────────────────────────────────────
    public void loadAccounts(String search) {
        model.setRowCount(0);
        List<Account> list = (search == null || search.isEmpty())
                ? dao.getAll()
                : dao.search(search);

        for (Account a : list) {
            model.addRow(new Object[]{
                a.getAccountId(),
                a.getFullName(),
                a.getUsername(),
                a.getEmail(),
                a.getPhone() == null ? "" : a.getPhone(),
                a.getRole(),
                a.getStatus(),
                a.getCreatedAt() == null ? "" : a.getCreatedAt().toString().substring(0, 19)
            });
        }
        int total = dao.getAll().size();
        long active   = dao.getAll().stream().filter(a -> "Active".equals(a.getStatus())).count();
        long inactive = total - active;
        lblCount.setText(list.size() + " shown  |  Total: " + total
                + "  ✅ Active: " + active + "  ⛔ Inactive: " + inactive);
    }

    // ── Actions ──────────────────────────────────────────────────────
    private void openAddDialog() {
        AccountFormDialog dlg = new AccountFormDialog(parent, null);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadAccounts(txtSearch.getText().trim());
    }

    private void openEditDialog() {
        Account selected = getSelectedAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(parent, "Please select an account to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AccountFormDialog dlg = new AccountFormDialog(parent, selected);
        dlg.setVisible(true);
        if (dlg.isSaved()) loadAccounts(txtSearch.getText().trim());
    }

    private void toggleSelectedStatus() {
        Account selected = getSelectedAccount();
        if (selected == null) {
            JOptionPane.showMessageDialog(parent, "Please select an account.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newStatus = "Active".equals(selected.getStatus()) ? "Inactive" : "Active";
        String icon      = "Active".equals(newStatus) ? "✅" : "⛔";

        int confirm = JOptionPane.showConfirmDialog(parent,
            icon + "  Set \"" + selected.getFullName() + "\" to " + newStatus + "?",
            "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.toggleStatus(selected.getAccountId(), newStatus)) {
                JOptionPane.showMessageDialog(parent, "Account status updated to " + newStatus + ".", "Updated", JOptionPane.INFORMATION_MESSAGE);
                loadAccounts(txtSearch.getText().trim());
            } else {
                JOptionPane.showMessageDialog(parent, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Account getSelectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, COL_ID);
        List<Account> all = dao.getAll();
        return all.stream().filter(a -> a.getAccountId() == id).findFirst().orElse(null);
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7, 14, 7, 14));
        return b;
    }
}
