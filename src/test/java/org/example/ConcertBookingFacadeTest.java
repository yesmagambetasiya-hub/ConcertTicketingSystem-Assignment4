
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

public class ConcertBookingFacadeTest {

    @TempDir
    Path tempDir;

    @Test
    void successfulBookingTest() {

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

        bookingFacade.bookConcertTicket(
                "TEST-101",
                "A1"
        );

        assertEquals(
                ReservationStatus.RESERVED,
                reservationService
                        .getReservation("A1")
                        .getStatus()
        );

        assertEquals(
                TicketStatus.PRINTED,
                processingService
                        .getTicket("TEST-101")
                        .getStatus()
        );

        assertEquals(
                FinalizationStatus.APPROVED,
                finalizationService
                        .getFinalization("TEST-101")
                        .getStatus()
        );

        assertEquals(
                WorkflowStatus.COMPLETED,
                workflowRepository
                        .findByTicketId("TEST-101")
                        .getStatus()
        );
    }

    @Test
    void fraudFailureCompensationTest() {

        ReservationService reservationService =
                new ReservationService(
                        new ReservationRepository(
                                tempDir.resolve("fraud-reservations.csv")
                        ),
                        new SeatValidator()
                );

        TicketProcessingService processingService =
                new TicketProcessingService(
                        new TicketRepository(
                                tempDir.resolve("fraud-tickets.csv")
                        ),
                        new FraudChecker(),
                        new TicketPrinter()
                );

        FinalizationService finalizationService =
                new FinalizationService(
                        new FinalizationRepository(
                                tempDir.resolve("fraud-finalizations.csv")
                        ),
                        new AccessControlGate(),
                        new RefundService()
                );

        WorkflowRepository workflowRepository =
                new WorkflowRepository(
                        tempDir.resolve("fraud-workflows.csv")
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
                        "FRAUD-102",
                        "B2"
                )
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reservationService.getReservation("B2").getStatus()
        );

        assertEquals(
                TicketStatus.CANCELLED,
                processingService.getTicket("FRAUD-102").getStatus()
        );

        assertEquals(
                WorkflowStatus.COMPENSATED,
                workflowRepository
                        .findByTicketId("FRAUD-102")
                        .getStatus()
        );

        assertNull(
                finalizationService.getFinalization("FRAUD-102")
        );
    }

}

