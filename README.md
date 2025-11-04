# 3SemExam – Candidate Matcher API

Backend exam project for 3rd semester.  
Exposes a secured REST API for managing **candidates**, **skills**, and a **report** based on external skill popularity stats.

**Tech stack**

- Java 17
- Javalin 6
- Hibernate 6 + HikariCP
- PostgreSQL
- JWT security (TokenSecurity library)
- JUnit 5, RestAssured, Testcontainers, Hamcrest
- Lombok, Jackson

---

## 1. Running the application

### 1.1. Database

Create a local PostgreSQL database:

```sql
CREATE DATABASE "3SemExam";
```

Configure `src/main/resources/config.properties`:

```properties
DB_NAME=3SemExam
ISSUER=3-sem-exam-api
TOKEN_EXPIRE_TIME=1800000
SECRET_KEY=Tcl0bGc4EDlwd0LoGmYphFlxphyzYB3kqDXTxDK/H4o=
```

### 1.2. Build & run

```bash
mvn clean package
java -jar target/app-shade.jar
```

The API will be available at:

- Base URL: `http://localhost:7070/api`
- Route overview: `http://localhost:7070/routes`

---

## 2. Architecture overview

**Packages**

- `app.config` – Javalin + Hibernate setup, DB populate
- `app.entities` – `Candidate`, `Skill`, `CandidateSkill`, `SkillCategory`
- `app.dtos` – request/response DTOs and report DTOs
- `app.daos` – `CandidateDAO`, `SkillDAO`
- `app.controllers` – `CandidateController`, `SkillController`, `ReportController`
- `app.routes` – `CandidateRoutes`, `SkillRoutes`, `ReportRoutes`, security routes
- `app.services` – `SkillStatsService`, `CandidateReportService`
- `app.exceptions` – `ApiException`, `DatabaseException`
- `app.utils` – `Utils` (config + JSON helper)
- `app.security.*` – users, roles, JWT, access control
- `app.Main` – application entry point

---

## 3. Diagrams (docs/)

All diagrams are stored under `docs/`:

- `docs/domain-model.puml` – domain model for `Candidate`, `Skill`, `CandidateSkill`.
- `docs/candidate-flow.puml` – sequence: login → create candidate → create skill → assign skill.
- `docs/erd.png` – ERD for the PostgreSQL database schema.


---

## 4. Security

JWT-based security using TokenSecurity.

**Flow**

1. `POST /auth/register` – create user (gets USER role)
2. `POST /auth/login` – returns JWT with roles
3. `POST /auth/user/addrole` – add role (e.g. `"admin"`) to current user
4. Protected endpoints:
    - `GET /protected/user_demo` (USER/ADMIN)
    - `GET /protected/admin_demo` (ADMIN)

**Roles**

- USER – read candidates/skills, user demo, reports
- ADMIN – all of USER + create/update/delete + assign skills

---

## 5. API endpoints (summary)

Base URL: `http://localhost:7070/api`

### 5.1. Auth & health

- `GET /auth/healthcheck`
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/user/addrole`
- `GET /protected/user_demo`
- `GET /protected/admin_demo`

### 5.2. Skills (`/skills`)

- `GET /skills` – list (USER)
- `GET /skills/{id}` – details (USER)
- `POST /skills` – create (ADMIN)
- `PUT /skills/{id}` – update (ADMIN)
- `DELETE /skills/{id}` – delete (ADMIN)

### 5.3. Candidates (`/candidates`)

- `GET /candidates` – list (USER)  
  Optional `category` query: `PROG_LANG`, `DB`, `DEVOPS`, `FRONTEND`, `TESTING`, `DATA`, `FRAMEWORK`
- `GET /candidates/{id}` – details + enriched skills from external API (USER)
- `POST /candidates` – create (ADMIN)
- `PUT /candidates/{id}` – update (ADMIN)
- `DELETE /candidates/{id}` – delete (ADMIN)
- `PUT /candidates/{candidateId}/skills/{skillId}` – link existing skill (ADMIN)

### 5.4. Reports (`/reports`)

- `GET /reports/candidates/top-by-popularity` (USER)  
  Uses `CandidateReportService` + `SkillStatsService` and calls external API `.../skills/stats?slugs=...`.

---

## 6. Error handling & logging

- Central handlers in `ApplicationConfig`
- Custom exceptions: `ApiException`, `DatabaseException`
- JSON error responses via `Utils.convertToJsonMessage`
- Logging with Logback to console and `logs/*.log`

---

## 7. Testing

Run all tests:

```bash
mvn test
```

**Route tests**

- `routes.SecurityRoutesTest` – auth flow + protected endpoints
- `routes.CandidateRoutesTest` – candidate CRUD, filter, report, assign skill, negative category
- `routes.SkillRoutesTest` – skill CRUD

**DAO tests**

- `daos.CandidateDAOTest` – `findAll`, `create`, `find`, `findBySkillCategory`, `delete`

**Helpers**

- `populators.Populator` – seeds users, candidates, skills in Testcontainers DB and cleans up between tests.
- HTTP file: `src/test/resources/CandidateMatcherApplication.http` – manual test of all flows + negative case.

---
