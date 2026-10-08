import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenueDAO {

    public void addVenue(String name, int capacity, String location) {
        String sql = "INSERT INTO venues (name, capacity, location) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, capacity);
            ps.setString(3, location);
            ps.executeUpdate();
            System.out.println("Venue added.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Venue> getAllVenues() {
        List<Venue> venues = new ArrayList<>();
        String sql = "SELECT * FROM venues";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                venues.add(new Venue(
                    rs.getInt("venue_id"),
                    rs.getString("name"),
                    rs.getInt("capacity"),
                    rs.getString("location")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }
}