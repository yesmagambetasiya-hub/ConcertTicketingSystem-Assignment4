
package org.example.processing;

public class Ticket {

    private final String ticketId;
    private final String seatNumber;
    private TicketStatus status;

    public Ticket(String ticketId, String seatNumber) {

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
        this.status = TicketStatus.PENDING;
    }

    public void printTicket() {

        if (status != TicketStatus.PENDING) {
            throw new IllegalStateException(
                    "Ticket cannot be printed"
            );
        }

        status = TicketStatus.PRINTED;
    }

    public void cancelTicket() {

        if (status == TicketStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Ticket is already cancelled"
            );
        }

        status = TicketStatus.CANCELLED;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public TicketStatus getStatus() {
        return status;
    }
}

