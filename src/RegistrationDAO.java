import java.sql.*;

public class RegistrationDAO {

    private int getMaxCapacity(int eventId) {
        String sql = "SELECT max_capacity FROM events WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("max_capacity");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // event not found
    }

    public int countRegistrations(int eventId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean register(int eventId, int participantId) {
        int max = getMaxCapacity(eventId);
        if (max == -1) {
            System.out.println("Rejected: event not found.");
            return false;
        }
        if (!"APPROVED".equals(getStatus(eventId))) {
            System.out.println("Rejected: event is not approved yet.");
            return false;
        }
        if (countRegistrations(eventId) >= max) {
            System.out.println("Rejected: event is full.");
            return false;
        }

        String sql = "INSERT INTO registrations (event_id, participant_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, participantId);
            ps.executeUpdate();
            System.out.println("Registered successfully.");
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("Rejected: participant already registered.");
            } else if (e.getErrorCode() == 1452) {
                System.out.println("Rejected: participant not found.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    private String getStatus(int eventId) {
    String sql = "SELECT status FROM events WHERE event_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, eventId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("status");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
    }
}