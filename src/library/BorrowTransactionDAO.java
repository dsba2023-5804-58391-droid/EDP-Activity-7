package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BorrowTransactionDAO — handles all DB operations for borrow_transactions.
 */
public class BorrowTransactionDAO {

    // ── Add new borrow record ────────────────────────────────────────
    public boolean addBorrow(String memberName, String bookTitle,
                              String borrowDate, String dueDate,
                              String processedBy) {
        String sql = "INSERT INTO borrow_transactions "
                   + "(member_name,book_title,borrow_date,due_date,status,processed_by) "
                   + "VALUES (?,?,?,?,'Borrowed',?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, memberName);
            ps.setString(2, bookTitle);
            ps.setString(3, borrowDate);
            ps.setString(4, dueDate);
            ps.setString(5, processedBy);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Get all records ──────────────────────────────────────────────
    public List<Object[]> getAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT borrow_id,member_name,book_title,borrow_date,due_date,return_date,status,processed_by,created_at FROM borrow_transactions ORDER BY created_at DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("borrow_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date") == null ? "—" : rs.getString("return_date"),
                    rs.getString("status"),
                    rs.getString("processed_by"),
                    rs.getString("created_at")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Get by date range (for reports) ─────────────────────────────
    public List<Object[]> getByDateRange(String from, String to) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT borrow_id,member_name,book_title,borrow_date,due_date,return_date,status,processed_by FROM borrow_transactions WHERE borrow_date BETWEEN ? AND ? ORDER BY borrow_date";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, from);
            ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("borrow_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date"),
                    rs.getString("return_date") == null ? "—" : rs.getString("return_date"),
                    rs.getString("status"),
                    rs.getString("processed_by")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Get only Borrowed/Overdue (for Return form) ──────────────────
    public List<Object[]> getUnreturned() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT borrow_id,member_name,book_title,borrow_date,due_date FROM borrow_transactions WHERE status IN ('Borrowed','Overdue') ORDER BY due_date";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("borrow_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getString("borrow_date"),
                    rs.getString("due_date")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Mark as returned ────────────────────────────────────────────
    public boolean markReturned(int borrowId, String returnDate) {
        String sql = "UPDATE borrow_transactions SET return_date=?, status='Returned' WHERE borrow_id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, returnDate);
            ps.setInt(2, borrowId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Monthly summary for chart (last 6 months) ───────────────────
    public List<Object[]> getMonthlySummary() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(borrow_date,'%b %Y') AS month, COUNT(*) AS total "
                   + "FROM borrow_transactions GROUP BY YEAR(borrow_date),MONTH(borrow_date) "
                   + "ORDER BY borrow_date DESC LIMIT 6";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new Object[]{rs.getString("month"), rs.getInt("total")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}

