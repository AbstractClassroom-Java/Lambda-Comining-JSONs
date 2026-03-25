package io.github.nathanjrussell;

import io.github.nathanjrussell.service.DataMerge;

public class Main {
    public static void main(String[] args) {
        DataMerge merge = new DataMerge();

        String employeeId = (args.length > 0) ? args[0] : "E00005";

        merge.findCombinedByEmployeeId(employeeId)
                .ifPresentOrElse(
                        combined -> System.out.println(combined.toString()),
                        () -> System.out.println("No client found for employeeID=" + employeeId)
                );
    }
}