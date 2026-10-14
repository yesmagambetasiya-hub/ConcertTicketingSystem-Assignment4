
package org.example.finalization;

public class RefundService {

    public void processRefund(FinalizationRecord record) {

        if (record == null) {
            throw new IllegalArgumentException(
                    "Finalization record cannot be null"
            );
        }

        if (record.getStatus() == FinalizationStatus.REFUNDED) {
            throw new IllegalStateException(
                    "Ticket is already refunded"
            );
        }

        record.refund();

        System.out.println("=== REFUND PROCESSING ===");
        System.out.println("Ticket ID: " + record.getTicketId());
        System.out.println("Status: " + record.getStatus());
        System.out.println("Refund processed successfully!");
    }
}

