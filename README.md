# PaySwitch — Digital Wallet & Payment Switch Engine

Console-only Java project for course **25CSE0202 — Programming in Java**, built by the 4-member Core team.

## Architecture

```text
Scanner CLI (ConsoleMenu / Main)
          |
          v
PaymentService ----> Account + Transaction (domain model)
     |                         |
     +--> ConcurrentTransferSimulator (ExecutorService workers)
     |
     +--> JDBC layer (DAO interfaces + Jdbc implementations)
              |
       DatabaseConfig --> MySQL (schema.sql)
```

The interactive program uses in-memory state by default, so the menu remains usable without MySQL. JDBC persistence is provided as a separate layer and `transferAtomicInDb` shows a database transaction.

## Team responsibilities

| Member | Module | Main files |
| --- | --- | --- |
| Mehul | Account & Payment Core | `model`, `exception`, `service/PaymentService.java` |
| Prince | Multithreading & Concurrency | `concurrency` |
| Keshav | Database & JDBC Persistence | `config`, `dao`, `database/schema.sql` |
| Armaan | Console UI & Reports | `ui`, `Main.java`, tests and documentation |

## Build, test, and run

Requires JDK 21 and Maven.

```sh
mvn clean test
mvn exec:java
```

Configure the optional local database with `PAYSWITCH_DB_URL`, `PAYSWITCH_DB_USER`, and `PAYSWITCH_DB_PASSWORD`. Run `database/schema.sql` in MySQL first. Defaults target `localhost:3306/payswitch_db`, user `root`, blank password.

## Viva concepts

- **Money:** `BigDecimal` and two-decimal validation avoid binary floating-point errors.
- **Encapsulation:** account fields are private; balance changes go through credit/debit methods.
- **Concurrency:** a synchronized service boundary makes transfer debit, credit, and audit logging atomic. It uses one lock, so opposing transfers cannot deadlock through inconsistent lock ordering. The simulator dispatches `Callable` jobs to a fixed `ExecutorService` and waits on their `Future`s; `AtomicInteger` tracks outcomes.
- **ACID / JDBC:** `PreparedStatement` binds values separately from SQL text. `JdbcPaymentRepository` disables auto-commit, locks both account rows in ascending ID order, updates balances and inserts the ledger row, then commits. SQL or runtime failure causes rollback.
- **Custom exceptions:** runtime exceptions express domain failures such as missing accounts and insufficient funds without obscuring the business operation with checked-exception plumbing.
- **Audit trail:** successful and failed in-memory payment attempts are recorded with UUIDs, timestamps, and failure reasons.
