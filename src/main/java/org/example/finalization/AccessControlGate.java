
package org.example.finalization;

public class AccessControlGate {

    public void verifyAccess(FinalizationRecord record) {

        if (record == null) {
            throw new IllegalArgumentException(
                    "Finalization record cannot be null"
            );
        }

        if (record.getStatus() != FinalizationStatus.PENDING) {
            throw new IllegalStateException(
                    "Ticket is not ready for access approval"
            );
        }

        if (record.getTicketId().startsWith("BLOCKED")) {
            throw new SecurityException(
                    "Access denied: blocked ticket"
            );
        }

        record.approve();

        System.out.println("Access approved!");
    }
}

