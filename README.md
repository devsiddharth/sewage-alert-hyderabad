# 🚨 Sewage Alert Hyderabad

A cloud-native microservices-based platform designed to help citizens report sewage and sanitation issues while enabling authorities to track, manage, and resolve complaints efficiently.

---

## 📌 Project Overview

Sewage Alert Hyderabad is being developed to bridge the communication gap between citizens and municipal authorities.

The platform enables users to:

- Report sewage and drainage issues
- Upload complaint images
- Track complaint status in real time
- View complaint history
- Improve transparency in issue resolution
- Build a cleaner and healthier city through technology

---

# 🏗️ System Architecture

```
                React Frontend (In Progress)
                        │
                        ▼
               Spring Cloud Gateway
                        │
                        ▼
                Eureka Discovery Server
                        │
 ┌──────────────┬──────────────┬──────────────┐
 ▼            ▼              ▼              ▼
Auth Service  User Service  Complaint Service  Community Service (Upcoming)
        │             ▲
        └────Feign────┘
              ▲
              │
     Complaint Service
```

---

# 🚀 Tech Stack

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud OpenFeign
- Spring Data JPA
- Hibernate
- JWT Authentication

## Database

- MySQL

## Frontend

- React (Planned)

## Tools

- IntelliJ IDEA
- Maven
- Postman
- Git
- GitHub
- MySQL Workbench

---

# 📂 Microservices

## ✅ Discovery Server

Responsibilities

- Service Registration
- Service Discovery

Technology

- Eureka Server

---

## ✅ API Gateway

Responsibilities

- Centralized Routing
- Request Forwarding
- Gateway Entry Point

Technology

- Spring Cloud Gateway

---

## ✅ Auth Service

Responsibilities

- User Registration
- User Login
- JWT Generation
- Password Encryption
- Authentication

Features

- BCrypt Password Encoder
- JWT Authentication
- Global Exception Handling

Database

- sewagealert_auth

---

## ✅ User Service

Responsibilities

- User Profile Management
- Create Profile
- Update Profile
- Delete Profile
- Retrieve User Details

Features

- CRUD Operations
- OpenFeign Internal APIs

Database

- sewagealert_users

---

## ✅ Complaint Service

Responsibilities

- Create Complaint
- Complaint Tracking
- Complaint History
- Complaint Images
- Complaint Status Management

Features

- Complaint CRUD
- Complaint History
- Image Management
- OpenFeign Integration
- User Validation
- Exception Handling

Database

- sewagealert_complaints

---

# 🔄 Inter-Service Communication

Current Communication

```
Auth Service
        │
        ▼
User Service

Complaint Service
        │
        ▼
User Service
```

Implemented using

- Spring Cloud OpenFeign
- Eureka Service Discovery

---

# 🔐 Security

- JWT Authentication
- BCrypt Password Encryption
- Spring Security
- Role-Based Authorization (In Progress)

---

# 📊 Database Design

Each microservice owns its own database.

| Service | Database |
|----------|----------|
| Auth Service | sewagealert_auth |
| User Service | sewagealert_users |
| Complaint Service | sewagealert_complaints |

This follows the Database per Service pattern.

---

# 📡 API Flow

###
