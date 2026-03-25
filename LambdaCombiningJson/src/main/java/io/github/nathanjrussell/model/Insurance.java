package io.github.nathanjrussell.model;

import java.util.List;
import java.util.Objects;

/**
 * Insurance document is keyed by employerID, and the value is a 2-element array of ints:
 *   [planForLastNameAtoM, planForLastNameNtoZ]
 */
public record Insurance(String employerID, List<Integer> plans) {
    public Insurance {
        Objects.requireNonNull(employerID, "employerID");
        Objects.requireNonNull(plans, "plans");
        if (plans.size() != 2) {
            throw new IllegalArgumentException("plans must contain exactly 2 integers");
        }
    }

    public int planForLastName(String lastName) {
        Objects.requireNonNull(lastName, "lastName");
        if (lastName.isEmpty()) {
            throw new IllegalArgumentException("lastName must not be empty");
        }
        char c = Character.toUpperCase(lastName.charAt(0));
        return (c >= 'A' && c <= 'M') ? plans.getFirst() : plans.get(1);
    }
}
