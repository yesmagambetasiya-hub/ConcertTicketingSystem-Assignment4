
package org.example.facade;

import org.example.reservation.ReservationService;
import org.example.reservation.SeatReservation;
import org.example.reservation.ReservationStatus;

import org.example.processing.TicketProcessingService;
import org.example.processing.Ticket;
import org.example.processing.TicketStatus;

import org.example.finalization.FinalizationService;
import org.example.finalization.FinalizationRecord;
import org.example.finalization.FinalizationStatus;

public class AdminFacade {

    private final ReservationService reservationService;
    private final TicketProcessingService processingService;
    private final FinalizationService finalizationService;

    public AdminFacade(
            ReservationService reservationService,
            TicketProcessingService processingService,
            FinalizationService finalizationService) {

        this.reservationService = reservationService;
        this.processingService = processingService;
        this.finalizationService = finalizationService;
    }

    // Inspect booking status
    public void inspectBooking(
            String ticketId,
            String seatNumber) {

        System.out.println("=== BOOKING INSPECTION ===");

        SeatReservation reservation =
                reservationService.getReservation(seatNumber);

        Ticket ticket =
                processingService.getTicket(ticketId);

        FinalizationRecord finalization =
                finalizationService.getFinalization(ticketId);

        System.out.println(
                "Reservation: " +
                        (reservation == null
                                ? "NOT FOUND"
                                : reservation.getStatus())
        );

        System.out.println(
                "Ticket: " +
                        (ticket == null
                                ? "NOT FOUND"
                                : ticket.getStatus())
        );

        System.out.println(
                "Finalization: " +
                        (finalization == null
                                ? "NOT FOUND"
                                : finalization.getStatus())
        );
    }

    // Manual compensation
    public void compensateBooking(
            String ticketId,
            String seatNumber) {

        System.out.println("=== MANUAL COMPENSATION ===");

        RuntimeException failure = null;

        // Step C: Refund
        try {
            FinalizationRecord finalization =
                    finalizationService.getFinalization(ticketId);

            if (finalization != null
                    && finalization.getStatus()
                    != FinalizationStatus.REFUNDED) {

                finalizationService.refundTicket(ticketId);

                System.out.println("C: Refunded");
            }

        } catch (RuntimeException e) {
            failure = e;
        }

        // Step B: Cancel ticket
        try {
            Ticket ticket =
                    processingService.getTicket(ticketId);

            if (ticket != null
                    && ticket.getStatus()
                    != TicketStatus.CANCELLED) {

                processingService.cancelTicket(ticketId);

                System.out.println("B: Ticket cancelled");
            }

        } catch (RuntimeException e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }

        // Step A: Cancel reservation
        try {
            SeatReservation reservation =
                    reservationService.getReservation(seatNumber);

            if (reservation != null
                    && reservation.getStatus()
                    == ReservationStatus.RESERVED) {

                reservationService.cancelSeat(seatNumber);

                System.out.println("A: Reservation cancelled");
            }

        } catch (RuntimeException e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }

        if (failure != null) {
            throw new IllegalStateException(
                    "Manual compensation incomplete",
                    failure
            );
        }

        System.out.println("=== COMPENSATION COMPLETED ===");
    }
}
