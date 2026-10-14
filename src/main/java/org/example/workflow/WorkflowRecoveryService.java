
package org.example.workflow;

import org.example.reservation.*;
import org.example.processing.*;
import org.example.finalization.*;

import java.util.List;

public class WorkflowRecoveryService {

    private final WorkflowRepository workflowRepository;
    private final ReservationService reservationService;
    private final TicketProcessingService processingService;
    private final FinalizationService finalizationService;

    public WorkflowRecoveryService(
            WorkflowRepository workflowRepository,
            ReservationService reservationService,
            TicketProcessingService processingService,
            FinalizationService finalizationService) {

        this.workflowRepository = workflowRepository;
        this.reservationService = reservationService;
        this.processingService = processingService;
        this.finalizationService = finalizationService;
    }

    public void recoverIncompleteBookings() {

        System.out.println("=== WORKFLOW RECOVERY ===");

        List<WorkflowRecord> incomplete =
                workflowRepository.findIncomplete();

        if (incomplete.isEmpty()) {
            System.out.println("No incomplete bookings found.");
            return;
        }

        for (WorkflowRecord record : incomplete) {
            recoverBooking(record);
        }
    }

    private void recoverBooking(WorkflowRecord record) {

        String ticketId = record.getTicketId();
        String seatNumber = record.getSeatNumber();

        System.out.println("Recovering: " + ticketId);

        if (record.getStatus() != WorkflowStatus.COMPENSATING) {
            record.updateStatus(WorkflowStatus.COMPENSATING);
            workflowRepository.save(record);
        }

        boolean successful = true;

        // Step C: Refund if necessary
        try {
            FinalizationRecord finalization =
                    finalizationService.getFinalization(ticketId);

            if (finalization != null
                    && finalization.getStatus()
                    != FinalizationStatus.REFUNDED) {

                finalizationService.refundTicket(ticketId);
                System.out.println("Recovery C: Refunded");
            }

        } catch (RuntimeException e) {
            successful = false;
            System.out.println("Recovery C failed: " + e.getMessage());
        }

        // Step B: Cancel ticket
        try {
            Ticket ticket = processingService.getTicket(ticketId);

            if (ticket != null
                    && ticket.getStatus()
                    != TicketStatus.CANCELLED) {

                processingService.cancelTicket(ticketId);
                System.out.println("Recovery B: Ticket cancelled");
            }

        } catch (RuntimeException e) {
            successful = false;
            System.out.println("Recovery B failed: " + e.getMessage());
        }

        // Step A: Cancel reservation
        try {
            SeatReservation reservation =
                    reservationService.getReservation(seatNumber);

            if (reservation != null
                    && reservation.getStatus()
                    == ReservationStatus.RESERVED) {

                reservationService.cancelSeat(seatNumber);
                System.out.println("Recovery A: Reservation cancelled");
            }

        } catch (RuntimeException e) {
            successful = false;
            System.out.println("Recovery A failed: " + e.getMessage());
        }

        if (successful) {
            record.updateStatus(WorkflowStatus.COMPENSATED);
            workflowRepository.save(record);

            System.out.println("Recovery completed: " + ticketId);

        } else {
            record.updateStatus(WorkflowStatus.RECOVERY_FAILED);
            workflowRepository.save(record);

            System.out.println("Recovery incomplete: " + ticketId);
        }
    }
}
