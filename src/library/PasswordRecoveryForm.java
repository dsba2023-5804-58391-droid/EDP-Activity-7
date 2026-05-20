package library;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * PasswordRecoveryForm — 3-step password recovery:
 *  Step 1: Enter username
 *  Step 2: Answer security question
 *  Step 3: Set new password
 */
public class PasswordRecoveryForm extends JDialog {

    private final AccountDAO dao = new AccountDAO();

    // ── Step panels ──────────────────────────────────────────────────
    private JPanel     pnlStep1, pnlStep2, pnlStep3, pnlSuccess;
    private CardLayout cardLayout;
    private JPanel     cardPanel;

    // ── Step 1 ───────────────────────────────────────────────────────
    private JTextField txtUser1;

    // ── Step 2 ───────────────────────────────────────────────────────
    private JLabel     lblQuestion;
    private JTextField txtAnswer;
    private String     recoveryUsername;

    // ── Step 3 ───────────────────────────────────────────────────────
    private JPasswordField txtNewPass, txtConfPass;

    public PasswordRecoveryForm(JFrame parent) {
        super(parent, "Password Recovery", true);
        setSize(440, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LoginForm.AMBER_50);

        // Header
        JPanel header = new JPanel();
        header.setBackground(LoginForm.AMBER_800);
        header.setBorder(new EmptyBorder(18, 24, 14, 24));
        JLabel title = new JLabel("🔑  Password Recovery", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setForeground(LoginForm.AMBER_50);
        header.add(title);

        // Card layout for steps
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(LoginForm.AMBER_50);

        buildStep1();
        buildStep2();
        buildStep3();
        buildSuccess();

        cardPanel.add(pnlStep1,   "step1");
        cardPanel.add(pnlStep2,   "step2");
        cardPanel.add(pnlStep3,   "step3");
        cardPanel.add(pnlSuccess, "success");

        root.add(header,    BorderLayout.NORTH);
        root.add(cardPanel, BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Step 1: Username ─────────────────────────────────────────────
    private void buildStep1() {
        pnlStep1 = stepPanel("Step 1 of 3 — Enter your username");

        pnlStep1.add(fieldLabel("Username"));
        pnlStep1.add(Box.createVerticalStrut(6));
        txtUser1 = styledField("Enter your username");
        pnlStep1.add(txtUser1);
        pnlStep1.add(Box.createVerticalStrut(20));

        JButton btn = primaryButton("Next →");
        btn.addActionListener(e -> {
            String user = txtUser1.getText().trim();
            if (user.isEmpty()) { err("Please enter your username."); return; }
            String q = dao.getSecurityQuestion(user);
            if (q == null) { err("Username not found."); return; }
            recoveryUsername = user;
            lblQuestion.setText("<html><b>Q:</b> " + q + "</html>");
            txtAnswer.setText("");
            cardLayout.show(cardPanel, "step2");
        });
        pnlStep1.add(btn);
        pnlStep1.add(Box.createVerticalStrut(10));
        pnlStep1.add(cancelBtn());
    }

    // ── Step 2: Security Question ────────────────────────────────────
    private void buildStep2() {
        pnlStep2 = stepPanel("Step 2 of 3 — Answer security question");

        lblQuestion = new JLabel();
        lblQuestion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblQuestion.setForeground(LoginForm.AMBER_900);
        lblQuestion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFD, 0xE6, 0x8A)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        lblQuestion.setBackground(LoginForm.AMBER_100);
        lblQuestion.setOpaque(true);
        lblQuestion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        lblQuestion.setAlignmentX(LEFT_ALIGNMENT);
        pnlStep2.add(lblQuestion);
        pnlStep2.add(Box.createVerticalStrut(14));

        pnlStep2.add(fieldLabel("Your Answer"));
        pnlStep2.add(Box.createVerticalStrut(6));
        txtAnswer = styledField("Type your answer");
        pnlStep2.add(txtAnswer);
        pnlStep2.add(Box.createVerticalStrut(20));

        JButton btn = primaryButton("Verify →");
        btn.addActionListener(e -> {
            String ans = txtAnswer.getText().trim();
            if (ans.isEmpty()) { err("Please enter your answer."); return; }
            if (dao.verifySecurityAnswer(recoveryUsername, ans)) {
                txtNewPass.setText(""); txtConfPass.setText("");
                cardLayout.show(cardPanel, "step3");
            } else {
                err("Incorrect answer. Please try again.");
            }
        });
        pnlStep2.add(btn);
        pnlStep2.add(Box.createVerticalStrut(8));

        JButton back = linkButton("← Back");
        back.addActionListener(e -> cardLayout.show(cardPanel, "step1"));
        pnlStep2.add(back);
    }

    // ── Step 3: New Password ─────────────────────────────────────────
    private void buildStep3() {
        pnlStep3 = stepPanel("Step 3 of 3 — Set new password");

        pnlStep3.add(fieldLabel("New Password"));
        pnlStep3.add(Box.createVerticalStrut(4));
        txtNewPass = new JPasswordField();
        styleInput(txtNewPass);
        pnlStep3.add(txtNewPass);
        pnlStep3.add(Box.createVerticalStrut(12));

        pnlStep3.add(fieldLabel("Confirm New Password"));
        pnlStep3.add(Box.createVerticalStrut(4));
        txtConfPass = new JPasswordField();
        styleInput(txtConfPass);
        pnlStep3.add(txtConfPass);
        pnlStep3.add(Box.createVerticalStrut(20));

        JButton btn = primaryButton("Reset Password");
        btn.addActionListener(e -> {
            String np = new String(txtNewPass.getPassword()).trim();
            String cp = new String(txtConfPass.getPassword()).trim();
            if (np.isEmpty())       { err("New password cannot be empty."); return; }
            if (np.length() < 6)    { err("Password must be at least 6 characters."); return; }
            if (!np.equals(cp))     { err("Passwords do not match."); return; }
            if (dao.updatePassword(recoveryUsername, np)) {
                cardLayout.show(cardPanel, "success");
            } else {
                err("Failed to update password. Please try again.");
            }
        });
        pnlStep3.add(btn);
    }

    // ── Success ──────────────────────────────────────────────────────
    private void buildSuccess() {
        pnlSuccess = new JPanel();
        pnlSuccess.setLayout(new BoxLayout(pnlSuccess, BoxLayout.Y_AXIS));
        pnlSuccess.setBackground(LoginForm.WHITE);
        pnlSuccess.setBorder(new EmptyBorder(32, 36, 28, 36));

        JLabel ico = new JLabel("✅", SwingConstants.CENTER);
        ico.setFont(new Font("SansSerif", Font.PLAIN, 48));
        ico.setAlignmentX(CENTER_ALIGNMENT);

        JLabel msg = new JLabel("<html><center>Password reset successfully!<br>You may now log in with your new password.</center></html>", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msg.setForeground(LoginForm.AMBER_900);
        msg.setAlignmentX(CENTER_ALIGNMENT);

        JButton btn = primaryButton("Return to Login");
        btn.addActionListener(e -> dispose());

        pnlSuccess.add(Box.createVerticalGlue());
        pnlSuccess.add(ico);
        pnlSuccess.add(Box.createVerticalStrut(16));
        pnlSuccess.add(msg);
        pnlSuccess.add(Box.createVerticalStrut(24));
        pnlSuccess.add(btn);
        pnlSuccess.add(Box.createVerticalGlue());
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private JPanel stepPanel(String stepTitle) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(LoginForm.WHITE);
        p.setBorder(new EmptyBorder(22, 32, 20, 32));

        JLabel lbl = new JLabel(stepTitle);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(LoginForm.AMBER_700);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(14));
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(LoginForm.SANS_BOLD);
        l.setForeground(LoginForm.AMBER_900);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String tip) {
        JTextField f = new JTextField();
        styleInput(f);
        f.setToolTipText(tip);
        return f;
    }

    private void styleInput(JComponent f) {
        f.setFont(LoginForm.SANS);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFC, 0xD3, 0x4D)),
            new EmptyBorder(4, 10, 4, 10)
        ));
        f.setAlignmentX(LEFT_ALIGNMENT);
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(LoginForm.SANS_BOLD);
        b.setBackground(LoginForm.AMBER_700);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFont(LoginForm.SANS);
        b.setForeground(LoginForm.AMBER_700);
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton cancelBtn() {
        JButton b = linkButton("Cancel");
        b.addActionListener(e -> dispose());
        return b;
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Recovery Error", JOptionPane.ERROR_MESSAGE);
    }
}
