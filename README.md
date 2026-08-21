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
            React + TypeScript Frontend (frontend/)
                        │
                        ▼
               Spring Cloud Gateway (:8080)
                        │
                        ▼
                Eureka Discovery Server (:8761)
                        │
 ┌──────────┬──────────┬──────────┬──────────┬──────────┐
 ▼          ▼          ▼          ▼          ▼          ▼
Auth      User     Complaint  Community  Notification  AI
Service   Service  Service    Service    Service     Service
        │             ▲          ▲                       │
        └────Feign────┘          │ (Feign)              │
                          ┌──────┴──────┐        ┌──────┴──────┐
                          │  Community  │        │  Complaint  │
                          │  Internal   │        │  Internal   │
                          │  AI Data    │        │  AI Data    │
                          └──────┬──────┘        └──────┬──────┘
                                 │                       │
                          ┌──────▼───────────────────────▼──────┐
                          │          AI Service (:8086)          │
                          │  Intent Detection → Data Grounding   │
                          │  → OpenAI-Compatible API → Response  │
                          └─────────────────────────────────────┘
                          ┌──────────▼──────────┐
                          │   RabbitMQ (AMQP)   │
                          │ notification.exchange│
                          └──────────┬──────────┘
                                     │ (consumes events)
                                     ▼
                          Notification Service
```

---

# 🚀 Tech Stack

## Backend

- Java 25
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Cloud OpenFeign
- Spring Data JPA
- Hibernate
- JWT Authentication
- RabbitMQ (Spring AMQP) — event-driven notifications
- Spring WebFlux (WebClient) — AI provider HTTP calls

## Database

- MySQL

## Frontend (`frontend/`)

- React 18 + TypeScript
- Vite
- Tailwind CSS
- React Router
- Recharts (analytics)
- Lucide Icons

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
- RabbitMQ Producer (publishes notification.* events)

Database

- sewagealert_complaints

---

## ✅ AI Service (v2.1.0)

Responsibilities

- Natural-language AI assistant for platform users
- Intent detection and query classification
- Grounded data retrieval from platform services
- OpenAI-compatible AI provider integration
- Article draft generation
- Complaint insights and analytics
- NGO activity summaries
- Admin platform-wide analytics

Features

- Keyword-based intent detection (no AI call needed)
- Data grounding — retrieves platform data before AI generation
- Role-based authorization (Citizen, NGO, Admin)
- Configurable AI provider (OpenAI, Groq, Together AI, Ollama, etc.)
- Graceful degradation when AI is disabled
- Hallucination prevention — AI answers from actual platform data
- Floating chat widget for all authenticated dashboards

AI Configuration

| Variable | Description | Default |
|----------|-------------|--------|
| `AI_ENABLED` | Master toggle for AI features | `false` |
| `AI_API_KEY` | API key for OpenAI-compatible provider | *(empty)* |
| `AI_MODEL` | Model identifier (e.g., `gpt-4o-mini`) | `gpt-4o-mini` |
| `AI_BASE_URL` | Base URL of the AI API | `https://api.openai.com/v1` |
| `AI_MAX_TOKENS` | Max tokens for AI response | `1024` |
| `AI_TEMPERATURE` | Response temperature (0.0-1.0) | `0.7` |
| `AI_TIMEOUT_SECONDS` | HTTP timeout for AI calls | `30` |

API Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| POST | `/api/v1/ai/chat` | Unified AI chat | Any user |
| POST | `/api/v1/ai/user/query` | User AI assistant | Any user |
| POST | `/api/v1/ai/ngo/query` | NGO AI assistant | NGO Rep |
| POST | `/api/v1/ai/admin/query` | Admin AI assistant | Admin |
| POST | `/api/v1/ai/articles/generate` | Generate article draft | Any user |
| POST | `/api/v1/ai/articles/summarize` | Summarize content | Any user |
| POST | `/api/v1/ai/events/discover` | Discover events | Any user |
| POST | `/api/v1/ai/complaints/insights` | Complaint insights | Any user |
| POST | `/api/v1/ai/community/query` | Community queries | Any user |

