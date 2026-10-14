
package org.example.workflow;

public class WorkflowRecord {

    private final String ticketId;
    private final String seatNumber;
    private WorkflowStatus status;

    public WorkflowRecord(
            String ticketId,
            String seatNumber) {

        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException(
                    "Ticket ID cannot be empty"
            );
        }

        if (seatNumber == null || seatNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Seat number cannot be empty"
            );
        }

        this.ticketId = ticketId;
        this.seatNumber = seatNumber;
        this.status = WorkflowStatus.STARTED;
    }

    public void updateStatus(WorkflowStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Workflow status cannot be null"
            );
        }

        if (!isAllowedTransition(status, newStatus)) {
            throw new IllegalStateException(
                    "Invalid workflow transition: "
                            + status + " -> " + newStatus
            );
        }

        this.status = newStatus;
    }

    private boolean isAllowedTransition(
            WorkflowStatus current,
            WorkflowStatus next) {

        return switch (current) {

            case STARTED ->
                    next == WorkflowStatus.A_COMPLETED
                            || next == WorkflowStatus.COMPENSATING;

            case A_COMPLETED ->
                    next == WorkflowStatus.B_COMPLETED
                            || next == WorkflowStatus.COMPENSATING;

            case B_COMPLETED ->
                    next == WorkflowStatus.COMPLETED
                            || next == WorkflowStatus.COMPENSATING;

            case COMPENSATING ->
                    next == WorkflowStatus.COMPENSATED
                            || next == WorkflowStatus.RECOVERY_FAILED;

            case RECOVERY_FAILED ->
                    next == WorkflowStatus.COMPENSATING;

            case COMPLETED, COMPENSATED -> false;
        };
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public WorkflowStatus getStatus() {
        return status;
    }
}
