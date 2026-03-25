package io.github.nathanjrussell.model;

import java.util.Objects;

public record Employer(String companyName, String employerID) {
    public Employer {
        Objects.requireNonNull(companyName, "companyName");
        Objects.requireNonNull(employerID, "employerID");
    }
}
