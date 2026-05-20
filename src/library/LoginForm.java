package library;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginForm — User Authentication screen.
 * Entry point of the Library Management System.
 */
public class LoginForm extends JFrame {

    // ── Fields ──────────────────────────────────────────────────────
    private JTextField     txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox      chkShow;
    private JButton        btnLogin;
    private JButton        btnForgot;

    // ── Colors (matching amber/library theme) ────────────────────────
    static final Color AMBER_900  = new Color(0x78, 0x35, 0x0F);
    static final Color AMBER_800  = new Color(0x92, 0x40, 0x0E);
    static final Color AMBER_700  = new Color(0xB4, 0x53, 0x09);
    static final Color AMBER_500  = new Color(0xF5, 0x9E, 0x0B);
    static final Color AMBER_100  = new Color(0xFE, 0xF3, 0xC7);
    static final Color AMBER_50   = new Color(0xFF, 0xFB, 0xEB);
    static final Color WHITE      = Color.WHITE;
    static final Font  SERIF_BIG  = new Font("Serif", Font.BOLD, 22);
    static final Font  SERIF_MED  = new Font("Serif", Font.BOLD, 16);
    static final Font  SANS       = new Font("SansSerif", Font.PLAIN, 13);
    static final Font  SANS_BOLD  = new Font("SansSerif", Font.BOLD, 13);

    public LoginForm() {
        setTitle("Library Management System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AMBER_50);

        // ── Header ──────────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setBackground(AMBER_800);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(28, 24, 22, 24));

        JLabel icon = new JLabel("📚", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 44));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Library Management System", SwingConstants.CENTER);
        title.setFont(SERIF_BIG);
        title.setForeground(AMBER_50);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account", SwingConstants.CENTER);
        sub.setFont(SANS);
        sub.setForeground(AMBER_100);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(8));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ── Form Card ───────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFD, 0xE6, 0x8A), 1),
            new EmptyBorder(28, 32, 24, 32)
        ));

        // Username
        card.add(fieldLabel("Username"));
        card.add(Box.createVerticalStrut(4));
        txtUsername = styledField("Enter username");
        card.add(txtUsername);
        card.add(Box.createVerticalStrut(14));

        // Password
        card.add(fieldLabel("Password"));
        card.add(Box.createVerticalStrut(4));
        txtPassword = new JPasswordField();
        styleInput(txtPassword, "Enter password");
        card.add(txtPassword);
        card.add(Box.createVerticalStrut(10));

        // Show password + forgot
        JPanel rowOpts = new JPanel(new BorderLayout());
        rowOpts.setBackground(WHITE);
        chkShow = new JCheckBox("Show password");
        chkShow.setFont(SANS);
        chkShow.setBackground(WHITE);
        chkShow.setForeground(AMBER_900);
        chkShow.addActionListener(e -> txtPassword.setEchoChar(chkShow.isSelected() ? (char)0 : '•'));
        rowOpts.add(chkShow, BorderLayout.WEST);

        btnForgot = new JButton("Forgot password?");
        btnForgot.setBorderPainted(false);
        btnForgot.setContentAreaFilled(false);
        btnForgot.setFont(SANS);
        btnForgot.setForeground(AMBER_700);
        btnForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnForgot.addActionListener(e -> openRecovery());
        rowOpts.add(btnForgot, BorderLayout.EAST);
        card.add(rowOpts);
        card.add(Box.createVerticalStrut(18));

        // Login button
        btnLogin = new JButton("Log In");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setBackground(AMBER_700);
        btnLogin.setForeground(WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> doLogin());
        card.add(btnLogin);

        // Hint
        card.add(Box.createVerticalStrut(14));
        JLabel hint = new JLabel("Default: admin / admin123", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(0xB4, 0x53, 0x09, 160));
        hint.setAlignmentX(CENTER_ALIGNMENT);
        card.add(hint);

        // Wrap card in padded panel
        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setBackground(AMBER_50);
        cardWrap.setBorder(new EmptyBorder(28, 36, 28, 36));
        cardWrap.add(card, BorderLayout.CENTER);

        // Footer
        JLabel footer = new JLabel("© 2026 Library Management System. All rights reserved.", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(AMBER_700);
        footer.setBorder(new EmptyBorder(0, 0, 12, 0));

        root.add(header, BorderLayout.NORTH);
        root.add(cardWrap, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);

        // Enter key triggers login
        getRootPane().setDefaultButton(btnLogin);
    }

    // ── Actions ──────────────────────────────────────────────────────
    private void doLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        AccountDAO dao = new AccountDAO();
        Account acc = dao.authenticate(user, pass);

        if (acc != null) {
            dispose();
            new MainDashboard(acc).setVisible(true);
        } else {
            showError("Invalid credentials or account is inactive.\nPlease try again.");
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }

    private void openRecovery() {
        new PasswordRecoveryForm(this).setVisible(true);
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SANS_BOLD);
        l.setForeground(AMBER_900);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        styleInput(f, placeholder);
        return f;
    }

    private void styleInput(JComponent f, String placeholder) {
        f.setFont(SANS);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFC, 0xD3, 0x4D), 1),
            new EmptyBorder(4, 10, 4, 10)
        ));
        if (f instanceof JTextField) {
            ((JTextField) f).setToolTipText(placeholder);
        }
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Login Failed", JOptionPane.ERROR_MESSAGE);
    }

    // ── Entry Point ──────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}