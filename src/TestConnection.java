import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/event_management";
        String user = "root";
        String password = "Password@123";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}