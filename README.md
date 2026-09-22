# StockShield
## Real-Time Concurrent Order Processing and Inventory Protection Platform

Competition-level starter implementation for a flash-sale order-processing challenge.

### Architecture
React/Vite dashboard → REST API + SSE → Spring Boot service layer → bounded ExecutorService → transactional order service → PostgreSQL row lock (`SELECT ... FOR UPDATE` via JPA `PESSIMISTIC_WRITE`). Flyway owns the schema.

### Why overselling is prevented
Every successful purchase executes inside one database transaction. The product row is locked with a pessimistic write lock before checking inventory. Concurrent transactions therefore serialize on the same product row. The check and decrement happen in the same transaction, and the PostgreSQL constraint also rejects negative inventory. This is safe across multiple application instances because the lock lives in PostgreSQL rather than in a JVM monitor.

### State machine
CREATED → PROCESSING → SUCCESS / OUT_OF_STOCK
PROCESSING → RETRYING → PROCESSING … → DLQ

### Modules
- Product/inventory management
- Concurrent order submission with bounded executor
- Idempotency keys
- Pessimistic inventory locking
- Retry + exponential backoff
- Dead-letter persistence + reprocessing endpoint
- Order event audit trail
- Flash Sale Simulator
- SSE live activity stream
- React operations dashboard
- PostgreSQL + Flyway

### Run locally
1. Install Java 21, Maven, Node 22 and Docker.
2. Start PostgreSQL: `docker compose up -d postgres`
3. Backend: `cd backend && mvn spring-boot:run`
4. Frontend: `cd frontend && npm install && npm run dev`
5. Open `http://localhost:5173`.
6. Swagger: `http://localhost:8080/swagger-ui.html`.

Or run the complete stack: `docker compose up --build` and open `http://localhost:5173`.

### Demo
Default product is `FLASH-100` with inventory 100. Run a simulation with 5,000 customers and 100 threads. The key invariant to demonstrate is `inventory >= 0`. Because this starter uses asynchronous processing, wait for all submitted orders to settle before treating final counters as the benchmark result.

### Important engineering note
The simulation endpoint is intentionally simple for a competition starter. For a production-grade benchmark, add Testcontainers-based PostgreSQL integration tests, a dedicated simulation run ID, per-run metrics, Micrometer timers, and an explicit wait/barrier for terminal states. The current database locking strategy is the critical correctness mechanism.

### API
- `GET /api/products`
- `POST /api/products`
- `PUT /api/products/{id}/inventory?quantity=100`
- `POST /api/orders`
- `GET /api/orders`
- `GET /api/stats`
- `GET /api/stream` (SSE)
- `GET /api/dlq`
- `POST /api/dlq/{id}/reprocess`
- `POST /api/simulate`

### Next competition upgrades
Redis rate limiting, RabbitMQ/Kafka event transport, Testcontainers concurrency tests, Prometheus/Grafana, authentication/RBAC, per-simulation metrics, reservation expiry, and Kubernetes horizontal scaling can be added after correctness is validated.
