# 🔔 Notification Service

Event-driven notification microservice for **Sewage Alert Hyderabad**. Whenever an important
action occurs in another microservice (complaint submitted, status changed, resolved, …), the
producing service publishes an event to **RabbitMQ**. This service consumes that event, persists
an in-app notification, and exposes REST APIs for retrieving them.

> **Key rule:** No service ever inserts notification records directly into this service's
> database. All notification creation is **event-driven** through RabbitMQ.

---

## 🏗️ Architecture

```
                        ┌────────────────────┐
                        │  Eureka Discovery  │
                        └─────────┬──────────┘
                                  │
┌───────────────┐   publish   ┌───▼────────────┐   consume    ┌──────────────────────┐
│ Complaint     │────────────►│   RabbitMQ     │─────────────►│  Notification Service │
│ Service       │             │ notification.* │              │                      │
│ (producer)    │             └────────────────┘              │  Consumer → Service   │
└───────────────┘                                            │  → Repository → MySQL │
                                                              └──────────┬───────────┘
                                                                         │
┌───────────────┐   (future)                                        ┌────▼────┐
│ Community     │────────────►  notification.community.event        │  REST   │
│ Service       │                                                   │  APIs   │
└───────────────┘                                                   └─────────┘
```

Layered packages (Interface → `service/impl` everywhere):

```
com.sewagealert.notification
├── config/     RabbitMqConfig (topology), RabbitMqProperties (@ConfigurationProperties)
├── consumer/   NotificationEventConsumer (main queue), DeadLetterQueueConsumer (DLQ)
├── producer/   NotificationEventProducer (re-publishes for future email/SMS/push channels)
├── controller/ NotificationController (REST API)
├── dto/        ApiResponse, NotificationEvent (contract), NotificationResponse, PagedResponse
├── enums/      NotificationType
├── exception/  NotificationNotFoundException, NotificationProcessingException,
│               RabbitMqPublishException, RabbitMqConsumerException, GlobalExceptionHandler
├── mapper/     NotificationMapper (event ⇄ entity ⇄ response)
├── model/      Notification (JPA entity)
├── repository/ NotificationRepository
├── service/    NotificationService (interface) → impl/NotificationServiceImpl
└── util/       EventTypeResolver, JsonUtils
```

---

## 📨 RabbitMQ Flow

1. **Complaint Service** publishes a `NotificationEvent` to the topic exchange
   `notification.exchange` with a routing key such as `notification.created`.
2. RabbitMQ routes it (wildcard binding `notification.*`) to the durable queue `notification.queue`.
3. **NotificationEventConsumer** picks it up, the service **validates** the payload
   (`userId`, `title`, `message`, `eventType`), and **stores** the notification in MySQL.
4. The message is **acknowledged** only after successful processing.
5. On transient failure → container retry with **exponential backoff** (1s → 2s → 4s → 8s,
   max 5 attempts).
6. After retries are exhausted the message is rejected with `requeue=false` and RabbitMQ
   routes it to the **Dead Letter Queue** `notification.dlq` (via `notification.dlx`),
   where **DeadLetterQueueConsumer** logs it for inspection/replay.

```
notification.created / notification.status.updated / ...   (produced by Complaint Service)
        │
        ▼
notification.exchange (topic, durable)
        │  binding: notification.*
        ▼
notification.queue (durable, x-dead-letter-exchange=notification.dlx)
        │  reject after retries
        ▼
notification.dlx (direct) ──► notification.dlq ──► DeadLetterQueueConsumer (logs)

notification.delivered   (re-published by this service after storing)
        │
        ▼
notification.delivery.exchange (topic, durable — separate exchange, no queues bound yet)
        │  future: email / SMS / push workers bind queues here
        ▼
        (dropped until a worker exists)
```

> ⚠️ The delivery exchange is deliberately **separate** from `notification.exchange`: if delivery
> events were published on the main exchange they would match the `notification.*` binding,
> re-enter `notification.queue`, and cause an endless store→republish loop.

## 🔀 Exchange & Queues

