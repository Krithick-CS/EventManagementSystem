public class Venue {
    private int venueId;
    private String name;
    private int capacity;
    private String location;

    public Venue(int venueId, String name, int capacity, String location) {
        this.venueId = venueId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
    }

    public int getVenueId() { return venueId; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public String getLocation() { return location; }

    @Override
    public String toString() {
        return venueId + " | " + name + " | capacity: " + capacity + " | " + location;
    }
}