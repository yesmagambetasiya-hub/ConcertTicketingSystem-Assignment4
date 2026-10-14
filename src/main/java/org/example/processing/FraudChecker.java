
package org.example.processing;

public class FraudChecker {

    public void checkTicket(Ticket ticket) {

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket cannot be null"
            );
        }

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new IllegalStateException(
                    "Ticket is not pending"
            );
        }

        if (ticket.getTicketId().startsWith("FRAUD")) {
            throw new SecurityException(
                    "Fraud detected: suspicious ticket ID"
            );
        }

        System.out.println("Fraud check passed!");
    }
}
