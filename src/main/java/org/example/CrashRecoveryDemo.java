
package org.example;

import org.example.reservation.*;
import org.example.processing.*;
import org.example.finalization.*;
import org.example.workflow.*;

public class CrashRecoveryDemo {

    public static void main(String[] args) {

        String ticketId = "CRASH-DEMO-950";
        String seat = "Z950";

        ReservationService reservationService =
                new ReservationService(
                        new ReservationRepository(),
                        new SeatValidator()
                );

        TicketProcessingService processingService =
                new TicketProcessingService(
                        new TicketRepository(),
                        new FraudChecker(),
                        new TicketPrinter()
                );

        FinalizationService finalizationService =
                new FinalizationService(
                        new FinalizationRepository(),
                        new AccessControlGate(),
                        new RefundService()
                );

        WorkflowRepository workflowRepository =
                new WorkflowRepository();

        WorkflowRecoveryService recoveryService =
                new WorkflowRecoveryService(
                        workflowRepository,
                        reservationService,
                        processingService,
                        finalizationService
                );

        // First run: simulate interruption after B
        if (args.length > 0 && args[0].equals("crash")) {

            WorkflowRecord workflow =
                    new WorkflowRecord(ticketId, seat);

            workflowRepository.save(workflow);

            reservationService.reserveSeat(seat);
            workflow.updateStatus(WorkflowStatus.A_COMPLETED);
            workflowRepository.save(workflow);

            processingService.processTicket(ticketId, seat);
            workflow.updateStatus(WorkflowStatus.B_COMPLETED);
            workflowRepository.save(workflow);

            System.out.println("=== SIMULATED CRASH AFTER B ===");
            System.out.println("A and B saved to CSV.");
            System.out.println("C has not started.");

            // Abruptly terminate without compensation
            Runtime.getRuntime().halt(1);
        }

        // Second run: read persisted data and recover
        System.out.println("=== APPLICATION RESTARTED ===");
        recoveryService.recoverIncompleteBookings();

        System.out.println(
                "Reservation: " +
                        (reservationService.getReservation(seat) == null
                                ? "NOT FOUND"
                                : reservationService.getReservation(seat).getStatus())
        );

        System.out.println(
                "Ticket: " +
                        (processingService.getTicket(ticketId) == null
                                ? "NOT FOUND"
                                : processingService.getTicket(ticketId).getStatus())
        );

        System.out.println(
                "Workflow: " +
                        (workflowRepository.findByTicketId(ticketId) == null
                                ? "NOT FOUND"
                                : workflowRepository.findByTicketId(ticketId).getStatus())
        );

        System.out.println("=== RECOVERY FINISHED ===");
    }
}
