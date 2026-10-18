import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ParticipantDAO {

    public boolean addParticipant(String name, String email) {
        String sql = "INSERT INTO participants (name, email) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    System.out.println("Participant added. Their ID is " + keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("Rejected: that email is already registered.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public List<String[]> getAllParticipants() {
    List<String[]> list = new ArrayList<>();
    String sql = "SELECT participant_id, name, email FROM participants ORDER BY participant_id";
    try (Connection conn = DBConnection.getConnection();
         Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) {
            list.add(new String[]{
                String.valueOf(rs.getInt("participant_id")),
                rs.getString("name"),
                rs.getString("email")});
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}

public String tryAddParticipant(String name, String email) {
    String sql = "INSERT INTO participants (name, email) VALUES (?, ?)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, name);
        ps.setString(2, email);
        ps.executeUpdate();
        return null;
    } catch (SQLException e) {
        if (e.getErrorCode() == 1062) return "That email is already registered.";
        e.printStackTrace();
        return "Database error.";
    }
}
}