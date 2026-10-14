
package org.example.facade;

import org.example.reservation.ReservationService;
import org.example.processing.TicketProcessingService;
import org.example.finalization.FinalizationService;
import org.example.workflow.WorkflowRecord;
import org.example.workflow.WorkflowRepository;
import org.example.workflow.WorkflowStatus;

public class ConcertBookingFacade {

    private final ReservationService reservationService;
    private final TicketProcessingService processingService;
    private final FinalizationService finalizationService;
    private final WorkflowRepository workflowRepository;

    public ConcertBookingFacade(
            ReservationService reservationService,
            TicketProcessingService processingService,
            FinalizationService finalizationService,
            WorkflowRepository workflowRepository) {

        this.reservationService = reservationService;
        this.processingService = processingService;
        this.finalizationService = finalizationService;
        this.workflowRepository = workflowRepository;
    }

    public void bookConcertTicket(
            String ticketId,
            String seatNumber) {

        if (workflowRepository.findByTicketId(ticketId) != null) {
            throw new IllegalStateException(
                    "Workflow already exists: " + ticketId
            );
        }

        WorkflowRecord workflow =
                new WorkflowRecord(ticketId, seatNumber);

        workflowRepository.save(workflow);

        System.out.println("=== BOOKING STARTED ===");

        try {
            // Step A: Reservation
            reservationService.reserveSeat(seatNumber);

            workflow.updateStatus(WorkflowStatus.A_COMPLETED);
            workflowRepository.save(workflow);

            System.out.println("Step A: Seat reserved");

            // Step B: Processing
            processingService.processTicket(ticketId, seatNumber);

            workflow.updateStatus(WorkflowStatus.B_COMPLETED);
            workflowRepository.save(workflow);

            System.out.println("Step B: Ticket processed");

            // Step C: Finalization
            finalizationService.finalizeTicket(ticketId);

            workflow.updateStatus(WorkflowStatus.COMPLETED);
            workflowRepository.save(workflow);

            System.out.println("Step C: Access approved");
            System.out.println("=== BOOKING COMPLETED ===");

        } catch (RuntimeException error) {

            System.out.println(
                    "Booking failed: " + error.getMessage()
            );

            boolean compensated = true;

            try {
                workflow.updateStatus(WorkflowStatus.COMPENSATING);
                workflowRepository.save(workflow);
            } catch (RuntimeException e) {
                compensated = false;
                error.addSuppressed(e);
            }

            // Compensation C
            try {
                var finalization =
                        finalizationService.getFinalization(ticketId);

                if (finalization != null
                        && finalization.getStatus()
                        != org.example.finalization.FinalizationStatus.REFUNDED) {

                    finalizationService.refundTicket(ticketId);
                    System.out.println("Compensation C: Refunded");
                }
            } catch (RuntimeException e) {
                compensated = false;
                error.addSuppressed(e);
            }

            // Compensation B
            try {
                var ticket = processingService.getTicket(ticketId);

                if (ticket != null
                        && ticket.getStatus()
                        != org.example.processing.TicketStatus.CANCELLED) {

                    processingService.cancelTicket(ticketId);
                    System.out.println("Compensation B: Ticket cancelled");
                }
            } catch (RuntimeException e) {
                compensated = false;
                error.addSuppressed(e);
            }

            // Compensation A
            try {
                var reservation =
                        reservationService.getReservation(seatNumber);

                if (reservation != null
                        && reservation.getStatus()
                        == org.example.reservation.ReservationStatus.RESERVED) {

                    reservationService.cancelSeat(seatNumber);
                    System.out.println("Compensation A: Reservation cancelled");
                }
            } catch (RuntimeException e) {
                compensated = false;
                error.addSuppressed(e);
            }

            try {
                workflow.updateStatus(
                        compensated
                                ? WorkflowStatus.COMPENSATED
                                : WorkflowStatus.RECOVERY_FAILED
                );
                workflowRepository.save(workflow);
            } catch (RuntimeException e) {
                error.addSuppressed(e);
            }

            throw error;
        }
    }
}
