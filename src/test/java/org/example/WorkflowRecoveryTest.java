
package org.example;

import org.example.reservation.*;
import org.example.processing.*;
import org.example.finalization.*;
import org.example.workflow.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowRecoveryTest {

    @TempDir
    Path tempDir;

    @Test
    void recoveryAfterStepBTest() {

        // Create persistent repositories
        Path reservationFile =
                tempDir.resolve("reservations.csv");

        Path ticketFile =
                tempDir.resolve("tickets.csv");

        Path finalizationFile =
                tempDir.resolve("finalizations.csv");

        Path workflowFile =
                tempDir.resolve("workflows.csv");

        ReservationService reservationService =
                new ReservationService(
                        new ReservationRepository(reservationFile),
                        new SeatValidator()
                );

        TicketProcessingService processingService =
                new TicketProcessingService(
                        new TicketRepository(ticketFile),
                        new FraudChecker(),
                        new TicketPrinter()
                );

        FinalizationService finalizationService =
                new FinalizationService(
                        new FinalizationRepository(finalizationFile),
                        new AccessControlGate(),
                        new RefundService()
                );

        WorkflowRepository workflowRepository =
                new WorkflowRepository(workflowFile);

        // Simulate incomplete booking after Step B
        WorkflowRecord workflow =
                new WorkflowRecord("CRASH-TEST-101", "F6");

        workflowRepository.save(workflow);

        reservationService.reserveSeat("F6");

        workflow.updateStatus(WorkflowStatus.A_COMPLETED);
        workflowRepository.save(workflow);

        processingService.processTicket(
                "CRASH-TEST-101",
                "F6"
        );

        workflow.updateStatus(WorkflowStatus.B_COMPLETED);
        workflowRepository.save(workflow);

        // Simulate restart with new service instances
        ReservationService restartedReservationService =
                new ReservationService(
                        new ReservationRepository(reservationFile),
                        new SeatValidator()
                );

        TicketProcessingService restartedProcessingService =
                new TicketProcessingService(
                        new TicketRepository(ticketFile),
                        new FraudChecker(),
                        new TicketPrinter()
                );

        FinalizationService restartedFinalizationService =
                new FinalizationService(
                        new FinalizationRepository(finalizationFile),
                        new AccessControlGate(),
                        new RefundService()
                );

        WorkflowRepository restartedWorkflowRepository =
                new WorkflowRepository(workflowFile);

        WorkflowRecoveryService recoveryService =
                new WorkflowRecoveryService(
                        restartedWorkflowRepository,
                        restartedReservationService,
                        restartedProcessingService,
                        restartedFinalizationService
                );

        recoveryService.recoverIncompleteBookings();

        // Verify compensation
        assertEquals(
                ReservationStatus.CANCELLED,
                restartedReservationService
                        .getReservation("F6")
                        .getStatus()
        );

        assertEquals(
                TicketStatus.CANCELLED,
                restartedProcessingService
                        .getTicket("CRASH-TEST-101")
                        .getStatus()
        );

        assertEquals(
                WorkflowStatus.COMPENSATED,
                restartedWorkflowRepository
                        .findByTicketId("CRASH-TEST-101")
                        .getStatus()
        );

        assertNull(
                restartedFinalizationService
                        .getFinalization("CRASH-TEST-101")
        );

        // Verify repeated recovery is safe
        recoveryService.recoverIncompleteBookings();

        assertTrue(
                restartedWorkflowRepository.findIncomplete().isEmpty()
        );
    }
}
