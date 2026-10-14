
package org.example.processing;

public class TicketProcessingService {

    private final TicketRepository repository;
    private final FraudChecker fraudChecker;
    private final TicketPrinter ticketPrinter;

    public TicketProcessingService(
            TicketRepository repository,
            FraudChecker fraudChecker,
            TicketPrinter ticketPrinter) {

        this.repository = repository;
        this.fraudChecker = fraudChecker;
        this.ticketPrinter = ticketPrinter;
    }

    public Ticket processTicket(
            String ticketId,
            String seatNumber) {

        Ticket existing = repository.findById(ticketId);

        if (existing != null) {
            throw new IllegalStateException(
                    "Ticket ID already exists"
            );
        }

        Ticket ticket = new Ticket(ticketId, seatNumber);

        repository.save(ticket);

        fraudChecker.checkTicket(ticket);

        ticketPrinter.print(ticket);

        repository.save(ticket);

        return ticket;
    }

    public void cancelTicket(String ticketId) {

        Ticket ticket = repository.findById(ticketId);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket not found"
            );
        }

        ticket.cancelTicket();

        repository.save(ticket);
    }

    public Ticket getTicket(String ticketId) {
        return repository.findById(ticketId);
    }
}
