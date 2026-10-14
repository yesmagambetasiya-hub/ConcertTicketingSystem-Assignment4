
package org.example.finalization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FinalizationRepository {

    private final Path filePath;

    public FinalizationRepository() {
        this(Path.of("data", "finalizations.csv"));
    }

    public FinalizationRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }
        this.filePath = filePath;
    }

    public void save(FinalizationRecord record) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "Finalization record cannot be null"
            );
        }

        String ticketId = record.getTicketId();
        validateTicketId(ticketId);

        try {
            Path parent = filePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            String line = ticketId + ","
                    + record.getStatus()
                    + System.lineSeparator();

            Files.writeString(
                    filePath,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save finalization", e
            );
        }
    }

    public FinalizationRecord findByTicketId(String ticketId) {
        validateTicketId(ticketId);

        if (!Files.exists(filePath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath,
                    StandardCharsets.UTF_8
            );

            FinalizationStatus latestStatus = null;

            for (String line : lines) {
                String[] parts = line.split(",", -1);

                if (parts.length != 2) {
                    throw new IllegalStateException(
                            "Invalid finalization record"
                    );
                }

                if (parts[0].equals(ticketId)) {
                    latestStatus =
                            FinalizationStatus.valueOf(parts[1]);
                }
            }

            if (latestStatus == null) {
                return null;
            }

            FinalizationRecord record =
                    new FinalizationRecord(ticketId);

            if (latestStatus == FinalizationStatus.APPROVED) {
                record.approve();
            } else if (latestStatus == FinalizationStatus.REFUNDED) {
                record.refund();
            }

            return record;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load finalization", e
            );
        }
    }

    private void validateTicketId(String ticketId) {
        if (ticketId == null
                || ticketId.isBlank()
                || ticketId.contains(",")
                || ticketId.contains("\n")
                || ticketId.contains("\r")) {

            throw new IllegalArgumentException(
                    "Invalid ticket ID"
            );
        }
    }
}
