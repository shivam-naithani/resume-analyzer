# Resume Analyzer

A web application that compares a resume against a job description and
returns a quantitative match score, missing keywords, and improvement
suggestions — helping candidates get past ATS keyword filters before they
submit.

Monorepo: `client/` (React + Vite) and `server/` (Java 21 + Spring Boot 3.3 + MySQL).

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React + Vite |
| Backend | Spring Boot 3.3 (Java 21) |
| Data access | Spring Data JPA |
| Security | Spring Security + JWT |
| Schema migrations | Flyway |
| Database | MySQL |
| PDF parsing | Apache PDFBox |
| Matching algorithm | TF-IDF + cosine similarity |

## Prerequisites

- Java 21
- Maven
- A running MySQL server
- Node.js (for the client)

## Setup

### 1. Database
```sql
CREATE DATABASE resume_analyzer_dev;
CREATE USER 'resume_app'@'localhost' IDENTIFIED BY 'choose_a_password';
GRANT ALL PRIVILEGES ON resume_analyzer_dev.* TO 'resume_app'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Server
```bash
cd server
export DB_NAME=resume_analyzer_dev
export DB_USER=resume_app
export DB_PASSWORD=choose_a_password
export JWT_SECRET=a_long_random_string_at_least_32_characters

mvn clean install
mvn spring-boot:run      # starts on http://localhost:8080
```

Flyway applies the schema migrations automatically on startup.

### 3. Client
```bash
cd client
npm install
cp .env.example .env
npm run dev               # http://localhost:5173
```

## API

| Method & route | Description | Auth required |
|---|---|---|
| `GET /api/health` | Health check | No |
| `POST /api/auth/signup` | Create an account | No |
| `POST /api/auth/login` | Log in, returns a JWT | No |
| `POST /api/resumes` | Upload a resume (PDF) | Yes |
| `POST /api/analyses` | Score a resume against a job description | Yes |
| `GET /api/analyses` | List the caller's analysis history | Yes |
| `GET /api/analyses/{id}` | Get one analysis | Yes |

Authenticated requests need an `Authorization: Bearer <token>` header, using
the token returned from `/api/auth/login`.

## Running tests

```bash
cd server
mvn test
```

Covers the scoring engine (TF-IDF + cosine similarity) and PDF text
extraction in isolation, independent of the web/database layers.

## Project structure

```
server/
  config/        — Spring Security configuration
  controller/    — REST endpoints
  converter/     — JPA attribute converters (JSON columns)
  dto/           — request/response payloads
  entity/        — JPA entities (User, Resume, Analysis)
  repository/    — Spring Data JPA repositories
  security/      — JWT generation/validation
  service/       — business logic (auth, PDF extraction, scoring)
client/
  src/pages/       — top-level views
  src/components/  — reusable UI components
  src/services/    — API client functions
  src/hooks/       — custom React hooks
```
