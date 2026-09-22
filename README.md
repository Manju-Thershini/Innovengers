
````markdown
# 🛡️ StockShield

## Real-Time Concurrent Order Processing and Inventory Protection Platform

StockShield is a high-concurrency order processing platform designed for **e-commerce flash-sale scenarios**, where thousands of customers may attempt to purchase a limited-stock product simultaneously.

The primary engineering challenge is to process concurrent orders while guaranteeing that **inventory is never oversold or becomes negative**.

The system combines transactional processing, database-level concurrency control, bounded thread pools, idempotency, retry mechanisms, Dead Letter Queue (DLQ) handling, and a real-time operations dashboard.

---

## 🚀 Project Overview

Consider a flash sale where:

- Available inventory = 100 units
- Incoming customer requests = 5,000
- Concurrent processing threads = 100

StockShield processes these requests concurrently while ensuring:

```text
Successful Quantity ≤ Initial Inventory
````

and:

```text
Negative Inventory = 0
Overselling = 0
```

Instead of using a simple `synchronized` method, StockShield uses **database-level concurrency control and transactions**, making the design suitable for applications that may eventually run across multiple backend instances.

---

## 🎯 Problem Statement

High-traffic e-commerce systems face several challenges during flash sales:

* Thousands of users may purchase the same product simultaneously.
* Limited inventory can be oversold due to race conditions.
* Duplicate requests may create duplicate orders.
* Temporary failures may cause legitimate orders to fail.
* Repeated failures require controlled retry mechanisms.
* Permanently failed orders need reliable handling.
* Operations teams need real-time visibility into system activity.

StockShield addresses these problems through a concurrency-safe order processing architecture.

---

## ✨ Key Features

### 📦 Inventory Management

* Product creation and management
* Real-time inventory tracking
* Low-stock detection
* Out-of-stock handling
* Negative inventory prevention
* Transactional inventory updates

### ⚡ Concurrent Order Processing

* Bounded thread pool
* Concurrent order execution
* Database-level row locking
* Transaction management
* Race-condition protection
* Overselling prevention

### 🔐 Idempotency

Each order request can contain an idempotency key.

This prevents duplicate requests from creating multiple successful orders.

Example:

```text
Request 1 → IDEMPOTENCY-123 → SUCCESS
Request 2 → IDEMPOTENCY-123 → DUPLICATE
```

---

## 🔄 Order State Machine

Orders follow a controlled lifecycle:

```text
CREATED
   ↓
PROCESSING
   ↓
 ┌───────────────┐
 ↓               ↓
SUCCESS         FAILED
                  ↓
               RETRYING
                  ↓
              ┌───┴───┐
              ↓       ↓
           SUCCESS    DLQ
```

This makes order processing auditable and allows failed orders to be retried safely.

---

## 🔁 Retry Mechanism

Temporary processing failures are handled using:

* Bounded retries
* Retry attempt tracking
* Exponential backoff
* Maximum retry limits

Example:

```text
Attempt 1
   ↓
Failure
   ↓
Wait
   ↓
Attempt 2
   ↓
Failure
   ↓
Wait longer
   ↓
Attempt 3
   ↓
Failure
   ↓
