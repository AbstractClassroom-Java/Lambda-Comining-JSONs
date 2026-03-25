# LambdaCombiningJson

Small teaching demo that shows how to **combine multiple JSON documents** into a single in-memory view using **Java records**, **Maps**, and **lambda expressions**.

The core idea is:

- Load three JSON documents (`clients`, `employers`, `insurance`).
- Store each document in an in-memory structure optimized for lookups (a `Map`).
- Build a *combined* view (`CombinedClient`) **only when** someone asks for a specific `employeeID`.

## Data files

At runtime, the app reads JSON files from the **classpath**:

- `src/main/resources/data/clients.json`
- `src/main/resources/data/employer.json`
- `src/main/resources/data/insurance.json`

A Python generator exists under `data/generate_jsons.py` to regenerate `data/*.json`.
(Then copy them into `src/main/resources/data/` so Java can load them.)

### Schemas

#### `clients.json`
A JSON array (list) of clients:

- `firstName`
- `lastName`
- `employeeID`
- `employerID`

#### `employer.json`
A JSON object (dictionary) keyed by `employerID`:

```json
{
  "EMP001": { "companyName": "Acorn Analytics", "employerID": "EMP001" },
  "EMP002": { "companyName": "Blue Ridge Logistics", "employerID": "EMP002" }
}
```

#### `insurance.json`
A JSON object keyed by `employerID`.
The value is a 2-element array of integers:

- index `0`: the plan used when last name begins with **A–M**
- index `1`: the plan used when last name begins with **N–Z**

```json
{
  "EMP001": [1234, 2345],
  "EMP002": [1567, 2890]
}
```

## Java model (records)

Records live under `io.github.nathanjrussell.model`:

- `Client(firstName, lastName, employeeID, employerID)`
- `Employer(companyName, employerID)`
- `Insurance(employerID, plans)`
  - Contains the rule method `planForLastName(lastName)` implementing the A–M vs N–Z choice.
- `CombinedClient(...)`
  - Stores: client identity + employer name/id + chosen insurance plan
  - Overrides `toString()` to print neatly for demos.

## The merge: `DataMerge`

`io.github.nathanjrussell.service.DataMerge` is the teaching centerpiece.

### What it loads

On construction, it loads and stores these lookup maps:

- `clientsByEmployeeId: Map<String, Client>`
- `employersById: Map<String, Employer>`
- `insuranceByEmployerId: Map<String, Insurance>`

### The key lambda / mapper

The merge is expressed as a function (lambda):

- `Function<Client, CombinedClient> combine`

That function:

1. Looks up the employer using `client.employerID()`
2. Looks up insurance using the same `employerID`
3. Chooses the plan using `insurance.planForLastName(client.lastName())`
4. Returns a new `CombinedClient(...)`

### On-demand merge with `Optional.map`

This is the "build it only when asked" part:

- `findCombinedByEmployeeId(String employeeId)` returns `Optional<CombinedClient>`

Internally it uses an `Optional` mapper:

- `Optional.ofNullable(clientsByEmployeeId.get(employeeId)).map(combine)`

So:

- If no client exists for the `employeeId`, you get `Optional.empty()`.
- If the client exists, the `combine` lambda runs and creates the `CombinedClient`.

## Run

From the project root:

```zsh
mvn test
```

To run the demo main (defaults to `E00001`):

```zsh
mvn -q -DskipTests package
java -cp target/classes:target/dependency/* io.github.nathanjrussell.Main
```

Or pass an employee id:

```zsh
java -cp target/classes:target/dependency/* io.github.nathanjrussell.Main E00042
```

## Where to look in code

- `src/main/java/io/github/nathanjrussell/service/DataMerge.java`
  - shows the maps and the `Function<Client, CombinedClient>` lambda
  - shows `Optional.map(...)` used as a mapper to apply the lambda
- `src/main/java/io/github/nathanjrussell/model/Insurance.java`
  - shows the A–M vs N–Z plan selection logic
