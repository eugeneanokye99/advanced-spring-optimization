# Repository and Transaction Documentation

## Repository Structure and Query Logic

The ShopJoy system utilizes **Spring Data JPA** for the data access layer. This provides a robust and standardized way to interact with the PostgreSQL database.

### Repository Architecture
- **Location**: `src/main/java/com/shopjoy/repository/`
- **Pattern**: Interface-based repositories extending `JpaRepository`.
- **Naming Convention**: `[EntityName]Repository` (e.g., `ProductRepository`, `OrderRepository`).

### Query Logic
The system uses several query implementation strategies:

1.  **Repository Method Naming Convention**: Simple queries are derived from method names (e.g., `findByUserId` in `CartItemRepository`).
2.  **Custom JPQL Queries**: Complex queries that require specific joins or filters use the `@Query` annotation.
    - Example: `ProductRepository.searchByKeywords` uses `@Query` for full-text search simulation or complex filtering.
3.  **Entity Mapping**: All entities are mapped using JPA annotations (`@Entity`, `@Table`, `@Id`, `@Column`).
4.  **Batch Fetching**: Performance is optimized using Hibernate's `@BatchSize` on collection mappings and entities to prevent N+1 select problems.

---

## Transaction Handling and Rollback Strategies

ShopJoy ensures data integrity through a combination of declarative transaction management and Aspect-Oriented Programming (AOP) monitoring.

### Transaction Management
- **Declarative Transactions**: The system uses `@Transactional` at the service implementation layer (`com.shopjoy.service.impl`).
- **Standard Configuration**:
    - **Read-only Transactions**: Methods that only fetch data use `@Transactional(readOnly = true)` for database optimization.
    - **Write Transactions**: Methods that modify data use `@Transactional(readOnly = false)` (or default).
- **Advanced Configuration**:
    - Critical operations like order placement in `OrderServiceImpl.placeOrder` use specific attributes:
        - `isolation = Isolation.SERIALIZABLE`: Prevents race conditions during stock deduction.
        - `propagation = Propagation.REQUIRED`: Ensures the operation runs within a transaction context.
        - `rollbackFor = Exception.class`: Ensures rollback on any checked or unchecked exception.

### Rollback Strategy
Rollbacks occur automatically in the following scenarios:
1.  **Uncaught RuntimeExceptions**: Standard Spring behavior.
2.  **Explicit Rollback**: Configured via `rollbackFor` in critical business methods.
3.  **Atomic Operations**: Order creation, inventory deduction, and payment status updates are grouped into single transactions. If any part fails, the entire transaction is rolled back.

### Monitoring (TransactionAspect)
A custom AOP aspect, `TransactionAspect`, monitors all transactional methods:
- **Logging**: Captures `TRANSACTION START`, `COMMIT`, and `ROLLBACK` events with timestamps.
- **Performance Auditing**: Detects and warns about "Long Running Transactions" (exceeding 5 seconds).
- **Error Tracking**: Logs full exception details during rollback events for easier debugging.
