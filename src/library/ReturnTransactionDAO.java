package library;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ReturnTransactionDAO — handles all DB operations for return_transactions.
 */
public class ReturnTransactionDAO {

    // ── Add return record ────────────────────────────────────────────
    public boolean addReturn(int borrowId, String memberName, String bookTitle,
                              String returnDate, String conditionNote,
                              String processedBy) {
        String sql = "INSERT INTO return_transactions "
                   + "(borrow_id,member_name,book_title,return_date,condition_note,processed_by) "
                   + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, borrowId);
            ps.setString(2, memberName);
            ps.setString(3, bookTitle);
            ps.setString(4, returnDate);
            ps.setString(5, conditionNote);
            ps.setString(6, processedBy);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Get all return records ───────────────────────────────────────
    public List<Object[]> getAll() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT return_id,borrow_id,member_name,book_title,return_date,condition_note,processed_by,created_at FROM return_transactions ORDER BY created_at DESC";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("return_id"),
                    rs.getInt("borrow_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getString("return_date"),
                    rs.getString("condition_note") == null ? "—" : rs.getString("condition_note"),
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
        String sql = "SELECT return_id,borrow_id,member_name,book_title,return_date,condition_note,processed_by FROM return_transactions WHERE return_date BETWEEN ? AND ? ORDER BY return_date";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, from); ps.setString(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("return_id"),
                    rs.getInt("borrow_id"),
                    rs.getString("member_name"),
                    rs.getString("book_title"),
                    rs.getString("return_date"),
                    rs.getString("condition_note") == null ? "—" : rs.getString("condition_note"),
                    rs.getString("processed_by")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Monthly summary for chart ────────────────────────────────────
    public List<Object[]> getMonthlySummary() {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(return_date,'%b %Y') AS month, COUNT(*) AS total "
                   + "FROM return_transactions GROUP BY YEAR(return_date),MONTH(return_date) "
                   + "ORDER BY return_date DESC LIMIT 6";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                list.add(new Object[]{rs.getString("month"), rs.getInt("total")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}

