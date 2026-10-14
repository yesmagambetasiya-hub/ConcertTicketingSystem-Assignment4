
package org.example.finalization;

public class FinalizationService {

    private final FinalizationRepository repository;
    private final AccessControlGate accessControlGate;
    private final RefundService refundService;

    public FinalizationService(
            FinalizationRepository repository,
            AccessControlGate accessControlGate,
            RefundService refundService) {

        this.repository = repository;
        this.accessControlGate = accessControlGate;
        this.refundService = refundService;
    }

    public FinalizationRecord finalizeTicket(String ticketId) {

        FinalizationRecord existing =
                repository.findByTicketId(ticketId);

        if (existing != null) {
            throw new IllegalStateException(
                    "Finalization already exists"
            );
        }

        FinalizationRecord record =
                new FinalizationRecord(ticketId);

        repository.save(record);

        accessControlGate.verifyAccess(record);

        repository.save(record);

        return record;
    }

    public void refundTicket(String ticketId) {

        FinalizationRecord record =
                repository.findByTicketId(ticketId);

        if (record == null) {
            throw new IllegalArgumentException(
                    "Finalization record not found"
            );
        }

        refundService.processRefund(record);

        repository.save(record);
    }

    public FinalizationRecord getFinalization(String ticketId) {
        return repository.findByTicketId(ticketId);
    }
}
