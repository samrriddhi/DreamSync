# 🚀 DreamSync

A collaborative project management platform built with **Spring Boot** that enables teams to manage projects, tasks, comments, file attachments, and real-time notifications securely.

---

## 📌 Features

### 🔐 Authentication & Security
- JWT Authentication
- Refresh Token Authentication
- Spring Security
- BCrypt Password Encryption
- Role-Based Authorization (Admin / User)
- Secure REST APIs

### 👥 User Management
- User Registration & Login
- Update User Profile
- User CRUD Operations

### 📁 Project Management
- Create, edit, and delete projects
- Project status tracking (Planning / In Progress / On Hold / Completed)
- Manage Project Members and roles
- Activity Tracking

### ✅ Task Management
- Create, Update & Delete Tasks
- Task Priority
- Task Status
- Due Dates
- Dependency Tracking with circular-dependency prevention
- Critical Path Analysis
- Delay impact analysis
- Blocked Task Detection
- Task effort estimates and project timeline
- Project risk analysis

### 💬 Collaboration
- Comments on Tasks
- Project team chat with persisted message history
- Activity Logs
- Real-Time project chat and task notifications using authenticated WebSockets

### 📂 File Management
- Upload, download, and delete task attachments (10 MB limit)
- Store File Metadata in PostgreSQL
- Local File Storage

### 📊 Dashboard
- User Dashboard
- Activity Feed
- Current User Activities

### 💻 Frontend

The React + TypeScript frontend is located in `frontend/`.

```bash
cd frontend
npm ci
npm run dev
```

The frontend runs on Vite's local development server and uses `http://localhost:8080` as its default backend URL. Set `VITE_API_URL` if your backend runs elsewhere.

## ⚙️ Run with Docker Compose

Copy `.env.example` to `.env`, replace the database password and JWT secret with strong values, and set the frontend origins for both REST and WebSocket access. Then run:

```bash
docker compose up --build
```

Docker Compose waits for PostgreSQL to become healthy before starting the backend and persists both PostgreSQL data and uploaded task attachments in named volumes. The API is available at `http://localhost:8080`.

To stop the services:

```bash
docker compose down
```

Your named volumes remain intact. To remove the database and uploaded files as well, use `docker compose down -v`.

## ⚙️ Required Environment Variables

The backend reads its database password and JWT signing key from environment variables:

- `DB_PASSWORD` — PostgreSQL password
- `JWT_SECRET` — a strong signing key of at least 32 characters
- `CORS_ALLOWED_ORIGINS` — comma-separated frontend origins allowed to call the REST API
- `WEBSOCKET_ALLOWED_ORIGINS` — comma-separated frontend origins allowed to connect over WebSocket

On Windows PowerShell, set them before starting Spring Boot:

```powershell
$env:DB_PASSWORD="your_postgres_password"
$env:JWT_SECRET="replace_with_a_long_random_secret_at_least_32_characters"
.\mvnw.cmd spring-boot:run
```

## ✅ Continuous Integration

GitHub Actions runs the frontend production build and backend Maven test suite on pushes and pull requests to `master`.

---

## 📖 API Documentation
- Swagger UI Integration

---

# 🛠 Tech Stack

### Backend
- Java 25
- Spring Boot 3
- Spring MVC
- Spring Data JPA
- Hibernate

### Security
- Spring Security
- JWT
- Refresh Tokens

### Database
- PostgreSQL

### Real-Time Communication
- Spring WebSocket
- STOMP
- SockJS

### Documentation
- Swagger / OpenAPI

### Build Tool
- Maven

---

# 📂 Project Structure

```
src
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── mapper
 ├── config
 ├── security
 ├── exception
 └── resources
```

---

# 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/samrriddhi/dreamsync.git
```

---

## Configure Database

Create a PostgreSQL database.

Update:

```
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dreamsync
spring.datasource.username=postgres
spring.datasource.password=your_password
```

---

## Run Application

```bash
mvn spring-boot:run
```

Application starts at:

```
http://localhost:8080
```

---

# 📖 API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

# 🔄 Authentication Flow

```
Register
     │
     ▼
Login
     │
     ├── Access Token
     └── Refresh Token
            │
            ▼
Access Token Expires
            │
            ▼
Refresh Token API
            │
            ▼
New Access Token
            │
            ▼
Logout
```

---



# 🔮 Future Improvements

- Redis Caching
- AWS Deployment
- Kafka Event Streaming
- Email Notifications
- Expand automated integration and end-to-end test coverage

---

# 👩‍💻 Author

**Samriddhi Chauhan**

GitHub:
https://github.com/samrriddhi

LinkedIn:
https://www.linkedin.com/in/samriddhi-chauhan/