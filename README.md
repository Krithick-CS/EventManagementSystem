# Event Management System

A **Java-based Event Management System** designed to manage campus events, venues, participants, and event registrations using **MySQL** as the database.

The application is a console-based Java project that uses **JDBC** for database connectivity and follows a simple **DAO (Data Access Object)** structure for database operations.

---

## Features

### Event Management
- View all events
- Create new events
- Approve events
- Store event title, description, organizer, venue, start time, end time, capacity, and status
- Prevent scheduling conflicts for the same venue
- Ensure an event's end time is after its start time

### Venue Management
- View available venues
- Store venue name, capacity, and location
- Associate events with venues

### Participant Management
- Add participants
- Store participant name and email
- Prevent duplicate participant email addresses

### Event Registration
- Register participants for approved events
- Prevent duplicate registrations
- Prevent registration when an event is full
- Validate that the participant exists
- Display the total number of registrations for an event

### Validation
The system includes checks for:
- Invalid date/time input
- Invalid numeric input
- Venue scheduling conflicts
- Event approval status
- Maximum event capacity
- Duplicate participants
- Duplicate registrations
- Missing participants/events

---

## Technologies Used

- **Java**
- **JDBC**
- **MySQL**
- **MySQL Connector/J**
- **Visual Studio Code**
- **DAO (Data Access Object) pattern**

---

## Project Structure

```text
EventManagementSystem/
│
├── src/
│   ├── App.java
│   ├── Main.java
│   │
│   ├── DBConnection.java
│   ├── DBConnection.java.example
│   │
│   ├── Event.java
│   ├── EventDAO.java
│   │
│   ├── Venue.java
│   ├── VenueDAO.java
│   │
│   ├── ParticipantDAO.java
│   ├── RegistrationDAO.java
│   │
│   ├── TestConnection.java
│   └── TestRegistration.java
│
├── lib/
│   └── mysql-connector-j-26.7.0.jar
│
├── bin/
│   └── Compiled Java classes
│
└── .vscode/
    └── settings.json
```

---

## Architecture

The application separates database operations from the application logic using DAO classes.

```text
              ┌──────────────────────┐
              │       Main.java      │
              │  Console Application │
              └──────────┬───────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
          ▼              ▼              ▼
    ┌──────────┐   ┌───────────┐  ┌───────────────┐
    │ EventDAO │   │ VenueDAO  │  │ ParticipantDAO│
    └────┬─────┘   └─────┬─────┘  └───────┬───────┘
         │               │                │
         └───────────────┼────────────────┘
                         ▼
                ┌────────────────┐
                │ DBConnection   │
                │     JDBC       │
                └───────┬────────┘
                        ▼
                ┌────────────────┐
                │     MySQL      │
                │ event_management│
                └────────────────┘
```

### Main Components

| Component | Responsibility |
|---|---|
| `Main.java` | Console menu and user interaction |
| `DBConnection.java` | Creates MySQL database connections |
| `Event.java` | Event model |
| `EventDAO.java` | Event database operations |
| `Venue.java` | Venue model |
| `VenueDAO.java` | Venue database operations |
| `ParticipantDAO.java` | Participant database operations |
| `RegistrationDAO.java` | Event registration operations |
| `TestConnection.java` | Tests MySQL connectivity |
| `TestRegistration.java` | Tests registration scenarios |
| `App.java` | Basic Java application entry-point example |

---

## Database

The application connects to a MySQL database named:

```text
event_management
```

The source code expects the following database entities.

### `events`

The event-related code expects fields similar to:

```text
event_id
title
description
organizer
venue_id
start_time
end_time
max_capacity
status
```

### `venues`

The venue-related code expects:

```text
venue_id
name
capacity
location
```

### `participants`

The participant-related code expects:

```text
participant_id
name
email
```

### `registrations`

The registration-related code expects:

```text
event_id
participant_id
```

The application also relies on database constraints for duplicate registrations and duplicate participant emails.

> **Note:** A database schema/SQL file is not currently included in this repository. Before running the application, make sure the required database and tables exist.
---

## Application Menu

When the application starts, it displays:

```text
===== Campus Event Management =====

1. View venues
2. View events
3. Create event
4. Approve event
5. Add participant
6. Register participant for event
7. Show registration count
0. Exit
```

### Example Workflow

A typical workflow is:

```text
1. View venues
        ↓
2. Create an event
        ↓
3. Approve the event
        ↓
4. Add participants
        ↓
5. Register participants
        ↓
6. View registration count
```

---

## Event Validation

When creating an event, the system checks that:

### End time is after start time

```text
start_time < end_time
```

If this condition is not satisfied, the event is rejected.

### Venue availability

The application checks for overlapping events at the same venue.

Cancelled events are excluded from the venue conflict check.

---

## Registration Validation

A participant can register only when:

1. The event exists.
2. The event has been approved.
3. The event has not reached its maximum capacity.
4. The participant exists.
5. The participant has not already registered for the event.

For example:

```text
Event Capacity: 2

Participant 1 → SUCCESS
Participant 2 → SUCCESS
Participant 3 → REJECTED (Event Full)
```

---

## Testing

### Database Connection Test

Use:

```text
TestConnection.java
```

to verify that the application can connect to MySQL.

### Registration Test

Use:

```text
TestRegistration.java
```

to test scenarios such as:

- Successful registration
- Duplicate registration
- Event capacity limits
- Non-existent events

---

## Important Security Note

Before making the repository public, make sure you **remove real database credentials from the source code**.

For example, avoid committing:

```java
private static final String PASSWORD = "your-real-password";
```

Instead, keep credentials local and use:

```text
DBConnection.java.example
```

as a template.

If a real password has already been pushed to GitHub, change/rotate that database password.

---

## Future Improvements

Possible improvements for the project include:

- Add a complete SQL database initialization script
- Move database credentials to environment variables
- Add event cancellation functionality
- Add participant listing
- Add event search/filtering
- Add venue creation through the main menu
- Add stronger input validation
- Add unit tests
- Add a graphical user interface or web interface
- Add role-based access for organizers/admins
- Add transaction handling for registration
- Add logging instead of printing stack traces
- Add Maven or Gradle for dependency management

---
