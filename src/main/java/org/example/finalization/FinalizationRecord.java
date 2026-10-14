
package org.example.finalization;

public class FinalizationRecord {

    private final String ticketId;
    private FinalizationStatus status;

    public FinalizationRecord(String ticketId) {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket ID cannot be empty"
            );
        }

        this.ticketId = ticketId;
        this.status = FinalizationStatus.PENDING;
    }

    public void approve() {

        if (status != FinalizationStatus.PENDING) {
            throw new IllegalStateException(
                    "Finalization cannot be approved"
            );
        }

        status = FinalizationStatus.APPROVED;
    }

    public void refund() {

        if (status == FinalizationStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Ticket is already refunded"
            );
        }

        status = FinalizationStatus.REFUNDED;
    }

    public String getTicketId() {
        return ticketId;
    }

    public FinalizationStatus getStatus() {
        return status;
    }
}