| Component | Name | Type | Notes |
|-----------|------|------|-------|
| Exchange | `notification.exchange` | **topic**, durable | Carries all notification domain events |
| Queue | `notification.queue` | durable | Bound via `notification.*`; DLX args configured |
| DL Exchange | `notification.dlx` | direct, durable | Receives rejected messages |
| DL Queue | `notification.dlq` | durable | Permanently failed messages |
| Delivery Exchange | `notification.delivery.exchange` | topic, durable | Re-published events for future email/SMS/push workers (no queues bound yet) |

## 🔑 Routing Keys

| Routing Key | Event Type | Trigger |
|-------------|-----------|---------|
| `notification.created` | `COMPLAINT_CREATED` | Citizen submits a complaint |
| `notification.assigned` | `COMPLAINT_ASSIGNED` | Authority picks up a complaint |
| `notification.status.updated` | `COMPLAINT_STATUS_UPDATED` | Generic status change |
| `notification.resolved` | `COMPLAINT_RESOLVED` | Complaint resolved |
| `notification.rejected` | `COMPLAINT_REJECTED` | Complaint rejected |
| `notification.reopened` | `COMPLAINT_REOPENED` | Closed complaint reopened |
| `notification.community.event` | `COMMUNITY_EVENT` | Event published / registration (future) |
| `notification.article.published` | `ARTICLE` | Article published (future) |
| `notification.announcement` | `ADMIN` | Admin announcement (future) |
| `notification.event` | `SYSTEM` | Generic/fallback |
| `notification.delivered` | *(internal)* | Re-published by this service **to `notification.delivery.exchange`** for future email/SMS/push workers |

> **Adding a new event type** = add a constant to `NotificationType`, publish with a new
> routing key, done — the wildcard binding already routes it.

---

## 🧾 Event Payload (contract)

```json
{
  "eventId": "uuid",
  "eventType": "COMPLAINT_CREATED",
  "userId": 5,
  "complaintId": 12,
  "referenceType": "COMPLAINT",
  "referenceId": 12,
  "title": "Complaint Submitted",
  "message": "Your complaint #12 has been submitted successfully.",
  "status": "PENDING",
  "priority": "HIGH",
  "createdAt": "2026-08-06T10:00:00",
  "metadata": { "remarks": "..." }
}
```

---

## 🚀 Local Setup

### Prerequisites

- Java 25, Maven
- MySQL (create the database first: `CREATE DATABASE sewagealert_notifications;`)
- RabbitMQ (see Docker setup below)
- Eureka Server running on `:8761`

### 1. RabbitMQ via Docker

```bash
cd notification-service
docker compose up -d
# Management UI: http://localhost:15672  (guest / guest)
```

### 2. Run the service

```bash
cd notification-service
mvn spring-boot:run
# Registers as NOTIFICATION-SERVICE in Eureka on port 8085
```

Start order: `eureka-server` → RabbitMQ → `notification-service` → other services → `api-gateway`.

### 3. Gateway route

Already enabled in `api-gateway/src/main/resources/application.yml`:

```yaml
- id: notification-service
  uri: lb://NOTIFICATION-SERVICE
  predicates:
    - Path=/api/v1/notifications/**
```

---

## 🌐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `RABBITMQ_PORT` | RabbitMQ AMQP port | `5672` |
| `RABBITMQ_USERNAME` | RabbitMQ user | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
| `DB_URL` / datasource block | MySQL JDBC URL | `jdbc:mysql://localhost:3306/sewagealert_notifications` |
| `EUREKA_URL` | Eureka server zone | `http://localhost:8761/eureka/` |

Values after `:` in `application.yml` are local development defaults — set the variables to override.

---

## 🐳 Docker RabbitMQ Setup

`docker-compose.yml` (in this module) exposes:

- **AMQP** port `5672`
- **Management UI** port `15672` — http://localhost:15672 (`guest` / `guest`)
- **Persistent volume** `rabbitmq-data` so queues survive container restarts
- **Healthcheck** so dependent services can wait for the broker

```bash
docker compose up -d       # start
docker compose down        # stop
docker compose down -v     # stop + wipe data (recreates queues on next start)
```

---

## 🔐 Security

- The authenticated user id is read from the **`X-Auth-User-Id` gateway header** — the same
  convention used by `complaint-service` and `user-service`.
- **Frontend-supplied user ids are never trusted**: every query and mutation is scoped to the
  header value (`findByIdAndUserIdAndDeletedFalse`, user-filtered bulk updates).
