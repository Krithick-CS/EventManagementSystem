import java.sql.*;

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
}