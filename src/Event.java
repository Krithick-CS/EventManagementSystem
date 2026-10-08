import java.time.LocalDateTime;

public class Event {
    private int eventId;
    private String title;
    private String description;
    private String organizer;
    private int venueId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxCapacity;
    private String status;

    public Event(int eventId, String title, String description, String organizer,
                 int venueId, LocalDateTime startTime, LocalDateTime endTime,
                 int maxCapacity, String status) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.organizer = organizer;
        this.venueId = venueId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxCapacity = maxCapacity;
        this.status = status;
    }

    public int getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOrganizer() { return organizer; }
    public int getVenueId() { return venueId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public int getMaxCapacity() { return maxCapacity; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return eventId + " | " + title + " | by " + organizer + " | venue " + venueId
             + " | " + startTime + " to " + endTime + " | " + status;
    }
}