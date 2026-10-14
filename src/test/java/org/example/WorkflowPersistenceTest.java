
package org.example;

import org.example.workflow.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void workflowPersistenceTest() {

        Path file = tempDir.resolve("workflows.csv");

        // First repository instance
        WorkflowRepository firstRepository =
                new WorkflowRepository(file);

        WorkflowRecord workflow =
                new WorkflowRecord("PERSIST-101", "D4");

        firstRepository.save(workflow);

        workflow.updateStatus(
                WorkflowStatus.A_COMPLETED
        );
        firstRepository.save(workflow);

        workflow.updateStatus(
                WorkflowStatus.B_COMPLETED
        );
        firstRepository.save(workflow);

        // Simulate application restart
        WorkflowRepository secondRepository =
                new WorkflowRepository(file);

        WorkflowRecord restored =
                secondRepository.findByTicketId("PERSIST-101");

        assertNotNull(restored);

        assertEquals(
                "D4",
                restored.getSeatNumber()
        );

        assertEquals(
                WorkflowStatus.B_COMPLETED,
                restored.getStatus()
        );

        assertEquals(
                1,
                secondRepository.findIncomplete().size()
        );
    }
}