Dead Letter Queue
```

---

## ☠️ Dead Letter Queue

Orders that cannot be successfully processed after the maximum retry attempts are moved to the **Dead Letter Queue (DLQ)**.

Administrators can:

* View failed orders
* Inspect failure reasons
* Review retry history
* Reprocess DLQ orders
* Track the complete order event history

---

# 🔥 Flash Sale Simulator

StockShield includes a dedicated **Flash Sale Simulator** for demonstrating the concurrency problem.

Administrators can configure:

* Product
* Initial inventory
* Number of simulated customers
* Number of concurrent threads
* Order quantity

### Example

```text
Inventory:          100
Customers:        5,000
Threads:             100
Quantity/request:      1
```

The simulator sends concurrent orders through the **same order-processing pipeline used by real customer requests**.

---

## 📊 Simulation Metrics

After a simulation, StockShield reports:

| Metric                   | Description                                   |
| ------------------------ | --------------------------------------------- |
| Total Requests           | Total simulated customer requests             |
| Successful Orders        | Orders successfully processed                 |
| Failed Orders            | Orders that failed                            |
| Out-of-Stock Orders      | Orders rejected due to insufficient inventory |
| Retry Count              | Number of retry attempts                      |
| DLQ Orders               | Orders moved to DLQ                           |
| Current Inventory        | Remaining product inventory                   |
| Processing Time          | Total simulation processing time              |
| Requests/Second          | Approximate system throughput                 |
| Duplicate Requests       | Duplicate requests prevented                  |
| Overselling Count        | Number of overselling incidents               |
| Negative Inventory Count | Number of negative inventory incidents        |

The most important system invariant is:

```text
Negative Inventory = 0
```

and:

```text
Successful Quantity ≤ Initial Inventory
```

---

# 📡 Real-Time Operations Dashboard

StockShield provides a React-based operations dashboard for monitoring the order processing system.

The dashboard displays:

* Total orders
* Processing orders
* Successful orders
* Failed orders
* Retry count
* DLQ count
* Current inventory
* Low-stock products
* Out-of-stock products
* Order throughput
* Recent order activity
* Retry activity
* DLQ activity

Live updates are delivered using **Server-Sent Events (SSE)**.

---

## 📈 Dashboard Visualizations

The operations dashboard includes:

* Orders over time
* Successful vs failed orders
* Inventory consumption
* Retry statistics
* Order status distribution
* Live order activity

Example activity feed:

```text
ORD-1001 → SUCCESS
ORD-1002 → PROCESSING
ORD-1003 → OUT_OF_STOCK
ORD-1004 → RETRYING 2/3
ORD-1005 → MOVED TO DLQ
```

---

# 👤 Customer Features

Customers can:

* Browse products
* View product availability
* Place orders
* Track order status
* View order history

---

# 👨‍💻 Admin / Operations Features

Administrators can:

* Manage products
* Update inventory
* View all orders
* Inspect order details
* Monitor retries
* Manage DLQ orders
* Reprocess failed orders
* Run flash-sale simulations
* Monitor system statistics
* Observe live system activity

---

# 🏗️ System Architecture

```text
                     ┌──────────────────────┐
                     │     React Frontend   │
                     │   Operations Panel   │
                     └──────────┬───────────┘
                                │
                         REST API / SSE
                                │
                                ▼
                  ┌──────────────────────────┐
                  │     Spring Boot API      │
                  └────────────┬─────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
        Order Service    Inventory Service   Event Service
              │                │                │
              ▼                ▼                ▼
        Thread Pool       Transaction       Audit Events
              │                │
              └────────────────┘
                       │
                       ▼
              PostgreSQL Database
                       │
              ┌────────┴────────┐
              │                 │
        Row-Level Locking    Constraints
              │                 │
              └────────┬────────┘
                       ▼
              Inventory Protection
```

---

# 🔒 Concurrency Control

Concurrency protection is the core engineering component of StockShield.

When multiple threads attempt to purchase the same product, the application performs the inventory operation inside a database transaction.

A row-level lock is acquired for the relevant product inventory.

Conceptually:

```text
Thread A ──┐
Thread B ──┤
Thread C ──┤──→ PostgreSQL
Thread D ──┤
Thread E ──┘
                │
                ▼
          Product Row Lock
                │
                ▼
        Check Current Stock
                │
          ┌─────┴─────┐
          │           │
       Stock > 0   Stock = 0
          │           │
          ▼           ▼
      Decrement    OUT_OF_STOCK
          │
          ▼
       Commit
