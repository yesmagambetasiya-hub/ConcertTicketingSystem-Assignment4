
# Concert Ticketing System

**Assignment 4 - Facade Design Pattern**

## 1. Project Overview

Concert Ticketing System is a Java application that demonstrates the Facade Design Pattern, workflow orchestration, persistence, compensation, and crash recovery.

The system manages concert ticket bookings through three independent subsystems:

- Subsystem A: Reservation
- Subsystem B: Ticket Processing
- Subsystem C: Finalization

The application uses Facades to coordinate these subsystems and simplify client interactions.

## 2. Technologies Used

- Java 25
- Maven
- IntelliJ IDEA
- JUnit 5
- JaCoCo (configured, but coverage not verified)
- CSV files for data persistence

## 3. Project Structure

The project contains the following packages:

### reservation

Subsystem A manages seat reservations and pricing.

Classes:

- SeatReservation
- ReservationStatus
- SeatValidator
- ReservationService
- ReservationRepository
- DynamicPricingEngine

### processing

Subsystem B manages ticket processing, fraud detection, and printing.

Classes:

- Ticket
- TicketStatus
- FraudChecker
- TicketPrinter
- TicketProcessingService
- TicketRepository

### finalization

Subsystem C manages access verification and refunds.

Classes:

- FinalizationRecord
- FinalizationStatus
- AccessControlGate
- RefundService
- FinalizationService
- FinalizationRepository

### facade

This package contains the Facade classes.

- ConcertBookingFacade
- AdminFacade

### workflow

This package manages workflow progress and crash recovery.

- WorkflowRecord
- WorkflowStatus
- WorkflowRepository
- WorkflowRecoveryService

### Main

The Main class demonstrates application functionality.

## 4. Facade Design Pattern

The Facade Pattern provides a simple interface to a complex system.

Instead of directly interacting with multiple services, the client communicates with a Facade.

### ConcertBookingFacade

ConcertBookingFacade coordinates the complete booking workflow.

Booking steps:

1. Reserve a seat using Subsystem A.
2. Process and print a ticket using Subsystem B.
3. Verify access using Subsystem C.
4. Save workflow progress.
5. Complete the booking.

If a failure occurs, the Facade performs compensation.

### AdminFacade

AdminFacade provides administrative operations.

Main responsibilities:

- Inspect booking information.
- Display reservation, ticket, and finalization statuses.
- Manually compensate incomplete or unsuccessful bookings.

## 5. Principle of Least Knowledge

The Principle of Least Knowledge, also known as the Law of Demeter, recommends reducing unnecessary dependencies between objects.

### Before Facade

Without the Facade Pattern, the client must interact directly with several services.

Example operations:

- Call ReservationService to reserve a seat.
- Call TicketProcessingService to process a ticket.
- Call FinalizationService to approve access.
- Handle failures and compensation manually.

This makes the client more complex and increases coupling.

### After Facade

With the Facade Pattern, the client calls one method:

**bookConcertTicket(ticketId, seatNumber)**

The Facade handles all subsystem interactions.

Benefits:

- Simpler client code.
- Reduced coupling.
- Better separation of responsibilities.
- Centralized workflow coordination.
- Easier maintenance and testing.

## 6. Subsystem A - Reservation

Subsystem A is responsible for seat reservations.

Reservation statuses:

- AVAILABLE
- RESERVED
- CANCELLED

The SeatValidator checks whether a seat is available.

The ReservationService performs reservation and cancellation operations.

The ReservationRepository saves reservation states in a CSV file.

The DynamicPricingEngine calculates ticket prices based on seat categories.

Example prices:

- VIP: 30000
- STANDARD: 15000
- ECONOMY: 8000

## 7. Subsystem B - Ticket Processing

Subsystem B processes concert tickets.

Ticket statuses:

- PENDING
- PRINTED
- CANCELLED

The FraudChecker checks whether a ticket is suspicious.

The TicketPrinter prints ticket information.

The TicketProcessingService coordinates ticket processing.

The TicketRepository saves ticket states.

If fraud is detected, processing fails and compensation is performed.

## 8. Subsystem C - Finalization

Subsystem C handles final access verification.

Finalization statuses:

- PENDING
- APPROVED
- REFUNDED

The AccessControlGate verifies ticket access.

The RefundService processes refunds.

The FinalizationService coordinates finalization operations.

The FinalizationRepository stores finalization states.

## 9. Compensation Mechanism

Compensation reverses previously completed operations when a later operation fails.

### Failure in Subsystem B

If ticket processing fails:

1. Cancel the ticket if it was created.
2. Cancel the reservation.
3. Update the workflow status.

### Failure in Subsystem C

If access verification fails:

1. Refund the finalization record.
2. Cancel the ticket.
3. Cancel the reservation.
4. Update the workflow status.

Compensation follows the reverse order:

**C -> B -> A**

This helps prevent incomplete bookings from remaining active.

## 10. Workflow Management

The application stores the progress of each booking.

Workflow statuses:

- STARTED
- A_COMPLETED
- B_COMPLETED
- COMPLETED
- COMPENSATING
- COMPENSATED
- RECOVERY_FAILED

