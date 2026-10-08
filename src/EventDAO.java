import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public boolean addEvent(String title, String description, String organizer,
                            int venueId, LocalDateTime start, LocalDateTime end,
                            int maxCapacity) {

        if (!end.isAfter(start)) {
            System.out.println("Rejected: end time must be after start time.");
            return false;
        }
        if (hasConflict(venueId, start, end)) {
            System.out.println("Rejected: venue is already booked for that time.");
            return false;
        }

        String sql = "INSERT INTO events (title, description, organizer, venue_id, "
                   + "start_time, end_time, max_capacity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setString(3, organizer);
            ps.setInt(4, venueId);
            ps.setTimestamp(5, Timestamp.valueOf(start));
            ps.setTimestamp(6, Timestamp.valueOf(end));
            ps.setInt(7, maxCapacity);
            ps.executeUpdate();
            System.out.println("Event added.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                events.add(new Event(
                    rs.getInt("event_id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("organizer"),
                    rs.getInt("venue_id"),
                    rs.getTimestamp("start_time").toLocalDateTime(),
                    rs.getTimestamp("end_time").toLocalDateTime(),
                    rs.getInt("max_capacity"),
                    rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    public boolean updateStatus(int eventId, String status) {
        String sql = "UPDATE events SET status = ? WHERE event_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasConflict(int venueId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT COUNT(*) FROM events "
                   + "WHERE venue_id = ? "
                   + "AND status <> 'CANCELLED' "
                   + "AND start_time < ? AND end_time > ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, venueId);
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // if something goes wrong, play safe and block the booking
        }
    }
}