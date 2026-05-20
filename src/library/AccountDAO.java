package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountDAO - Data Access Object for the accounts table.
 * Uses DatabaseConnection (public class) for all queries.
 */
public class AccountDAO {

    // ── Authentication ──────────────────────────────────────────────
    public Account authenticate(String username, String password) {
        String sql = "SELECT * FROM accounts WHERE username = ? AND password = ? AND status = 'Active'";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Password Recovery ───────────────────────────────────────────
    /** Returns account if username + email match. */
    public Account findByUsernameAndEmail(String username, String email) {
        String sql = "SELECT * FROM accounts WHERE username = ? AND email = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String getSecurityQuestion(String username) {
        String sql = "SELECT security_q FROM accounts WHERE username = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("security_q");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean verifySecurityAnswer(String username, String answer) {
        String sql = "SELECT account_id FROM accounts WHERE username = ? AND LOWER(security_a) = LOWER(?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, answer.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {
        String sql = "UPDATE accounts SET password = ? WHERE username = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── CRUD ────────────────────────────────────────────────────────
    public List<Account> getAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY full_name";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Account> search(String term) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE full_name LIKE ? OR username LIKE ? OR email LIKE ? OR role LIKE ? OR status LIKE ? ORDER BY full_name";
        String like = "%" + term + "%";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean addAccount(Account a) {
        String sql = "INSERT INTO accounts (username,password,full_name,email,phone,role,status,security_q,security_a) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getUsername());
            ps.setString(2, a.getPassword());
            ps.setString(3, a.getFullName());
            ps.setString(4, a.getEmail());
            ps.setString(5, a.getPhone());
            ps.setString(6, a.getRole());
            ps.setString(7, a.getStatus());
            ps.setString(8, a.getSecurityQ());
            ps.setString(9, a.getSecurityA());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAccount(Account a) {
        String sql = "UPDATE accounts SET username=?,full_name=?,email=?,phone=?,role=?,status=?,security_q=?,security_a=? WHERE account_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, a.getUsername());
            ps.setString(2, a.getFullName());
            ps.setString(3, a.getEmail());
            ps.setString(4, a.getPhone());
            ps.setString(5, a.getRole());
            ps.setString(6, a.getStatus());
            ps.setString(7, a.getSecurityQ());
            ps.setString(8, a.getSecurityA());
            ps.setInt(9, a.getAccountId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean toggleStatus(int accountId, String newStatus) {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Helpers ─────────────────────────────────────────────────────
    private Account mapRow(ResultSet rs) throws SQLException {
        return new Account(
            rs.getInt("account_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("role"),
            rs.getString("status"),
            rs.getTimestamp("created_at"),
            rs.getString("security_q"),
            rs.getString("security_a")
        );
    }
}