Database

- None (stateless service — reads data from other services via Feign)

Port

- 8086

---

## ✅ Notification Service

Responsibilities

- Consume domain events from RabbitMQ
- Store notifications
- Serve in-app notifications to users
- Send transactional emails (EmailJS) — verification, welcome, future complaint updates

Features

- RabbitMQ Consumer (topic exchange, DLQ, retries)
- Event-driven storage (no service writes to its DB directly)
- EmailJS REST integration (`emailjs.*` env vars) — email delivery stays server-side
- Paginated APIs, newest first
- Read / unread tracking (single + bulk)
- Soft delete
- SLF4J logging + global exception handling

Database

- sewagealert_notifications

Broker

- RabbitMQ (docker-compose.yml in `notification-service/`)

---

# 🚀 Running the Application

## 1. Backend (microservices)

Each service is a Spring Boot app. Start them in order (MySQL must be running
and the databases from `docs/04 - Database Design` created):

```bash
# 0. Message broker (RabbitMQ) — required by the notification flow
cd notification-service && docker compose up -d   # Management UI: http://localhost:15672

# 1. Discovery server
cd eureka-server && mvn spring-boot:run

# 2. Backend services (each in its own terminal)
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd complaint-service && mvn spring-boot:run
cd community-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd ai-service && mvn spring-boot:run  # Optional — requires AI_API_KEY

# 3. API Gateway (single entry point on :8080)
cd api-gateway && mvn spring-boot:run
```

## 2. Frontend

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173 (proxies /api -> :8080)
```

See `frontend/README.md` for deployment, environment variables and the
API integration notes.

---

# ☁️ Cloudinary (Complaint Service)

Complaint images **and the mandatory resolution-proof photos** (v1.1.0) are uploaded
to **Cloudinary** object storage; only the returned URLs are stored in MySQL (no
Base64 image payloads in the database). The complaint service reads these variables
via `application.yml` — **set them before starting `complaint-service`**, otherwise
image uploads fail at runtime:

| Variable | Description | Default |
|----------|-------------|---------|
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name | *(empty)* |
| `CLOUDINARY_API_KEY` | Cloudinary API key | *(empty)* |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | *(empty)* |

```bash
# Linux/macOS
export CLOUDINARY_CLOUD_NAME=your-cloud
export CLOUDINARY_API_KEY=your-key
export CLOUDINARY_API_SECRET=your-secret
```

Never commit real credentials — they live in your environment, not in Git.

---
# ✉️ Email Verification & Email Notifications (EmailJS)

New customer accounts must **verify their email address before they can log in**.
Emails are sent by the **Notification Service** via the **EmailJS REST API** — the
React frontend and the Auth Service never talk to EmailJS directly:

```text
Register ──► Auth Service ──► RabbitMQ (USER_REGISTERED) ──► Notification Service ──► EmailJS ──► Customer inbox
                                                                                                  │
                                                                                                  ▼
                    Customer types the 6-digit code into the registration page ──► POST /api/v1/auth/verify-code
                                                                                                  │
                                                                                                  ▼
Login ◄── emailVerified = true
```

## Configuration

The Notification Service reads these variables via `application.yml`. The
**private key must only live in your environment** — never commit it:

| Variable | Description | Default |
|----------|-------------|---------|
| `EMAILJS_SERVICE_ID` | EmailJS service id (`service_2mr35tu`) | *(empty)* |
| `EMAILJS_TEMPLATE_ID` | Verification template id (`template_w4koj8i`) | *(empty)* |
| `EMAILJS_PUBLIC_KEY` | EmailJS public key | *(empty)* |
| `EMAILJS_PRIVATE_KEY` | EmailJS private key — **env var / deployment secret only** | *(empty)* |
| `EMAILJS_WELCOME_TEMPLATE_ID` | Optional welcome-email template (post-verification). Template content + setup steps: `notification-service/EMAILJS-WELCOME-TEMPLATE.md` | *(empty → no welcome email)* |

> The verification template (`template_w4koj8i`) renders the typed-in code via the
> `{{verification_code}}` parameter (plus `{{name}}`, `{{email}}`). Verification is
> **OTP-only** — the email contains no link. See `notification-service/EMAILJS-WELCOME-TEMPLATE.md`
> for the exact snippet.
| `FRONTEND_URL` | Public origin of the React app (used for the welcome-email login link) | `http://localhost:5173` |
| `VERIFICATION_TOKEN_TTL_MINUTES` | Verification code lifetime (Auth Service) | `30` |

