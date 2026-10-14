
package org.example;

import org.example.facade.ConcertBookingFacade;
import org.example.reservation.*;
import org.example.processing.*;
import org.example.finalization.*;
import org.example.workflow.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FinalizationFailureTest {

    @TempDir
    Path tempDir;

    @Test
    void finalizationFailureCompensationTest() {

        ReservationService reservationService =
                new ReservationService(
                        new ReservationRepository(
                                tempDir.resolve("reservations.csv")
                        ),
                        new SeatValidator()
                );

        TicketProcessingService processingService =
                new TicketProcessingService(
                        new TicketRepository(
                                tempDir.resolve("tickets.csv")
                        ),
                        new FraudChecker(),
                        new TicketPrinter()
                );

        FinalizationService finalizationService =
                new FinalizationService(
                        new FinalizationRepository(
                                tempDir.resolve("finalizations.csv")
                        ),
                        new AccessControlGate(),
                        new RefundService()
                );

        WorkflowRepository workflowRepository =
                new WorkflowRepository(
                        tempDir.resolve("workflows.csv")
                );

        ConcertBookingFacade bookingFacade =
                new ConcertBookingFacade(
                        reservationService,
                        processingService,
                        finalizationService,
                        workflowRepository
                );

        assertThrows(
                SecurityException.class,
                () -> bookingFacade.bookConcertTicket(
                        "BLOCKED-201",
                        "C3"
                )
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reservationService
                        .getReservation("C3")
                        .getStatus()
        );

        assertEquals(
                TicketStatus.CANCELLED,
                processingService
                        .getTicket("BLOCKED-201")
                        .getStatus()
        );

        assertEquals(
                FinalizationStatus.REFUNDED,
                finalizationService
                        .getFinalization("BLOCKED-201")
                        .getStatus()
        );

        assertEquals(
                WorkflowStatus.COMPENSATED,
                workflowRepository
                        .findByTicketId("BLOCKED-201")
                        .getStatus()
        );
    }
}
