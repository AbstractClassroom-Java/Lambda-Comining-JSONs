package io.github.nathanjrussell.service;

import io.github.nathanjrussell.model.CombinedClient;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DataMergeTest {

    @Test
    void lookup_existing_employee_returns_combined_client() {
        DataMerge merge = new DataMerge();

        Optional<CombinedClient> combinedOpt = merge.findCombinedByEmployeeId("E00001");

        assertTrue(combinedOpt.isPresent());
        CombinedClient combined = combinedOpt.get();

        assertEquals("E00001", combined.employeeID());
        assertNotNull(combined.firstName());
        assertNotNull(combined.lastName());
        assertNotNull(combined.employerID());
        assertNotNull(combined.employerName());
        assertTrue(combined.insurancePlan() > 0);
    }

    @Test
    void lookup_missing_employee_returns_empty() {
        DataMerge merge = new DataMerge();
        assertTrue(merge.findCombinedByEmployeeId("E99999").isEmpty());
    }
}
