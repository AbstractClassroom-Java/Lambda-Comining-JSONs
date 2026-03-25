package io.github.nathanjrussell.model;

import java.util.Objects;

public record CombinedClient(
        String firstName,
        String lastName,
        String employeeID,
        String employerID,
        String employerName,
        int insurancePlan
) {
    public CombinedClient {
        Objects.requireNonNull(firstName, "firstName");
        Objects.requireNonNull(lastName, "lastName");
        Objects.requireNonNull(employeeID, "employeeID");
        Objects.requireNonNull(employerID, "employerID");
        Objects.requireNonNull(employerName, "employerName");
    }

    @Override
    public String toString() {
        return """
                Employee
                  Name       : %s %s
                  EmployeeID  : %s
                  Employer    : %s (%s)
                  Insurance   : plan %d
                """.formatted(firstName, lastName, employeeID, employerName, employerID, insurancePlan);
    }
}
