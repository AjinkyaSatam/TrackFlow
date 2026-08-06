# 🚀 TrackFlow — Intelligent Issue & Sprint Management Platform

TrackFlow is a SaaS-focused enterprise backend application designed for software teams to plan sprints, track issues, assign developers, and monitor team workload. 

This project is built from scratch using clean code principles, SOLID design, and industry-standard backend patterns (not a Jira clone, but an original implementation). It showcases skills suitable for professional Software Developer and Backend Developer interviews.

---

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.4
* **Security:** Spring Security (Stateless JWT Authentication, Token Rotation & Revocation)
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA (Hibernate)
* **Validation:** Jakarta Validation (JSR-380)
* **Documentation:** Swagger/OpenAPI 3
* **Testing:** JUnit 5, Mockito, H2 In-Memory Database (Test Profile)
* **Containerization:** Docker, Docker Compose
* **Build Tool:** Maven

---

## 🏗️ Architecture & Core Design Patterns

TrackFlow uses a strict **Enterprise Layered Architecture** with clear boundaries:

```
Controller  ──(DTOs)──>  Service  ──(Entities)──>  Repository  ──(SQL)──>  Database
```

### Key Design Implementation:
1. **The Envelope Pattern (`ApiResponse`):** Every API endpoint returns a standardized JSON structure with consistent metadata (status, message, payload, and audit timestamp).
2. **Global Exception Handling (`@RestControllerAdvice`):** Centralizes error logging and response generation, completely eliminating cluttered `try-catch` blocks from controller files.
3. **Database Decoupling (DTO & Mapper Pattern):** Domain entities never escape the service boundary. High-performance, compile-safe manual mappers translate data to DTOs for client consumption.
4. **Soft Delete (`BaseEntity`):** Automatic auditable timestamps (`createdAt`, `updatedAt`) and soft-deletion flags prevent actual record destruction, retaining referential integrity.
5. **Workflow State Machine:** Enforces valid transitions for issues (e.g., `OPEN` can only go to `IN_PROGRESS`) at the domain level.
6. **Multi-Tenancy Access Control:** Restricts user data views and directory searches within their own organizational boundaries to prevent cross-tenant security leaks.

---

## 📂 Project Structure

```
src/main/java/com/trackflow/
├── config/             # Security rules, Swagger docs configuration
├── controller/         # REST Controllers (Auth, Users, Health checks)
├── dto/                # Data Transfer Objects (Payload validation)
├── entity/             # JPA Entities & Enums (Database schema)
├── exception/          # Custom exceptions & Global Exception Handler
├── mapper/             # Entity-DTO translator classes
├── repository/         # Spring Data JPA repositories
├── security/           # JWT, UserDetails adapter, Security filters
├── service/            # Core business logic (Services & Implementations)
└── util/               # Constants & general utility helpers
```

---

## 📌 Development Roadmap

- [x] **Step 1:** Project foundation, standard responses, global exception handlers, and database setups.
- [x] **Step 2:** Database schema design and JPA relationships (13 entities, 7 enums).
- [x] **Step 3:** Database access layer (Spring Data JPA repositories, JPQL optimizations).
- [x] **Step 4:** JWT Authentication, refresh token engine, and Spring Security configurations.
- [x] **Step 5:** User Profile management, credentials update, and member lookup directories.
- [ ] **Step 6:** Organization Management Module.
- [ ] **Step 7:** Project Management Module.
- [ ] **Step 8:** Sprint Lifecycle & Planning Module.
- [ ] **Step 9:** Issue Lifecycle Workflow & Assignment Module.
- [ ] **Step 10:** Comment Threading & Mention Parser.
- [ ] **Step 11:** File Storage Interface (Local Storage / MinIO / AWS S3).
- [ ] **Step 12:** Audit Logs & Event-Driven Notification System.
- [ ] **Step 13:** Original Algorithms (Duplicate Detection, Workload Balancer, Sprint Health Score).
- [ ] **Step 14:** Unit & Integration Testing Suite.
- [ ] **Step 15:** Production Docker builds & deployment setup.

---

## 🚀 Getting Started

### Prerequisites
* Java 21 JDK or higher
* Docker & Docker Compose
* Maven 3.9+ (or use the included wrapper `./mvnw`)

### 1. Boot up the Database
Start the local PostgreSQL container using Docker Compose:
```bash
docker-compose up -d
```

### 2. Run the Application
Start the Spring Boot backend server:
```bash
./mvnw spring-boot:run
```
The server will boot on `http://localhost:8080/api`.

### 3. Check App Health
Verify the health check endpoint:
```
GET http://localhost:8080/api/health
```

### 4. Interactive API Documentation
Open Swagger UI to test and document all API endpoints:
```
http://localhost:8080/api/swagger-ui.html
```