```bash
# Linux/macOS
cd notification-service
export EMAILJS_SERVICE_ID=service_2mr35tu
export EMAILJS_TEMPLATE_ID=template_w4koj8i
export EMAILJS_PUBLIC_KEY=your-public-key
export EMAILJS_PRIVATE_KEY=your-private-key
export FRONTEND_URL=http://localhost:5173
mvn spring-boot:run
```

A git-ignored local copy with your real values can live at
`notification-service/.env.local` (see `.env.example` at the repo root). Without
credentials the service still starts — emails are skipped and logged, and customers
can use **Resend verification email** later.

## Registration flow

Verification is asked **during registration itself**: right after submitting the
form, the customer sees a 6-digit-code screen and types in the code from the email.

1. Citizen registers → account is created **unverified** (no JWT is issued).
2. Auth Service generates a secure, single-use 6-digit code (SHA-256 hashed in the
   DB, expires in 30 minutes) and publishes `USER_REGISTERED` to RabbitMQ.
3. Notification Service consumes the event and sends the verification email via
   EmailJS — it contains the code rendered with `{{verification_code}}`. No
   verification link is sent (OTP-only flow).
4. The customer types the code into the registration page →
   `POST /api/v1/auth/verify-code` validates it (max 5 wrong attempts, then a
   60s lockout), marks it used, sets `emailVerified = true`, and publishes
   `EMAIL_VERIFIED`.
5. Notification Service consumes `EMAIL_VERIFIED` and sends the **welcome email**
   (only when `EMAILJS_WELCOME_TEMPLATE_ID` is set).
6. Citizen logs in → JWT issued. Unverified logins are rejected with code
   `EMAIL_NOT_VERIFIED`.

`POST /api/v1/auth/resend-verification` re-issues a fresh code (throttled
to one email per account per minute; the response is generic to prevent account
enumeration).

### 🔎 Email not received? Check in this order

1. **RabbitMQ is running** — `cd notification-service && docker compose up -d`.
   If the Auth Service logged `Failed to publish notification event`, the broker
   was down at registration time (publishing is fire-and-forget by design).
2. **EmailJS credentials are set in the Notification Service environment**
   (`EMAILJS_SERVICE_ID`, `EMAILJS_TEMPLATE_ID`, `EMAILJS_PUBLIC_KEY`,
   `EMAILJS_PRIVATE_KEY`). Without them the service logs
   `EmailJS not configured — skipping verification email`. Copy
   `notification-service/.env.example` → `.env.local` and fill it in.
3. **The EmailJS service is activated.** EmailJS emails an activation link to the
   service's "From Email" address — until it is clicked, every send fails with
   an error in the Notification Service log (`EmailJS verification email FAILED …`,
   e.g. *domain not verified*).
4. **Check spam / promotions** — emails from EmailJS free services often land there.
5. Retry from the UI: the registration page has a **Resend code** button (60s
   cooldown), or open the login page and use **Resend verification email**.

---
# 🤖 AI Configuration (v2.1.0)

The AI Service integrates with any **OpenAI-compatible API** provider. Set the following
environment variables before starting the AI service:

```bash
# Enable AI features (disabled by default)
export AI_ENABLED=true

# AI provider credentials
export AI_API_KEY=your-api-key-here
export AI_MODEL=gpt-4o-mini
export AI_BASE_URL=https://api.openai.com/v1

# Optional tuning
export AI_MAX_TOKENS=1024
export AI_TEMPERATURE=0.7
export AI_TIMEOUT_SECONDS=30
```

