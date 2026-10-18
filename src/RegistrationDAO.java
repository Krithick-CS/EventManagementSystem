import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public String tryRegister(int eventId, int participantId) {
    String status = null;
    int max = 0;
    String sql = "SELECT status, max_capacity FROM events WHERE event_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, eventId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                status = rs.getString("status");
                max = rs.getInt("max_capacity");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return "Database error.";
    }

    if (status == null) return "Event not found.";
    if (!"APPROVED".equals(status)) return "Registration is only open for approved events.";
    if (countRegistrations(eventId) >= max) return "This event is full.";

    String insert = "INSERT INTO registrations (event_id, participant_id) VALUES (?, ?)";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(insert)) {
        ps.setInt(1, eventId);
        ps.setInt(2, participantId);
        ps.executeUpdate();
        return null;
    } catch (SQLException e) {
        if (e.getErrorCode() == 1062) return "That participant is already registered for this event.";
        if (e.getErrorCode() == 1452) return "Participant not found.";
        e.printStackTrace();
        return "Database error.";
    }
}

public List<String[]> getRegistrationsForEvent(int eventId) {
    List<String[]> list = new ArrayList<>();
    String sql = "SELECT p.participant_id, p.name, p.email, r.registered_at "
               + "FROM registrations r JOIN participants p "
               + "ON r.participant_id = p.participant_id "
               + "WHERE r.event_id = ? ORDER BY r.registered_at";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, eventId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("participant_id")),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getTimestamp("registered_at").toString()});
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
}