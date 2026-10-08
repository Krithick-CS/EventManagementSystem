import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static VenueDAO venueDAO = new VenueDAO();
    static EventDAO eventDAO = new EventDAO();
    static ParticipantDAO participantDAO = new ParticipantDAO();
    static RegistrationDAO registrationDAO = new RegistrationDAO();
    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== Campus Event Management =====");
            System.out.println("1. View venues");
            System.out.println("2. View events");
            System.out.println("3. Create event");
            System.out.println("4. Approve event");
            System.out.println("5. Add participant");
            System.out.println("6. Register participant for event");
            System.out.println("7. Show registration count");
            System.out.println("0. Exit");

            int choice = readInt("Choose: ");
            switch (choice) {
                case 1 -> venueDAO.getAllVenues().forEach(System.out::println);
                case 2 -> eventDAO.getAllEvents().forEach(System.out::println);
                case 3 -> createEvent();
                case 4 -> {
                    int id = readInt("Event ID to approve: ");
                    System.out.println(eventDAO.updateStatus(id, "APPROVED")
                        ? "Event approved." : "Event not found.");
                }
                case 5 -> {
                    System.out.print("Name: ");
                    String name = sc.nextLine();
                    System.out.print("Email: ");
                    String email = sc.nextLine();
                    participantDAO.addParticipant(name, email);
                }
                case 6 -> {
                    int eventId = readInt("Event ID: ");
                    int participantId = readInt("Participant ID: ");
                    registrationDAO.register(eventId, participantId);
                }
                case 7 -> {
                    int eventId = readInt("Event ID: ");
                    System.out.println("Registered: " + registrationDAO.countRegistrations(eventId));
                }
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    static void createEvent() {
        System.out.print("Title: ");
        String title = sc.nextLine();
        System.out.print("Description: ");
        String desc = sc.nextLine();
        System.out.print("Organizer: ");
        String organizer = sc.nextLine();
        int venueId = readInt("Venue ID: ");
        LocalDateTime start = readDateTime("Start (yyyy-MM-dd HH:mm): ");
        LocalDateTime end = readDateTime("End   (yyyy-MM-dd HH:mm): ");
        int capacity = readInt("Max capacity: ");
        eventDAO.addEvent(title, desc, organizer, venueId, start, end, capacity);
    }

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    static LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDateTime.parse(sc.nextLine().trim(), FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Use the format 2026-11-10 14:30");
            }
        }
    }
}