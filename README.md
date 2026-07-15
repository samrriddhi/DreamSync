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
- Create Projects
- Manage Project Members
- Activity Tracking

### ✅ Task Management
- Create, Update & Delete Tasks
- Task Priority
- Task Status
- Due Dates
- Dependency Tracking
- Critical Path Analysis
- Blocked Task Detection

### 💬 Collaboration
- Comments on Tasks
- Activity Logs
- Real-Time Notifications using WebSockets

### 📂 File Management
- Upload Attachments
- Store File Metadata in PostgreSQL
- Local File Storage

### 📊 Dashboard
- User Dashboard
- Activity Feed
- Current User Activities

### 📖 API Documentation
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

- Docker
- Redis Caching
- AWS Deployment
- Kafka Event Streaming
- Email Notifications
- Unit & Integration Testing

---

# 👩‍💻 Author

**Samriddhi Chauhan**

GitHub:
https://github.com/samrriddhi

LinkedIn:
https://www.linkedin.com/in/samriddhi-chauhan/