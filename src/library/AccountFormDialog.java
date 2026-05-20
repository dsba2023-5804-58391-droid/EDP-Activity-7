package library;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * AccountFormDialog — Add or Edit an account.
 */
public class AccountFormDialog extends JDialog {

    private final AccountDAO dao = new AccountDAO();
    private Account account;        // null = add mode
    private boolean saved = false;

    // ── Fields ───────────────────────────────────────────────────────
    private JTextField     txtUsername, txtFullName, txtEmail, txtPhone;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole, cmbStatus;
    private JTextField     txtSecQ, txtSecA;

    public AccountFormDialog(JFrame parent, Account existing) {
        super(parent, existing == null ? "Add New Account" : "Edit Account", true);
        this.account = existing;
        setSize(480, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        buildUI();
        if (existing != null) populate();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LoginForm.WHITE);

        // Header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hdr.setBackground(LoginForm.AMBER_800);
        hdr.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel((account == null ? "➕  Add New Account" : "✏  Edit Account"));
        title.setFont(new Font("Serif", Font.BOLD, 16));
        title.setForeground(LoginForm.AMBER_50);
        hdr.add(title);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(LoginForm.WHITE);
        form.setBorder(new EmptyBorder(20, 24, 16, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        int row = 0;

        txtFullName = addField(form, gbc, row++, "Full Name *", new JTextField());
        txtUsername = addField(form, gbc, row++, "Username *",  new JTextField());
        txtPassword = (JPasswordField) addFieldComp(form, gbc, row++, "Password *",
                account == null ? "Required for new account" : "Leave blank to keep current",
                new JPasswordField());
        txtEmail    = addField(form, gbc, row++, "Email *",     new JTextField());
        txtPhone    = addField(form, gbc, row++, "Phone",       new JTextField());

        // Role
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0.3;
        form.add(lbl("Role"), gbc);
        gbc.gridx=1; gbc.gridwidth=2; gbc.weightx=0.7;
        cmbRole = new JComboBox<>(new String[]{"Admin","Librarian","Staff"});
        styleCombo(cmbRole);
        form.add(cmbRole, gbc);
        row++;

        // Status
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0.3;
        form.add(lbl("Status"), gbc);
        gbc.gridx=1; gbc.gridwidth=2; gbc.weightx=0.7;
        cmbStatus = new JComboBox<>(new String[]{"Active","Inactive"});
        styleCombo(cmbStatus);
        form.add(cmbStatus, gbc);
        row++;

        // Security question
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=3; gbc.weightx=1;
        JLabel secHdr = new JLabel("─── Password Recovery ───────────────────────");
        secHdr.setFont(new Font("SansSerif", Font.PLAIN, 11));
        secHdr.setForeground(LoginForm.AMBER_700);
        form.add(secHdr, gbc); row++;

        txtSecQ = addField(form, gbc, row++, "Security Question", new JTextField());
        txtSecQ.setToolTipText("e.g. What is your pet's name?");
        txtSecA = addField(form, gbc, row++, "Answer",            new JTextField());

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setBackground(LoginForm.WHITE);
        btnRow.setBorder(new EmptyBorder(10, 20, 16, 20));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(LoginForm.SANS_BOLD);
        btnCancel.setForeground(LoginForm.AMBER_800);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton("💾  Save Account");
        btnSave.setFont(LoginForm.SANS_BOLD);
        btnSave.setBackground(LoginForm.AMBER_700);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setOpaque(true);
        btnSave.addActionListener(e -> doSave());

        btnRow.add(btnCancel);
        btnRow.add(btnSave);

        root.add(hdr,    BorderLayout.NORTH);
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(btnRow, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void populate() {
        txtFullName.setText(account.getFullName());
        txtUsername.setText(account.getUsername());
        txtEmail.setText(account.getEmail());
        txtPhone.setText(account.getPhone() == null ? "" : account.getPhone());
        cmbRole.setSelectedItem(account.getRole());
        cmbStatus.setSelectedItem(account.getStatus());
        txtSecQ.setText(account.getSecurityQ() == null ? "" : account.getSecurityQ());
        txtSecA.setText(account.getSecurityA() == null ? "" : account.getSecurityA());
        // password blank = keep current
    }

    private void doSave() {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String email    = txtEmail.getText().trim();
        String phone    = txtPhone.getText().trim();
        String role     = (String) cmbRole.getSelectedItem();
        String status   = (String) cmbStatus.getSelectedItem();
        String secQ     = txtSecQ.getText().trim();
        String secA     = txtSecA.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full Name, Username, and Email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (account == null && password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password is required for new accounts.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (account == null) {
            // Add mode
            Account a = new Account();
            a.setFullName(fullName); a.setUsername(username); a.setPassword(password);
            a.setEmail(email); a.setPhone(phone); a.setRole(role); a.setStatus(status);
            a.setSecurityQ(secQ); a.setSecurityA(secA);
            if (dao.addAccount(a)) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Account added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add account.\nUsername or email may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Edit mode
            account.setFullName(fullName); account.setUsername(username);
            if (!password.isEmpty()) account.setPassword(password);
            account.setEmail(email); account.setPhone(phone);
            account.setRole(role); account.setStatus(status);
            account.setSecurityQ(secQ); account.setSecurityA(secA);
            if (dao.updateAccount(account)) {
                saved = true;
                JOptionPane.showMessageDialog(this, "Account updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSaved() { return saved; }

    // ── Helpers ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private <T extends JComponent> T addField(JPanel p, GridBagConstraints gbc, int row, String label, T field) {
        return (T) addFieldComp(p, gbc, row, label, null, field);
    }

    private JComponent addFieldComp(JPanel p, GridBagConstraints gbc, int row, String label, String tip, JComponent field) {
        gbc.gridx=0; gbc.gridy=row; gbc.gridwidth=1; gbc.weightx=0.3;
        p.add(lbl(label), gbc);
        gbc.gridx=1; gbc.gridwidth=2; gbc.weightx=0.7;
        styleField(field);
        if (tip != null) field.setToolTipText(tip);
        p.add(field, gbc);
        return field;
    }

    private void styleField(JComponent f) {
        f.setFont(LoginForm.SANS);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xFC, 0xD3, 0x4D)),
            new EmptyBorder(4, 8, 4, 8)
        ));
        f.setPreferredSize(new Dimension(200, 32));
    }

    private void styleCombo(JComboBox<String> c) {
        c.setFont(LoginForm.SANS);
        c.setPreferredSize(new Dimension(200, 32));
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text + ":");
        l.setFont(LoginForm.SANS_BOLD);
        l.setForeground(LoginForm.AMBER_900);
        return l;
    }
}
