# TrackFlow — Intelligent Issue & Sprint Management Platform

TrackFlow is a high-performance, backend-focused SaaS project management platform designed for engineering teams to coordinate sprints, trace issues, balance workloads, and manage document sharing. 

The project demonstrates clean code architecture, robust multi-tenant boundaries, secure stateless authorization protocols, and optimized database transactional boundaries.

---

## 🛠️ Technology Stack

* **Language:** Java 21
* **Framework:** Spring Boot 3.4
* **Security:** Spring Security (Stateless JWT, Token Rotation & Revocation)
* **Database:** PostgreSQL
* **ORM:** Spring Data JPA / Hibernate
* **API Documentation:** Swagger / OpenAPI 3
* **Testing:** JUnit 5, Mockito, H2 Database
* **Containerization:** Docker, Docker Compose
* **Frontend Demo:** HTML5, Vanilla CSS, JavaScript (served via Spring Boot)

---

## 🏗️ Architecture & Key Features

TrackFlow utilizes a strict **Enterprise Layered Architecture** with distinct boundaries separating controllers, services, database mappers, and repositories.

### 🔒 Enterprise Security & Auth
* **JWT Access & Refresh Tokens:** Implements stateless session authorization adapter with secure UUID refresh token generation.
* **Token Rotation & Revocation:** Blacklists tokens on logouts to prevent replay attacks and enforces token rotation.
* **Method-Level Authorizations:** Uses Spring Security annotations (`@PreAuthorize`) to lock down endpoints based on roles (`DEVELOPER`, `PROJECT_MANAGER`, `ORG_ADMIN`, `SUPER_ADMIN`).

### 🏛️ Multi-Tenant Bounds
* **Tenant Isolation:** Enforces strict logical boundaries at the service layer. Users, projects, and directories can only query resources belonging to their organization.
* **Access Control Checkers:** Validates ownership of comments, issues, and attachments before mutations.

### 📂 Decoupled Attachment Storage
* **Strategy Pattern:** Decouples the upload controller from storage backends through a `StorageService` interface.
* **Path Validation:** Prevents directory traversal attacks by validating path resolutions relative to root upload paths.

### 📊 Intelligent Analytics
* **Developer Workload Balancer:** Sums active estimation loads for assignees and suggests who has the lowest allocation first.
* **Sprint Health Scores:** Employs algorithmic computations rating sprint execution health (0-100) based on completion rate, penalized by active blocker bugs and overloaded developers.
* **Duplicate Detection:** Queries local databases for title pattern overlaps before issues registration to warn of duplication.

### 📝 Global Exceptions & Standard API Responses
* **Envelope Pattern:** Wraps all API responses in a standard `ApiResponse<T>` record keeping metadata and timestamps consistent.
* **Controller Advice:** Catches all exceptions globally and translates them to client-friendly errors.

---

## 🚀 Getting Started

### Prerequisites
* Java 21 JDK or higher
* Docker & Docker Compose
* Maven 3.9+ (or use the included wrapper `./mvnw.cmd`)

### 1. Build and Package
To build the project and package it into a single executable JAR file:
```bash
./mvnw clean package -DskipTests
```
This generates the packaged production archive inside the `target/` directory.

### 2. Local Development (In-Memory Database)
To run the server locally on H2 for testing without setting up databases:
```bash
# On Windows PowerShell:
$env:SPRING_PROFILES_ACTIVE="test"; .\mvnw.cmd spring-boot:run

# On Linux/macOS:
SPRING_PROFILES_ACTIVE=test ./mvnw spring-boot:run
```

Once running, open your browser and navigate to:
* **Interactive SPA Web Dashboard:** `http://localhost:8080/api/index.html`
* **Swagger API Explorer:** `http://localhost:8080/api/swagger-ui.html`

### 3. Production Deployment (Docker Compose)
To boot up the complete environment (Spring Boot + PostgreSQL container instance):
```bash
docker-compose up --build -d
```
The compose manifest includes a healthy database verification check before the application container initiates.

---

## 📂 Project Structure

```
src/main/java/com/trackflow/
├── config/             # Security configurations, swagger beans
├── controller/         # API REST controllers
├── dto/                # Data Transfer Objects & validation templates
├── entity/             # JPA entities & enums
├── exception/          # Custom exceptions & REST controller advice
├── mapper/             # Entity-DTO mapping translators
├── repository/         # JPA data repositories
├── security/           # JWT processing, filters, custom user details
├── service/            # Business boundary logic services
└── util/               # Constants & helpers
```
