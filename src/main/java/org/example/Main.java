
package org.example;

import org.example.reservation.*;
import org.example.processing.*;
import org.example.finalization.*;
import org.example.facade.*;
import org.example.workflow.*;

public class Main {

    public static void main(String[] args) {

        // Subsystem A: Reservation
        ReservationService reservationService =
                new ReservationService(
                        new ReservationRepository(),
                        new SeatValidator()
                );

        // Subsystem B: Processing
        TicketProcessingService processingService =
                new TicketProcessingService(
                        new TicketRepository(),
                        new FraudChecker(),
                        new TicketPrinter()
                );

        // Subsystem C: Finalization
        FinalizationService finalizationService =
                new FinalizationService(
                        new FinalizationRepository(),
                        new AccessControlGate(),
                        new RefundService()
                );

        // Workflow persistence
        WorkflowRepository workflowRepository =
                new WorkflowRepository();

        // Booking Facade
        ConcertBookingFacade bookingFacade =
                new ConcertBookingFacade(
                        reservationService,
                        processingService,
                        finalizationService,
                        workflowRepository
                );

        // Admin Facade
        AdminFacade adminFacade =
                new AdminFacade(
                        reservationService,
                        processingService,
                        finalizationService
                );

        // Crash Recovery Service
        WorkflowRecoveryService recoveryService =
                new WorkflowRecoveryService(
                        workflowRepository,
                        reservationService,
                        processingService,
                        finalizationService
                );

        System.out.println(
                "=== CONCERT TICKETING SYSTEM ==="
        );

        // Recover incomplete bookings after restart
        recoveryService.recoverIncompleteBookings();

        String ticketId = "TICKET-9002";
        String seatNumber = "Z9002";

        try {
            bookingFacade.bookConcertTicket(
                    ticketId,
                    seatNumber
            );

        } catch (RuntimeException e) {
            System.out.println(
                    "Booking error: " + e.getMessage()
            );
        }

        // Admin inspection
        adminFacade.inspectBooking(
                ticketId,
                seatNumber
        );

        System.out.println(
                "=== SYSTEM FINISHED ==="
        );
    }
}
