package io.github.nathanjrussell.model;

import java.util.Objects;

public record Client(String firstName, String lastName, String employeeID, String employerID) {
    public Client {
        Objects.requireNonNull(firstName, "firstName");
        Objects.requireNonNull(lastName, "lastName");
        Objects.requireNonNull(employeeID, "employeeID");
        Objects.requireNonNull(employerID, "employerID");
    }
}