**Supported providers:**

| Provider | Base URL | Model Examples |
|----------|----------|----------------|
| OpenAI | `https://api.openai.com/v1` | `gpt-4o-mini`, `gpt-4o` |
| Groq | `https://api.groq.com/openai/v1` | `llama-3.3-70b-versatile` |
| Together AI | `https://api.together.xyz/v1` | `meta-llama/Llama-3-70b-chat-hf` |
| Ollama (local) | `http://localhost:11434/v1` | `llama3`, `mistral` |
| Azure OpenAI | `https://<resource>.openai.azure.com/openai/deployments/<model>` | `gpt-4o-mini` |

**When `AI_ENABLED=false`** (default), the AI service starts normally but all endpoints
return a friendly "AI is disabled" message. The rest of the platform operates normally.

For Docker deployment, add the AI variables to your `.env` file:

```bash
AI_ENABLED=false
AI_API_KEY=
AI_MODEL=gpt-4o-mini
AI_BASE_URL=https://api.openai.com/v1
```

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

Complaint Service ──(RabbitMQ: notification.*)──► Notification Service
```

Implemented using

- Spring Cloud OpenFeign
- Eureka Service Discovery
- RabbitMQ (Spring AMQP) for event-driven notifications

## Event-Driven Notification Flow

1. A citizen submits a complaint (or an authority changes its status).
2. **Complaint Service** publishes a `NotificationEvent` to the `notification.exchange`
   topic exchange (routing key e.g. `notification.created`, `notification.resolved`).
3. **Notification Service** consumes the event, validates it, and stores an in-app
   notification for the citizen in its own database.
4. Failed messages are retried with exponential backoff; permanently failed messages
   are parked in the dead letter queue `notification.dlq`.
5. The citizen reads notifications through `GET /api/v1/notifications` (via the gateway).

See `notification-service/README.md` for the full RabbitMQ topology and API reference.

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
| Notification Service | sewagealert_notifications |

This follows the Database per Service pattern.

---

# 📡 API Flow

###

---

# 🚀 Product Roadmap & Upcoming Releases

> **Note:** Version numbers below represent the **planned product roadmap** and may be
> adjusted as implementation priorities evolve. `v1.0.0` reflects the currently
> **released** production functionality; `v1.1.x`, `v1.2.0`, `v2.0.0`, `v2.1.0`, and
> `v3.0.0` describe **planned/upcoming** functionality that is not yet implemented.

## ✅ v1.0.0 — Production Release

The **current production release**. The core platform is **deployed and operational**, including:

- Web frontend (React + TypeScript)
- Microservices backend
- Authentication (JWT + email verification)
- Complaint management (including image uploads via Cloudinary)
- Notifications (in-app + EmailJS email, event-driven via RabbitMQ)
- Docker deployment
- CI/CD pipeline
- AWS EC2 backend hosting
- Vercel frontend hosting

---

## 📋 v1.1.x — Reliability & UX Improvements *(Partially implemented)*

- ✅ **Mandatory resolution/closure proof image:** Admin/authorized personnel must
  upload a resolution photo before a complaint can be marked as `RESOLVED`. Enforced
  at the **backend** (the JSON status endpoint rejects `RESOLVED`; the dedicated
  `/resolve` endpoints validate, upload and store the proof photo before the status
  change) and surfaced in the admin & field-officer UIs, with the proof photo shown
  to citizens on the complaint detail page.
- ✅ **User-facing content cleanup:** Obsolete/misleading content removed and
  terminology simplified across the complaint workflow (friendly status labels,
  clearer resolution copy).

---

## 📋 v1.2.0 — Responsive Web Experience *(Planned)*

- Make the existing React web application fully responsive and mobile-friendly
  across phones, tablets, and desktops.
- Improve mobile navigation, forms, complaint reporting, image uploads,
  dashboards, tables, modals, maps, notifications, and other relevant UI components.
- Serves as the foundation for the future mobile application.

---

## 📋 v2.0.0 — NGO & Community Engagement Platform *(Next Major Release — Planned)*

### NGO Authentication & Dashboard

- Separate NGO login/authentication.
- Dedicated NGO dashboard.
- NGO profile and organization information.
- NGO mission/motive and areas of focus.
- Progress tracking from the beginning of the NGO's activities.
- Achievements and milestones.
- Events and drives management.
- Participant management.
- Funds received and expenses/spending transparency related to
  community-development activities.

### NGO Event & Drive Workflow

```
NGO creates event/drive
→ Request submitted for Admin approval
→ Admin reviews request
→ Admin approves/rejects
→ Approved event/drive becomes visible to users
→ Users can view details and register/join
→ Registered users receive reminders
→ Participant information becomes available to the respective NGO dashboard
→ Admin retains access to monitor and manage events/drives
```

Admin remains responsible for moderation, approval, and management even after an
event/drive is published.

### User Participation

- Users can discover upcoming NGO events and drives after logging in.
- Users can join/register for approved events and drives.
- Users can view their registered/upcoming/completed participation.
- Users receive relevant reminders and notifications.
- Registration/participant data is made available to the respective NGO dashboard
  with appropriate privacy controls.

---

## ✅ v2.1.0 — AI Capabilities *(Implemented)*

Implemented as a dedicated **AI Service** microservice with:

- **User AI Assistant:** Natural-language queries about NGOs, events, drives, articles,
  complaints, and platform usage.
- **NGO AI Assistant:** Drive summaries, volunteer insights, activity analytics,
  event description generation, and article draft creation.
- **Admin AI Assistant:** Platform-wide complaint analytics, hotspot identification,
  recurring issue detection, NGO activity summaries, and trend analysis.
- **Article Assistance:** AI-assisted article drafting and content generation.
- **Event/Drive Discovery:** Natural-language search for upcoming events and drives.
- **Complaint Intelligence:** Complaint volume analysis, geographic hotspot detection,
  overdue identification, and priority distribution insights.
- **Community Intelligence:** Cross-domain queries combining NGOs, events, drives,
  articles, and complaints.
- **Infrastructure Intelligence:** Natural-language queries about pipelines, lakes,
  treatment plants (STPs), operational status, maintenance history, and water reuse.
- **Data Grounding:** All platform-specific answers are grounded in actual data —
  the AI retrieves structured data before generating responses to prevent hallucination.
- **Configurable AI Provider:** Works with any OpenAI-compatible API (OpenAI, Groq,
  Together AI, Azure OpenAI, local Ollama, etc.).
- **Floating Chat Widget:** Available on all authenticated dashboards (citizen, admin, NGO).
- **Article Generator:** Dedicated article generation workflow in the NGO dashboard.
- **AI-Enabled Toggle:** Platform works normally with `AI_ENABLED=false`.

---

## 📋 v3.0.0 — Mobile Application *(Future Major Release — Planned)*

A future major release, not part of v2.0.0, including:

- Dedicated mobile application.
- Android/iOS support as appropriate.
- Reuse the existing backend APIs and API Gateway rather than creating a separate backend.
- Mobile authentication.
- Complaint reporting and image capture/upload.
- Location-aware functionality.
- Notifications/push notifications.
- NGO event/drive discovery and registration.
- User participation tracking.

---

## 📅 Roadmap Summary

| Version | Planned Focus | Status |
|---------|---------------|--------|
| v1.0.0 | Production release and core platform | ✅ Released |
| v1.1.x | Resolution proof + UX/content cleanup | ✅ Partially implemented |
| v1.2.0 | Responsive/mobile-friendly web experience | 📋 Planned |
| v2.0.0 | NGO ecosystem, events, drives, approvals and user participation | 📋 Planned |
| v2.1.0 | AI-powered platform capabilities | ✅ Implemented |
| v3.0.0 | Dedicated mobile application | 📋 Planned |
