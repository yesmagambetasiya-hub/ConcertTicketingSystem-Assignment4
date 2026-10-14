
package org.example.processing;

public class TicketPrinter {

    public void print(Ticket ticket) {

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket cannot be null"
            );
        }

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalStateException(
                    "Ticket is not ready for printing"
            );
        }

        ticket.printTicket();

        System.out.println("=== CONCERT TICKET ===");
        System.out.println("Ticket ID: " + ticket.getTicketId());
        System.out.println("Seat: " + ticket.getSeatNumber());
        System.out.println("Status: " + ticket.getStatus());
        System.out.println("Ticket printed successfully!");
    }
}

