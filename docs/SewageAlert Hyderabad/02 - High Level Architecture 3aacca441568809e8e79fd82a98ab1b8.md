# 02 - High Level Architecture

## High-Level System Architecture

The High-Level Architecture defines the overall structure of the Sewage Alert Hyderabad platform and illustrates how different software components interact with each other. The application follows a Microservices Architecture where independent services communicate through an API Gateway and are registered with a Eureka Discovery Server. This architecture improves scalability, maintainability, and allows each service to be developed and deployed independently.

---

## Architecture Goals

The architecture of Sewage Alert Hyderabad is designed to achieve the following goals:

- Build a modular and scalable system.
- Separate business functionalities into independent microservices.
- Secure APIs using Spring Security and JWT.
- Enable service discovery through Eureka Server.
- Route all client requests through a centralized API Gateway.
- Simplify future expansion of the platform.
- Improve maintainability and code organization.
- Support independent deployment of services.

---

## System Components

### 🌐 **Frontend (React + TypeScript)**

<aside>
💡

**Purpose**

- User Interface

**Users**

- 👤 Citizen
- 🏛️ Authority
- ⚙️ Admin

**Responsibilities**

- Report Complaints
- Track Complaint Status
- Register for Events
- Read Articles
- View Infrastructure Information
</aside>

### 🚪 API Gateway

<aside>
💡

**Purpose**

- Single Entry Point

**Responsibilities**

- Route Requests
- Validate JWT
- Apply Security Filters
- Handle CORS
</aside>

### 🔍 Eureka Discovery Server

<aside>
💡

**Purpose**

- Service Discovery

**Responsibilities**

- Register Services
- Discover Services
- Enable Dynamic Communication
</aside>

### 🔐 **Authentication Service**

<aside>
💡

**Purpose**

- User Authentication

**Responsibilities**

- Register
- Login
- Generate JWT
- Encrypt Passwords
- Validate Tokens
</aside>

### 👤 **User Service**

<aside>
💡

**Purpose**

- User Management

**Responsibilities**

- User Profiles
- Preferences
- Role Information
</aside>

### 🚨 **Complaint Service**

<aside>
💡

**Purpose**

- Complaint Management

**Responsibilities**

- Register Complaints
- Store Images
- GPS Location
- Status Tracking
- Complaint History
</aside>

### 🌱 **Community Service**

<aside>
💡

**Purpose**

- Citizen Awareness & Community Engagement

**Responsibilities**

- NGO Events
- Educational Articles
- Volunteer Management
- Pipeline Information
- Sewage Treatment Plants
- Lake Restoration
</aside>

### 📧 **Notification Service**

<aside>
💡

**Purpose**

- Communication

**Responsibilities**

- Complaint Updates
- Event Reminders
- Email Notifications
- System Alerts
</aside>

### 🗄️ **MySQL Database**

<aside>
💡

**Purpose**

- Central Data Storage *(Version 1)*

**Stores**

- Users
- Complaints
- Events
- Articles
- NGOs
- Pipelines
- Lakes
- Notifications
</aside>

---

## System Architecture Diagram

![SewageAlert_High_Level_Architecture.drawio.png](02%20-%20High%20Level%20Architecture/SewageAlert_High_Level_Architecture.drawio.png)

---

## Request Flow

The following steps describe how a typical request is processed within the SewageAlert Hyderabad platform.

### Complaint Submission Flow

1. The citizen logs into the application using their credentials.
2. The citizen fills in the complaint form, uploads an image, and shares their GPS location.
3. The React frontend sends the complaint request to the **Spring Cloud API Gateway**.
4. The API Gateway validates the JWT token and routes the request to the **Complaint Service**.
5. The Complaint Service processes the request, validates the data, and stores the complaint in the database.
6. The Complaint Service communicates with the **Notification Service** to send a complaint submission confirmation to the citizen.
7. The Complaint Service returns a success response through the API Gateway.
8. The React frontend displays the complaint reference number and updated complaint status to the citizen.

---

### Complaint Status Update Flow

1. An authority logs into the system.
2. The authority views the assigned complaints.
3. The authority updates the complaint status and adds remarks if necessary.
4. The Complaint Service saves the updated information.
5. The Notification Service sends an email notification to the citizen informing them of the updated complaint status.
6. The citizen can view the latest status from the **My Complaints** page.

---

### Awareness Event Registration Flow

1. The citizen browses the list of awareness events.
2. The citizen selects an event and clicks **Register**.
3. The request is sent through the API Gateway to the **Community Service**.
4. The Community Service stores the registration details.
5. The Notification Service sends a registration confirmation email.
6. The registered event appears in the citizen's profile.

---

## Benefits of the Architecture

The High-Level Architecture of Sewage Alert Hyderabad is designed to support scalability, maintainability, and future expansion. By adopting a microservices-based architecture, each service can be developed, tested, deployed, and maintained independently while communicating through well-defined REST APIs.

### Scalability

- Individual services can be scaled independently based on demand.
- New services can be added without affecting existing components.

### Maintainability

- Business functionalities are separated into dedicated microservices.
- Code is easier to understand, maintain, and extend.

### Security

- All client requests pass through a centralized API Gateway.
- Authentication and authorization are handled using Spring Security and JWT.

### Fault Isolation

- A failure in one microservice does not necessarily impact the entire system.
- Issues can be identified and resolved more efficiently.

### Flexibility

- Each microservice can evolve independently.
- Future integrations with government systems or third-party services can be added with minimal impact.

### Improved Development Workflow

- Different modules can be developed simultaneously.
- Easier testing and debugging due to clear service boundaries.

### Better User Experience

- Citizens receive a centralized platform for complaint management, awareness, and community participation.
- Authorities benefit from organized workflows and better visibility into complaint resolution.