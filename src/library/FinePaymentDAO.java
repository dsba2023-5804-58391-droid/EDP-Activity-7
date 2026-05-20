package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FinePaymentDAO — handles all DB operations for fine_payments.
 */
public class FinePaymentDAO {

    private static final double FINE_PER_DAY = 5.00; // ₱5 per day overdue

    // ── Auto-generate fines for overdue borrows ──────────────────────
    public void generateOverdueFines(String processedBy) {
        String sql = "SELECT borrow_id, member_name, book_title, due_date "
                   + "FROM borrow_transactions "
                   + "WHERE status='Overdue' AND borrow_id NOT IN (SELECT borrow_id FROM fine_payments)";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int borrowId   = rs.getInt("borrow_id");
                String member  = rs.getString("member_name");
                String book    = rs.getString("book_title");
                Date dueDate   = rs.getDate("due_date");
                long days = (System.currentTimeMillis() - dueDate.getTime()) / (1000*60*60*24);
                if (days < 1) days = 1;
                double fine = days * FINE_PER_DAY;
                addFine(borrowId, member, book, (int)days, fine, processedBy);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean addFine(int borrowId, String memberName, String bookTitle,
                            int daysOverdue, double fineAmount, String processedBy) {
        String sql = "INSERT INTO fine_payments "
                   + "(borrow_id,member_name,book_title,days_overdue,fine_amount,paid_amount,status,processed_by) "
                   + "VALUES (?,?,?,?,?,0.00,'Unpaid',?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            ps.setString(2, memberName);
            ps.setString(3, bookTitle);
            ps.setInt(4, daysOverdue);
            ps.setDouble(5, fineAmount);
            ps.setString(6, processedBy);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Process a payment ────────────────────────────────────────────
    public boolean processPayment(int fineId, double paidAmount) {
        String sql = "UPDATE fine_payments SET paid_amount=?, status='Paid' WHERE fine_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, paidAmount);
            ps.setInt(2, fineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean waiveFine(int fineId) {
        String sql = "UPDATE fine_payments SET status='Waived' WHERE fine_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, fineId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Get all fine records ─────────────────────────────────────────
    public List<Object[]> getAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT fine_id,borrow_id,member_name,book_title,days_overdue,fine_amount,paid_amount,status,processed_by,created_at FROM fine_payments ORDER BY created_at DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("fine_id"),
                    rs.getInt("borrow_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getInt("days_overdue"),
                    String.format("₱%.2f", rs.getDouble("fine_amount")),
                    String.format("₱%.2f", rs.getDouble("paid_amount")),
                    rs.getString("status"),
                    rs.getString("processed_by"),
                    rs.getString("created_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Get by date range ────────────────────────────────────────────
    public List<Object[]> getByDateRange(String from, String to) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT fine_id,member_name,book_title,days_overdue,fine_amount,paid_amount,status,processed_by FROM fine_payments WHERE DATE(created_at) BETWEEN ? AND ? ORDER BY created_at";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("fine_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getInt("days_overdue"),
                    String.format("₱%.2f", rs.getDouble("fine_amount")),
                    String.format("₱%.2f", rs.getDouble("paid_amount")),
                    rs.getString("status"),
                    rs.getString("processed_by")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Summary for pie chart ────────────────────────────────────────
    public int[] getStatusSummary() {
        int paid = 0, unpaid = 0, waived = 0;
        String sql = "SELECT status, COUNT(*) AS cnt FROM fine_payments GROUP BY status";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                switch (rs.getString("status")) {
                    case "Paid":   paid   = rs.getInt("cnt"); break;
                    case "Unpaid": unpaid = rs.getInt("cnt"); break;
                    case "Waived": waived = rs.getInt("cnt"); break;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new int[]{paid, unpaid, waived};
    }
}