- Ownership is enforced at the query level, so a user cannot read or delete another user's
  notification (returns 404).
- Admin-only endpoints (e.g., DELETE) are authorized at the gateway; the service additionally
  enforces ownership.

---

## 📡 API Endpoints

Base path: `/api/v1/notifications` (through the gateway on `:8080`).

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/notifications?page=0&size=20` | Paginated notifications, **newest first** |
| `GET` | `/api/v1/notifications/unread-count` | Unread count (badge) |
| `PATCH` | `/api/v1/notifications/{id}/read` | Mark one as read |
| `PATCH` | `/api/v1/notifications/read-all` | Mark all as read |
| `DELETE` | `/api/v1/notifications/{id}` | Soft delete (user-scoped) |

All responses use the standard `ApiResponse<T>` envelope:

```json
{ "success": true, "message": "...", "data": { ... }, "error": null }
```

### Pagination response shape

```json
{
  "content": [ { "id": 1, "title": "...", "notificationType": "COMPLAINT_CREATED", "read": false, ... } ],
  "page": 0, "size": 20, "totalElements": 57, "totalPages": 3, "last": false
}
```

---

## 🧪 Testing

### Manual end-to-end (no frontend)

```bash
# 1. Start RabbitMQ + Eureka + this service.
# 2. Publish a test event from the RabbitMQ Management UI:
#    Exchange: notification.exchange | Routing key: notification.created
#    Payload (JSON, Content-Type application/json):
#    { "eventId":"test-1","eventType":"COMPLAINT_CREATED","userId":1,"complaintId":99,
#      "title":"Test","message":"Hello","createdAt":"2026-08-06T10:00:00" }
# 3. Verify in logs: "Notification stored — id: ..."
# 4. Fetch via API:
curl -H "X-Auth-User-Id: 1" "http://localhost:8080/api/v1/notifications"
curl -H "X-Auth-User-Id: 1" "http://localhost:8080/api/v1/notifications/unread-count"
curl -X PATCH -H "X-Auth-User-Id: 1" "http://localhost:8080/api/v1/notifications/1/read"
curl -X PATCH -H "X-Auth-User-Id: 1" "http://localhost:8080/api/v1/notifications/read-all"
curl -X DELETE -H "X-Auth-User-Id: 1" "http://localhost:8080/api/v1/notifications/1"
```

### Failure paths

- Publish an event with `"eventType":"BOGUS"` → consumer retries then parks it in
  `notification.dlq` (visible in the Management UI "Queues" tab + ERROR logs).
- Stop RabbitMQ, submit a complaint via the Complaint Service → complaint still saves;
  the producer logs a publish failure (fire-and-forget resilience).

---

## 📈 Performance & Scalability Notes

- **Pagination** at the query level; **bulk `UPDATE`** for mark-all-read (no N+1).
- Indexes on `(user_id)`, `(created_at)`, `(is_read)`, `(user_id, is_read)`.
- Single shared `RabbitTemplate` + `ObjectMapper`; connection/channel pooling by the framework.
- Consumer is **batch-friendly**: `spring.rabbitmq.listener.simple.prefetch` can be raised and
  processing is stateless (no in-memory state per message).
- Soft deletes keep the history table append-only-friendly.

### Future-ready extension points

- **Email / SMS / Push** — bind a queue to `notification.delivery.exchange` with
  `notification.delivered` (re-published by `NotificationEventProducer`); fetch contacts via
  `UserServiceClient`.
- **WebSockets / SSE** — subscribe to stored notifications.
- **FCM / OneSignal / Azure Notification Hub** — pluggable delivery workers.
- **Notification preferences** — filter by `NotificationType` at store time.
- **Scheduler / digests** — read the `notifications` table with `createdAt` range queries.
- **Idempotency** — persist `eventId` with a unique index to dedupe re-delivered events.
- **Outbox pattern** in producers to guarantee at-least-once delivery when RabbitMQ is down.

---

## 🧭 Related Services

| Service | Role |
|---------|------|
| `complaint-service` | Publishes `notification.*` events (producer) |
| `community-service` | Future producer (`notification.community.event`, `notification.article.published`) |
| `api-gateway` | Routes `/api/v1/notifications/**` → this service |
| `eureka-server` | Service discovery |
