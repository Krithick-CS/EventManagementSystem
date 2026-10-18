import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServer {
    static final Path WEB_DIR = Path.of("web").toAbsolutePath().normalize();
    static final VenueDAO venueDAO = new VenueDAO();
    static final EventDAO eventDAO = new EventDAO();
    static final RegistrationDAO registrationDAO = new RegistrationDAO();
    static final ParticipantDAO participantDAO = new ParticipantDAO();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        server.createContext("/api/venues", WebServer::handleVenues);
        server.createContext("/api/events/status", WebServer::handleEventStatus);
        server.createContext("/api/events", WebServer::handleEvents);
        server.createContext("/api/participants", WebServer::handleParticipants);
        server.createContext("/api/registrations", WebServer::handleRegistrations);
        server.createContext("/", WebServer::handleStatic);
        server.start();
        System.out.println("Server running at http://localhost:8080");
    }

    // ---------- API: /api/venues ----------
    static void handleVenues(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equals("GET")) {
                List<Venue> venues = venueDAO.getAllVenues();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < venues.size(); i++) {
                    Venue v = venues.get(i);
                    if (i > 0) sb.append(",");
                    String loc = v.getLocation() == null ? "" : v.getLocation();
                    sb.append("{\"id\":").append(v.getVenueId())
                      .append(",\"name\":\"").append(esc(v.getName()))
                      .append("\",\"capacity\":").append(v.getCapacity())
                      .append(",\"location\":\"").append(esc(loc)).append("\"}");
                }
                sb.append("]");
                send(ex, 200, "application/json", sb.toString());

            } else if (method.equals("POST")) {
                Map<String, String> form = parseForm(ex);
                String name = form.getOrDefault("name", "").trim();
                String loc = form.getOrDefault("location", "").trim();
                int cap;
                try {
                    cap = Integer.parseInt(form.getOrDefault("capacity", "").trim());
                } catch (NumberFormatException e) {
                    cap = -1;
                }
                if (name.isEmpty()) {
                    error(ex, 400, "Please enter a venue name.");
                } else if (cap <= 0) {
                    error(ex, 400, "Capacity must be a positive number.");
                } else {
                    venueDAO.addVenue(name, cap, loc);
                    send(ex, 201, "application/json", "{\"ok\":true}");
                }
            } else {
                error(ex, 405, "Method not allowed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(ex, 500, "Server error.");
        }
    }

    // ---------- API: /api/events ----------
    static void handleEvents(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equals("GET")) {
                Map<Integer, String> venueNames = new HashMap<>();
                for (Venue v : venueDAO.getAllVenues()) {
                    venueNames.put(v.getVenueId(), v.getName());
                }

                List<Event> events = eventDAO.getAllEvents();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < events.size(); i++) {
                    Event e = events.get(i);
                    if (i > 0) sb.append(",");
                    String desc = e.getDescription() == null ? "" : e.getDescription();
                    sb.append("{\"id\":").append(e.getEventId())
                      .append(",\"title\":\"").append(esc(e.getTitle()))
                      .append("\",\"description\":\"").append(esc(desc))
                      .append("\",\"organizer\":\"").append(esc(e.getOrganizer()))
                      .append("\",\"venueId\":").append(e.getVenueId())
                      .append(",\"venueName\":\"").append(esc(venueNames.getOrDefault(e.getVenueId(), "?")))
                      .append("\",\"start\":\"").append(e.getStartTime())
                      .append("\",\"end\":\"").append(e.getEndTime())
                      .append("\",\"capacity\":").append(e.getMaxCapacity())
                      .append(",\"registered\":").append(registrationDAO.countRegistrations(e.getEventId()))
                      .append(",\"status\":\"").append(esc(e.getStatus())).append("\"}");
                }
                sb.append("]");
                send(ex, 200, "application/json", sb.toString());

            } else if (method.equals("POST")) {
                Map<String, String> f = parseForm(ex);
                String title = f.getOrDefault("title", "").trim();
                String organizer = f.getOrDefault("organizer", "").trim();
                String description = f.getOrDefault("description", "").trim();

                if (title.isEmpty()) { error(ex, 400, "Please enter a title."); return; }
                if (organizer.isEmpty()) { error(ex, 400, "Please enter the organizer."); return; }

                int venueId, capacity;
                try {
                    venueId = Integer.parseInt(f.getOrDefault("venueId", "").trim());
                } catch (NumberFormatException e) {
                    error(ex, 400, "Please choose a venue.");
                    return;
                }
                try {
                    capacity = Integer.parseInt(f.getOrDefault("capacity", "").trim());
                    if (capacity <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    error(ex, 400, "Capacity must be a positive number.");
                    return;
                }

                LocalDateTime start, end;
                try {
                    start = LocalDateTime.parse(f.getOrDefault("start", "").trim());
                    end = LocalDateTime.parse(f.getOrDefault("end", "").trim());
                } catch (DateTimeParseException e) {
                    error(ex, 400, "Please enter valid start and end times.");
                    return;
                }

                String problem = eventDAO.tryAddEvent(title, description, organizer,
                        venueId, start, end, capacity);
                if (problem == null) {
                    send(ex, 201, "application/json", "{\"ok\":true}");
                } else {
                    error(ex, 409, problem);
                }
            } else {
                error(ex, 405, "Method not allowed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(ex, 500, "Server error.");
        }
    }

    // ---------- API: /api/events/status ----------
    static void handleEventStatus(HttpExchange ex) throws IOException {
        try {
            if (!ex.getRequestMethod().equals("POST")) {
                error(ex, 405, "Method not allowed.");
                return;
            }
            Map<String, String> f = parseForm(ex);
            int id;
            try {
                id = Integer.parseInt(f.getOrDefault("id", "").trim());
            } catch (NumberFormatException e) {
                error(ex, 400, "Invalid event id.");
                return;
            }
            String status = f.getOrDefault("status", "");
            if (!status.equals("APPROVED") && !status.equals("CANCELLED")) {
                error(ex, 400, "Invalid status.");
                return;
            }

            Event found = null;
            for (Event e : eventDAO.getAllEvents()) {
                if (e.getEventId() == id) { found = e; break; }
            }
            if (found == null) { error(ex, 404, "Event not found."); return; }

            String current = found.getStatus();
            if (status.equals("APPROVED") && !"DRAFT".equals(current)) {
                error(ex, 409, "Only draft events can be approved.");
                return;
            }
            if (status.equals("CANCELLED")
                    && ("CANCELLED".equals(current) || "COMPLETED".equals(current))) {
                error(ex, 409, "This event is already " + current.toLowerCase() + ".");
                return;
            }

            eventDAO.updateStatus(id, status);
            send(ex, 200, "application/json", "{\"ok\":true}");
        } catch (Exception e) {
            e.printStackTrace();
            error(ex, 500, "Server error.");
        }
    }

        // ---------- API: /api/participants ----------
    static void handleParticipants(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equals("GET")) {
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (String[] p : participantDAO.getAllParticipants()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"id\":").append(p[0])
                      .append(",\"name\":\"").append(esc(p[1]))
                      .append("\",\"email\":\"").append(esc(p[2])).append("\"}");
                }
                sb.append("]");
                send(ex, 200, "application/json", sb.toString());

            } else if (method.equals("POST")) {
                Map<String, String> f = parseForm(ex);
                String name = f.getOrDefault("name", "").trim();
                String email = f.getOrDefault("email", "").trim();
                if (name.isEmpty()) {
                    error(ex, 400, "Please enter a name.");
                } else if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                    error(ex, 400, "Please enter a valid email address.");
                } else {
                    String problem = participantDAO.tryAddParticipant(name, email);
                    if (problem == null) {
                        send(ex, 201, "application/json", "{\"ok\":true}");
                    } else {
                        error(ex, 409, problem);
                    }
                }
            } else {
                error(ex, 405, "Method not allowed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(ex, 500, "Server error.");
        }
    }

    // ---------- API: /api/registrations ----------
    static void handleRegistrations(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            if (method.equals("GET")) {
                int eventId = -1;
                String query = ex.getRequestURI().getQuery();
                if (query != null) {
                    for (String pair : query.split("&")) {
                        String[] kv = pair.split("=", 2);
                        if (kv[0].equals("eventId") && kv.length == 2) {
                            try {
                                eventId = Integer.parseInt(kv[1].trim());
                            } catch (NumberFormatException ignored) { }
                        }
                    }
                }
                if (eventId < 0) {
                    error(ex, 400, "Please choose an event.");
                    return;
                }
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (String[] r : registrationDAO.getRegistrationsForEvent(eventId)) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{\"id\":").append(r[0])
                      .append(",\"name\":\"").append(esc(r[1]))
                      .append("\",\"email\":\"").append(esc(r[2]))
                      .append("\",\"registeredAt\":\"").append(esc(r[3])).append("\"}");
                }
                sb.append("]");
                send(ex, 200, "application/json", sb.toString());

            } else if (method.equals("POST")) {
                Map<String, String> f = parseForm(ex);
                int eventId, participantId;
                try {
                    eventId = Integer.parseInt(f.getOrDefault("eventId", "").trim());
                    participantId = Integer.parseInt(f.getOrDefault("participantId", "").trim());
                } catch (NumberFormatException e) {
                    error(ex, 400, "Please choose an event and a participant.");
                    return;
                }
                String problem = registrationDAO.tryRegister(eventId, participantId);
                if (problem == null) {
                    send(ex, 201, "application/json", "{\"ok\":true}");
                } else {
                    error(ex, 409, problem);
                }
            } else {
                error(ex, 405, "Method not allowed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            error(ex, 500, "Server error.");
        }
    }

    // ---------- Static files from the web folder ----------
    static void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        Path file = WEB_DIR.resolve(path.substring(1)).normalize();
        if (!file.startsWith(WEB_DIR) || !Files.isRegularFile(file)) {
            send(ex, 404, "text/plain", "Not found");
            return;
        }

        String name = file.getFileName().toString();
        String type = name.endsWith(".html") ? "text/html; charset=utf-8"
                    : name.endsWith(".css")  ? "text/css; charset=utf-8"
                    : name.endsWith(".js")   ? "text/javascript; charset=utf-8"
                    : "application/octet-stream";

        byte[] bytes = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ---------- Helpers ----------
    static void error(HttpExchange ex, int code, String message) throws IOException {
        send(ex, code, "application/json", "{\"error\":\"" + esc(message) + "\"}");
    }

    static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    static Map<String, String> parseForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        if (body.isEmpty()) return map;
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}