WorkflowRecord represents a booking workflow.

WorkflowRepository saves and loads workflow information.

The workflow status helps identify incomplete operations.

## 11. Persistence

Persistence means that data remains available after the application stops.

The project uses CSV files to store data.

Main storage files:

- data/reservations.csv
- data/tickets.csv
- data/finalizations.csv
- data/workflows.csv

Each subsystem has its own repository.

Repositories save entity states and load them when needed.

The application can restore previously saved information after a restart.

## 12. Crash Recovery

Crash Recovery allows the application to handle incomplete workflows after an unexpected interruption.

WorkflowRecoveryService searches for unfinished bookings.

If an incomplete workflow is found, the recovery service attempts to compensate completed operations.

Example: Crash after Subsystem A

1. The reservation was completed.
2. The application stopped before Subsystem B.
3. Recovery detects the incomplete workflow.
4. The reservation is cancelled.
5. The workflow is marked as compensated.

Example: Crash after Subsystem B

1. The reservation was completed.
2. The ticket was processed.
3. The application stopped before Subsystem C.
4. Recovery detects the incomplete workflow.
5. The ticket is cancelled.
6. The reservation is cancelled.
7. The workflow is marked as compensated.

The recovery process was tested manually and using JUnit.

## 13. JUnit Testing

JUnit 5 is used for automated testing.

The following five tests were successfully executed:

### Test 1 - Successful Booking

Method: successfulBookingTest()

Verifies that:

- The seat is reserved.
- The ticket is printed.
- Access is approved.
- The booking is completed.

### Test 2 - Fraud Failure

Method: fraudFailureCompensationTest()

Verifies that:

- Fraud detection rejects a suspicious ticket.
- The ticket is cancelled.
- The reservation is cancelled.
- The workflow is compensated.

### Test 3 - Finalization Failure

Method: finalizationFailureCompensationTest()

Verifies that:

- Access verification rejects a blocked ticket.
- Finalization is refunded.
- The ticket is cancelled.
- The reservation is cancelled.
- The workflow is compensated.

### Test 4 - Persistence

Method: workflowPersistenceTest()

Verifies that:

- Workflow information is saved.
- A new repository instance can load saved data.
- The workflow status remains correct.
- Incomplete workflows can be identified.

### Test 5 - Crash Recovery

Method: recoveryAfterStepBTest()

Verifies that:

- An incomplete workflow is detected.
- Ticket processing is compensated.
- Reservation is compensated.
- Workflow status becomes COMPENSATED.
- Repeated recovery does not repeat completed compensation.

### Test Results

- Total tests: 5
- Passed: 5
- Failed: 0
- Errors: 0
- Skipped: 0

All five tests passed successfully using Maven with JaCoCo disabled.

## 14. Code Coverage

JaCoCo is configured in the Maven project.

The assignment requires at least 80% line coverage.

However, the coverage-enabled Maven execution encountered a Java instrumentation error.

Therefore, the 80% coverage requirement has not yet been verified.

## 15. Facade vs Adapter

Facade and Adapter are structural design patterns, but they solve different problems.

### Facade Pattern

Facade provides a simplified interface to multiple existing components.

It is useful when a client needs to work with a complex subsystem.

### Adapter Pattern

Adapter converts an existing interface into another interface expected by the client.

It is useful when two interfaces are incompatible.

### Why Facade Was Selected

Concert Ticketing System contains three independent subsystems.

The goal is to coordinate these subsystems through a simple interface.

Therefore, Facade is more appropriate than Adapter.

## 16. SOLID and Clean Code Principles

### Single Responsibility Principle (SRP)

Each class has a specific responsibility.

For example:

- FraudChecker checks tickets.
- TicketPrinter prints tickets.
- ReservationRepository stores reservations.
- WorkflowRecoveryService handles recovery.

### Dependency Inversion Principle (DIP)

The Facade receives service dependencies through its constructor.

This separates object creation from workflow coordination.

### Separation of Concerns

Reservation, processing, and finalization are implemented in separate packages.

Each subsystem manages its own operations and persistence.

### Principle of Least Knowledge

The client interacts with the Facade instead of directly coordinating multiple subsystem services.

## 17. Application Demonstration

The application demonstrates:

- Successful concert ticket booking.
- Reservation and ticket processing.
- Access verification.
- Fraud detection.
- Failure compensation.
- Administrative booking inspection.
- Workflow persistence.
- Crash recovery.

The Main class runs predefined demonstration scenarios.

## 18. Known Limitations

- CSV files are used instead of a database.
- Cancelled reservations cannot currently be booked again.
- Recovery is not implemented as a fully atomic database transaction.
- JaCoCo line coverage has not been verified.
- The current application does not provide a complete interactive CLI menu.

## 19. Conclusion

Concert Ticketing System demonstrates the Facade Design Pattern in Java.

The application coordinates three independent subsystems through simplified interfaces.

It supports booking workflows, status management, persistent storage, compensation, crash recovery, and automated testing.

The project demonstrates how the Facade Pattern reduces client complexity and improves separation of responsibilities.
