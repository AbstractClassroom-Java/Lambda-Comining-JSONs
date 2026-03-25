package io.github.nathanjrussell.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nathanjrussell.model.Client;
import io.github.nathanjrussell.model.CombinedClient;
import io.github.nathanjrussell.model.Employer;
import io.github.nathanjrussell.model.Insurance;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads the JSON documents into in-memory records and exposes an on-demand merge function.
 *
 * CombinedClient objects are NOT precomputed. They're built only when you call
 * {@link #findCombinedByEmployeeId(String)}.
 */
public final class DataMerge {

    private static final String DEFAULT_CLIENTS_RESOURCE = "/data/clients.json";
    private static final String DEFAULT_EMPLOYER_RESOURCE = "/data/employer.json";
    private static final String DEFAULT_INSURANCE_RESOURCE = "/data/insurance.json";

    private final Map<String, Client> clientsByEmployeeId;
    private final Map<String, Employer> employersById;
    private final Map<String, Insurance> insuranceByEmployerId;

    // lambda that turns a Client into a CombinedClient, using the other maps
    private final Function<Client, CombinedClient> combine;

    public DataMerge() {
        this(DEFAULT_CLIENTS_RESOURCE, DEFAULT_EMPLOYER_RESOURCE, DEFAULT_INSURANCE_RESOURCE);
    }

    public DataMerge(String clientsResource, String employerResource, String insuranceResource) {
        Objects.requireNonNull(clientsResource, "clientsResource");
        Objects.requireNonNull(employerResource, "employerResource");
        Objects.requireNonNull(insuranceResource, "insuranceResource");

        ObjectMapper mapper = new ObjectMapper();

        List<Client> clients = readResource(mapper, clientsResource, new TypeReference<>() {});
        Map<String, Employer> employers = readResource(mapper, employerResource, new TypeReference<>() {});
        Map<String, List<Integer>> insuranceRaw = readResource(mapper, insuranceResource, new TypeReference<>() {});

        this.clientsByEmployeeId = clients.stream()
                .collect(Collectors.toMap(Client::employeeID, Function.identity()));

        this.employersById = Map.copyOf(employers);

        this.insuranceByEmployerId = insuranceRaw.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> new Insurance(e.getKey(), e.getValue())));

        this.combine = client -> {
            Employer employer = Optional.ofNullable(employersById.get(client.employerID()))
                    .orElseThrow(() -> new IllegalStateException("Unknown employerID: " + client.employerID()));

            Insurance insurance = Optional.ofNullable(insuranceByEmployerId.get(client.employerID()))
                    .orElseThrow(() -> new IllegalStateException("Missing insurance for employerID: " + client.employerID()));

            int plan = insurance.planForLastName(client.lastName());

            return new CombinedClient(
                    client.firstName(),
                    client.lastName(),
                    client.employeeID(),
                    client.employerID(),
                    employer.companyName(),
                    plan
            );
        };
    }


    public Optional<CombinedClient> findCombinedByEmployeeId(String employeeId) {
        Objects.requireNonNull(employeeId, "employeeId");
        return Optional.ofNullable(clientsByEmployeeId.get(employeeId)).map(combine);
    }

    public int clientCount() {
        return clientsByEmployeeId.size();
    }

    private static <T> T readResource(ObjectMapper mapper, String resourcePath, TypeReference<T> type) {
        try (InputStream in = DataMerge.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + resourcePath);
            }
            return mapper.readValue(in, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse JSON resource: " + resourcePath, e);
        }
    }
}
