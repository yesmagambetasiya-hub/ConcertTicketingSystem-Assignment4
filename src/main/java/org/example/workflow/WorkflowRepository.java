
package org.example.workflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkflowRepository {

    private final Path filePath;

    public WorkflowRepository() {
        this(Path.of("data", "workflows.csv"));
    }

    public WorkflowRepository(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException(
                    "File path cannot be null"
            );
        }

        this.filePath = filePath;
    }

    // Save workflow state
    public void save(WorkflowRecord record) {

        if (record == null) {
            throw new IllegalArgumentException(
                    "Workflow record cannot be null"
            );
        }

        validateField(record.getTicketId());
        validateField(record.getSeatNumber());

        try {
            Path parent = filePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            String line = record.getTicketId() + ","
                    + record.getSeatNumber() + ","
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
                    "Failed to save workflow", e
            );
        }
    }

    // Find workflow by ticket ID
    public WorkflowRecord findByTicketId(String ticketId) {

        validateField(ticketId);

        for (WorkflowRecord record : findAll()) {
            if (record.getTicketId().equals(ticketId)) {
                return record;
            }
        }

        return null;
    }

    // Load latest state of every workflow
    public List<WorkflowRecord> findAll() {

        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath,
                    StandardCharsets.UTF_8
            );

            Map<String, WorkflowRecord> records =
                    new LinkedHashMap<>();

            for (String line : lines) {

                String[] parts = line.split(",", -1);

                if (parts.length != 3) {
                    throw new IllegalStateException(
                            "Invalid workflow record"
                    );
                }

                String ticketId = parts[0];
                String seatNumber = parts[1];

                validateField(ticketId);
                validateField(seatNumber);

                WorkflowStatus status =
                        WorkflowStatus.valueOf(parts[2]);

                WorkflowRecord record = records.get(ticketId);

                if (record == null) {
                    record = new WorkflowRecord(
                            ticketId,
                            seatNumber
                    );

                    records.put(ticketId, record);
                } else if (!record.getSeatNumber()
                        .equals(seatNumber)) {
                    throw new IllegalStateException(
                            "Seat number changed for workflow"
                    );
                }

                if (record.getStatus() != status) {
                    record.updateStatus(status);
                }
            }

            return new ArrayList<>(records.values());

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load workflows", e
            );
        }
    }

    // Find workflows that need recovery
    public List<WorkflowRecord> findIncomplete() {

        List<WorkflowRecord> incomplete =
                new ArrayList<>();

        for (WorkflowRecord record : findAll()) {

            if (record.getStatus() != WorkflowStatus.COMPLETED
                    && record.getStatus()
                    != WorkflowStatus.COMPENSATED) {

                incomplete.add(record);
            }
        }

        return incomplete;
    }

    private void validateField(String value) {

        if (value == null
                || value.isBlank()
                || value.contains(",")
                || value.contains("\n")
                || value.contains("\r")) {

            throw new IllegalArgumentException(
                    "Invalid workflow data"
            );
        }
    }
}
