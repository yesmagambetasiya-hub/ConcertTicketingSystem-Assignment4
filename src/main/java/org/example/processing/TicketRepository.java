
package org.example.processing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TicketRepository {

    private final Path filePath;

    public TicketRepository() {
        this(Path.of("data", "tickets.csv"));
    }

    public TicketRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }
        this.filePath = filePath;
    }

    public void save(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket cannot be null"
            );
        }

        validateField(ticket.getTicketId());
        validateField(ticket.getSeatNumber());

        try {
            Path parent = filePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            String line = ticket.getTicketId() + ","
                    + ticket.getSeatNumber() + ","
                    + ticket.getStatus() + System.lineSeparator();

            Files.writeString(
                    filePath,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save ticket", e
            );
        }
    }

    public Ticket findById(String ticketId) {
        validateField(ticketId);

        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath,
                    StandardCharsets.UTF_8
            );

            Ticket latestTicket = null;

            for (String line : lines) {
                String[] parts = line.split(",", -1);

                if (parts.length != 3) {
                    throw new IllegalStateException(
                            "Invalid ticket record"
                    );
                }

                if (parts[0].equals(ticketId)) {
                    Ticket ticket = new Ticket(
                            parts[0],
                            parts[1]
                    );

                    TicketStatus status =
                            TicketStatus.valueOf(parts[2]);

                    if (status == TicketStatus.PRINTED) {
                        ticket.printTicket();
                    } else if (status == TicketStatus.CANCELLED) {
                        ticket.cancelTicket();
                    }

                    latestTicket = ticket;
                }
            }

            return latestTicket;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load ticket", e
            );
        }
    }

    private void validateField(String value) {
        if (value == null || value.isBlank()
                || value.contains(",")
                || value.contains("\n")
                || value.contains("\r")) {
            throw new IllegalArgumentException(
                    "Invalid ticket data"
            );
        }
    }
}