```

This prevents two concurrent transactions from reading the same inventory value and both successfully decrementing it.

The system does **not** rely solely on Java-level synchronization because that would only protect threads within one JVM instance.

Database-level locking provides protection across multiple application instances accessing the same database.

---

# 🗄️ Database Design

PostgreSQL is used as the primary relational database.

Core entities include:

```text
products
orders
order_events
dead_letter_orders
idempotency_keys
inventory_reservations
```

### Products

Stores:

* Product ID
* Product name
* Price
* Inventory quantity
* Low-stock threshold
* Timestamps

### Orders

Stores:

* Order ID
* Customer ID
* Product ID
* Quantity
* Order status
* Retry count
* Idempotency key
* Failure reason
* Timestamps

### Order Events

Stores the complete lifecycle of an order.

Example:

```text
CREATED
PROCESSING
FAILED
RETRYING
SUCCESS
```

### Dead Letter Orders

Stores permanently failed orders for administrative inspection and reprocessing.

---

# 🧰 Technology Stack

## Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Maven
* REST APIs
* Server-Sent Events
* Transaction Management

## Frontend

* React
* Vite
* Axios
* Recharts

## Infrastructure

* Docker
* Docker Compose

## Development

* Git
* GitHub
* VS Code

---

# 📁 Project Structure

```text
StockShield/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   │
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
├── database/
│
├── docker-compose.yml
│
└── README.md
```

---

# 🧪 Testing Strategy

StockShield is designed to test both normal functionality and high-concurrency scenarios.

### Functional Tests

* Successful order
* Out-of-stock order
* Invalid order
* Duplicate request
* Retry
* DLQ insertion
* DLQ reprocessing
* Transaction rollback

### Concurrency Tests

Example:

```text
Initial Inventory = 50
Concurrent Requests = 1,000
```

Expected:

```text
Successful Quantity ≤ 50
Remaining Inventory ≥ 0
Overselling = 0
Negative Inventory = 0
```

The concurrency tests are intended to demonstrate that inventory protection continues to hold even when hundreds or thousands of requests arrive simultaneously.

---

# 🐳 Running with Docker

Start the infrastructure:

```bash
docker compose up -d
```

Check running containers:

```bash
docker compose ps
```

Stop the infrastructure:

```bash
docker compose down
```

---

# ▶️ Running the Backend

Navigate to the backend:

```bash
cd backend
```

Run:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

# ▶️ Running the Frontend

Navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will be available at:

```text
http://localhost:5173
```

---

# 🔬 Example Flash Sale Test

Configure:

```text
Product:       Limited Edition Product
Inventory:     100
Customers:     5,000
Threads:       100
Quantity:      1
```

Expected behavior:

```text
5,000 concurrent requests
          ↓
Bounded Thread Pool
          ↓
Transactional Order Processing
          ↓
Database Row-Level Lock
          ↓
Inventory Validation
          ↓
Maximum 100 successful purchases
          ↓
Remaining requests → OUT_OF_STOCK
```

The system should maintain:

```text
Negative Inventory = 0
Overselling = 0
```

---

# 🎯 Engineering Goals

StockShield focuses on demonstrating real-world backend engineering concepts rather than basic CRUD functionality.

Key engineering goals include:

* Concurrent request processing
* Database-level concurrency control
* Transactional consistency
* Idempotent APIs
* Failure recovery
* Retry strategies
* Dead Letter Queue processing
* Real-time monitoring
* Performance measurement
* Automated concurrency testing
* Scalable application architecture

---

# 🔮 Future Enhancements

Potential future improvements include:

* Redis-based distributed coordination
* Kafka/RabbitMQ event streaming
* Horizontal backend scaling
* Kubernetes deployment
* AWS deployment
* Distributed tracing
* Prometheus metrics
* Grafana monitoring
* Rate limiting
* Circuit breakers
* Load testing with dedicated performance tools

These technologies are intentionally kept optional so that the core system remains focused on **correctness, concurrency, and inventory protection**.

---

# 📌 Core Invariants

StockShield is built around two critical invariants:

### 1. Inventory must never become negative

```text
Inventory >= 0
```

### 2. Successful quantity must never exceed initial inventory

```text
Successful Quantity <= Initial Inventory
```

These invariants are validated through both application logic and automated concurrency tests.

---


---

#
