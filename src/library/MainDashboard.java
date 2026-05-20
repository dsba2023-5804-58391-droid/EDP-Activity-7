package library;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * MainDashboard — Activity 5 + 6 combined main window.
 * Sidebar: Dashboard | User Management | Borrow | Return | Fines | Reports | About
 */
public class MainDashboard extends JFrame {

    private final Account currentUser;
    private JPanel        contentArea;
    private CardLayout    cardLayout;

    private JButton btnNavDash, btnNavUsers, btnNavBorrow,
                    btnNavReturn, btnNavFines, btnNavReport, btnNavAbout;
    private JButton activeNavBtn;

    private UserManagementPanel  userPanel;
    private BorrowTransactionForm borrowForm;
    private ReturnTransactionForm returnForm;
    private FinePaymentForm       fineForm;
    private ReportPanel           reportPanel;

    public MainDashboard(Account user) {
        this.currentUser = user;
        setTitle("Library Management System — " + user.getFullName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 560));
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LoginForm.AMBER_50);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);
        navigate(btnNavDash);
    }

    // ── Sidebar ──────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(LoginForm.AMBER_800);
        side.setPreferredSize(new Dimension(228, 0));

        // Logo block
        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBackground(LoginForm.AMBER_800);
        logo.setBorder(new EmptyBorder(20,18,16,18));

        JLabel ico = new JLabel("📚");
        ico.setFont(new Font("SansSerif", Font.PLAIN, 34));
        ico.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sysName = new JLabel("Library System");
        sysName.setFont(new Font("Serif", Font.BOLD, 15));
        sysName.setForeground(LoginForm.AMBER_50);
        sysName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel sysSubt = new JLabel("Management Portal");
        sysSubt.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sysSubt.setForeground(LoginForm.AMBER_100);
        sysSubt.setAlignmentX(LEFT_ALIGNMENT);

        logo.add(ico);
        logo.add(Box.createVerticalStrut(4));
        logo.add(sysName);
        logo.add(sysSubt);

        // Nav buttons
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(LoginForm.AMBER_800);
        nav.setBorder(new EmptyBorder(8,12,8,12));

        btnNavDash   = navBtn("🏠  Dashboard");
        btnNavUsers  = navBtn("👥  User Management");

        JLabel txnSep = sectionLabel("TRANSACTIONS");
        btnNavBorrow = navBtn("📖  Borrow Book");
        btnNavReturn = navBtn("↩  Return Book");
        btnNavFines  = navBtn("💰  Fine Payment");

        JLabel repSep = sectionLabel("REPORTS");
        btnNavReport = navBtn("📊  Reports & Export");

        JLabel otherSep = sectionLabel("OTHER");
        btnNavAbout  = navBtn("ℹ  About");

        btnNavDash.addActionListener(e   -> navigate(btnNavDash));
        btnNavUsers.addActionListener(e  -> navigate(btnNavUsers));
        btnNavBorrow.addActionListener(e -> navigate(btnNavBorrow));
        btnNavReturn.addActionListener(e -> navigate(btnNavReturn));
        btnNavFines.addActionListener(e  -> navigate(btnNavFines));
        btnNavReport.addActionListener(e -> navigate(btnNavReport));
        btnNavAbout.addActionListener(e  -> navigate(btnNavAbout));

        nav.add(btnNavDash);
        nav.add(Box.createVerticalStrut(2));
        nav.add(btnNavUsers);
        nav.add(Box.createVerticalStrut(8));
        nav.add(txnSep);
        nav.add(Box.createVerticalStrut(2));
        nav.add(btnNavBorrow);
        nav.add(Box.createVerticalStrut(2));
        nav.add(btnNavReturn);
        nav.add(Box.createVerticalStrut(2));
        nav.add(btnNavFines);
        nav.add(Box.createVerticalStrut(8));
        nav.add(repSep);
        nav.add(Box.createVerticalStrut(2));
        nav.add(btnNavReport);
        nav.add(Box.createVerticalStrut(8));
        nav.add(otherSep);
        nav.add(Box.createVerticalStrut(2));
        nav.add(btnNavAbout);

        // Spacer
        JPanel spacer = new JPanel();
        spacer.setBackground(LoginForm.AMBER_800);
        spacer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // User box
        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.Y_AXIS));
        userBox.setBackground(new Color(0x78,0x35,0x0F));
        userBox.setBorder(new EmptyBorder(12,16,12,16));
        userBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel uName = new JLabel(currentUser.getFullName());
        uName.setFont(new Font("SansSerif", Font.BOLD, 12));
        uName.setForeground(LoginForm.AMBER_50);
        uName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel uRole = new JLabel(currentUser.getRole() + " · " + currentUser.getStatus());
        uRole.setFont(new Font("SansSerif", Font.PLAIN, 11));
        uRole.setForeground(LoginForm.AMBER_100);
        uRole.setAlignmentX(LEFT_ALIGNMENT);

        JButton btnLogout = new JButton("🚪  Logout");
        btnLogout.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnLogout.setForeground(new Color(0xFC,0xD3,0x4D));
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setAlignmentX(LEFT_ALIGNMENT);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> doLogout());

        userBox.add(uName);
        userBox.add(Box.createVerticalStrut(2));
        userBox.add(uRole);
        userBox.add(Box.createVerticalStrut(6));
        userBox.add(btnLogout);

        side.add(logo);
        side.add(divider());
        side.add(nav);
        side.add(spacer);
        side.add(divider());
        side.add(userBox);
        return side;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(LoginForm.AMBER_500);
        l.setBorder(new EmptyBorder(4,4,2,4));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setForeground(LoginForm.AMBER_100);
        b.setBackground(LoginForm.AMBER_800);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(7,12,7,12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (b != activeNavBtn) b.setBackground(new Color(0xA3,0x50,0x0E));
            }
            public void mouseExited(MouseEvent e) {
                if (b != activeNavBtn) b.setBackground(LoginForm.AMBER_800);
            }
        });
        return b;
    }

    private JPanel divider() {
        JPanel d = new JPanel();
        d.setBackground(new Color(0xFF,0xFF,0xFF,25));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        return d;
    }

    // ── Content ──────────────────────────────────────────────────────
    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(LoginForm.AMBER_50);

        contentArea.add(buildDashboard(), "dashboard");

        userPanel   = new UserManagementPanel(this, currentUser);
        borrowForm  = new BorrowTransactionForm(currentUser);
        returnForm  = new ReturnTransactionForm(currentUser);
        fineForm    = new FinePaymentForm(currentUser);
        reportPanel = new ReportPanel(currentUser);

        contentArea.add(userPanel,   "users");
        contentArea.add(borrowForm,  "borrow");
        contentArea.add(returnForm,  "return");
        contentArea.add(fineForm,    "fines");
        contentArea.add(reportPanel, "reports");
        contentArea.add(buildAbout(), "about");

        return contentArea;
    }

    // ── Dashboard ─────────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LoginForm.AMBER_50);
        p.setBorder(new EmptyBorder(26,30,26,30));

        JLabel title = new JLabel("📊  Dashboard");
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(LoginForm.AMBER_900);

        JLabel sub = new JLabel("Welcome, " + currentUser.getFullName() + "! Library overview below.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(LoginForm.AMBER_700);

        JPanel hdr = new JPanel();
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.setBackground(LoginForm.AMBER_50);
        hdr.setBorder(new EmptyBorder(0,0,18,0));
        hdr.add(title);
        hdr.add(Box.createVerticalStrut(3));
        hdr.add(sub);

        JPanel stats = new JPanel(new GridLayout(1,4,14,0));
        stats.setBackground(LoginForm.AMBER_50);
        stats.add(statCard("📚","Total Books",    "12,458",new Color(0x3B,0x82,0xF6)));
        stats.add(statCard("👥","Active Members", "3,247", new Color(0x22,0xC5,0x5E)));
        stats.add(statCard("📖","Books Borrowed", "892",   LoginForm.AMBER_500));
        stats.add(statCard("💰","Pending Fines",  "₱1,850",new Color(0xE2,0x4B,0x4A)));

        JPanel quickLinks = new JPanel(new GridLayout(1,3,14,0));
        quickLinks.setBackground(LoginForm.AMBER_50);
        quickLinks.setBorder(new EmptyBorder(14,0,0,0));

        quickLinks.add(quickCard("📖  New Borrow", "Record a book being borrowed", LoginForm.AMBER_700, () -> navigate(btnNavBorrow)));
        quickLinks.add(quickCard("↩  Process Return","Mark a book as returned",   new Color(0x05,0x96,0x69), () -> navigate(btnNavReturn)));
        quickLinks.add(quickCard("📊  Generate Report","Export data to Excel",    new Color(0x1D,0x4E,0xD8), () -> navigate(btnNavReport)));

        JPanel center = new JPanel(new BorderLayout(0,0));
        center.setBackground(LoginForm.AMBER_50);
        center.add(stats,      BorderLayout.NORTH);
        center.add(quickLinks, BorderLayout.CENTER);

        p.add(hdr,    BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel statCard(String icon, String label, String value, Color accent) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)),
            new EmptyBorder(16,18,16,18)
        ));
        JLabel i = new JLabel(icon);
        i.setFont(new Font("SansSerif",Font.PLAIN,28));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Serif",Font.BOLD,22));
        v.setForeground(accent);
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif",Font.PLAIN,12));
        l.setForeground(LoginForm.AMBER_700);
        c.add(i); c.add(Box.createVerticalStrut(6)); c.add(v); c.add(Box.createVerticalStrut(2)); c.add(l);
        return c;
    }

    private JPanel quickCard(String title, String sub, Color bg, Runnable action) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(bg);
        c.setBorder(new EmptyBorder(18,18,18,18));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif",Font.BOLD,14));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("<html>"+sub+"</html>");
        s.setFont(new Font("SansSerif",Font.PLAIN,12));
        s.setForeground(new Color(0xFF,0xFF,0xFF,200));
        c.add(t, BorderLayout.NORTH);
        c.add(s, BorderLayout.CENTER);
        c.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ action.run(); }
            public void mouseEntered(MouseEvent e){ c.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { c.setBackground(bg); }
        });
        return c;
    }

    // ── About ─────────────────────────────────────────────────────────
    private JPanel buildAbout() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(LoginForm.AMBER_50);
        p.setBorder(new EmptyBorder(26,30,26,30));
        JLabel title = new JLabel("ℹ  About the System");
        title.setFont(new Font("Serif",Font.BOLD,24));
        title.setForeground(LoginForm.AMBER_900);
        JTextArea area = new JTextArea(
            "Library Management System  |  Activity 5 + 6\n\n" +
            "Database: MySQL — library_system\n\n" +
            "Activity 5 Features:\n" +
            "  ✅ User Authentication\n" +
            "  ✅ Password Recovery\n" +
            "  ✅ User Management (Add, Edit, Activate/Deactivate, Search)\n" +
            "  ✅ Public MySQL Connection Class (DatabaseConnection.java)\n\n" +
            "Activity 6 Features:\n" +
            "  ✅ Transaction 1: Book Borrowing\n" +
            "  ✅ Transaction 2: Book Return\n" +
            "  ✅ Transaction 3: Fine Payment (auto-generated for overdue)\n" +
            "  ✅ Report Generation with Data Grid\n" +
            "  ✅ Export to Excel (.xlsx)\n" +
            "       Sheet 1: Company header + logo + data table + signature\n" +
            "       Sheet 2: Bar/pie chart from data\n\n" +
            "Tech Stack: Java Swing + JDBC + MySQL + Apache POI\n\n" +
            "© 2026 Library Management System — All rights reserved."
        );
        area.setFont(new Font("Monospaced",Font.PLAIN,13));
        area.setForeground(LoginForm.AMBER_900);
        area.setBackground(Color.WHITE);
        area.setEditable(false);
        area.setBorder(new EmptyBorder(18,18,18,18));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xFD,0xE6,0x8A)));
        p.add(title, BorderLayout.NORTH);
        p.add(Box.createVerticalStrut(14));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ── Navigation ────────────────────────────────────────────────────
    private void navigate(JButton btn) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(LoginForm.AMBER_800);
            activeNavBtn.setForeground(LoginForm.AMBER_100);
            activeNavBtn.setFont(new Font("SansSerif",Font.PLAIN,13));
        }
        activeNavBtn = btn;
        btn.setBackground(LoginForm.AMBER_700);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif",Font.BOLD,13));

        String card = "dashboard";
        if (btn == btnNavUsers)  card = "users";
        else if (btn == btnNavBorrow) card = "borrow";
        else if (btn == btnNavReturn) card = "return";
        else if (btn == btnNavFines)  card = "fines";
        else if (btn == btnNavReport) card = "reports";
        else if (btn == btnNavAbout)  card = "about";

        cardLayout.show(contentArea, card);

        if (btn == btnNavUsers)  userPanel.loadAccounts(null);
        if (btn == btnNavBorrow) borrowForm.loadTable();
        if (btn == btnNavReturn) returnForm.loadTable();
        if (btn == btnNavFines)  fineForm.loadTable();
    }

    private void doLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}