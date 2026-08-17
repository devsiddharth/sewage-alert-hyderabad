# 04 - Database Design

# 1️⃣ Database Overview

<aside>
🚀

> 🗄️ **Database Overview**
> 
> 
> The Sewage Alert Hyderabad platform uses **MySQL** as its primary relational database. The database is designed to store user information, complaints, awareness events, educational resources, infrastructure details, and notifications while maintaining data integrity and supporting future scalability.
> 
</aside>

---

# 2️⃣ Database Design Principles

✅ Primary Keys

✅ Foreign Keys

✅ Data Integrity

✅ Normalization

✅ Referential Integrity

✅ Scalable Design

---

# 3️⃣ ER Diagram

![ERD-diagram.png](04%20-%20Database%20Design/ERD-diagram.png)

---

# 4️⃣ Database Tables

## 👤 Users

> 🎯 Purpose
> 
> 
> Store application users.
> 

| Column | Type |
| --- | --- |
| id | BIGINT |
| name | VARCHAR |
| email | VARCHAR |
| password | VARCHAR |
| phone | VARCHAR |
| role | ENUM |
| created_at | TIMESTAMP |

---

## 🚨 Complaints

> 🎯 Purpose
> 
> 
> Store sewage complaints.
> 

| Column | Type |
| --- | --- |
| id | BIGINT |
| title | VARCHAR |
| description | TEXT |
| latitude | DOUBLE |
| longitude | DOUBLE |
| image_url | VARCHAR |
| status | ENUM |
| priority | ENUM |
| created_by | FK(User) |
| assigned_to | FK(User) |
| resolution_remarks | TEXT |
| resolution_proof_image_url | VARCHAR |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |

> `resolution_proof_image_url` stores the object-storage URL of the mandatory resolution-proof
> photo (v1.1.0) — set only when a complaint is marked RESOLVED. Image bytes are never stored
> in the database; only URLs are persisted (same as complaint images via Cloudinary).

---

## 📷 Complaint Images

| Column | Type |
| --- | --- |
| id | BIGINT |
| complaint_id | FK |
| image_url | VARCHAR |

---

## 📜 Complaint History

| Column | Type |
| --- | --- |
| id | BIGINT |
| complaint_id | FK |
| status | ENUM |
| remarks | TEXT |
| updated_at | TIMESTAMP |

---

## 🌱 Events

| Column | Type |
| --- | --- |
| id | BIGINT |
| title | VARCHAR |
| description | TEXT |
| location | VARCHAR |
| event_date | DATE |
| organizer | VARCHAR |
| capacity | INT |

---

## 🙋 Event Registration

| Column | Type |
| --- | --- |
| id | BIGINT |
| event_id | FK |
| user_id | FK |
| registered_at | TIMESTAMP |

---

## 📚 Articles

| Column | Type |
| --- | --- |
| id | BIGINT |
| title | VARCHAR |
| content | TEXT |
| category | VARCHAR |
| published_at | TIMESTAMP |

---

## 🤝 NGOs

| Column | Type |
| --- | --- |
| id | BIGINT |
| name | VARCHAR |
| contact_person | VARCHAR |
| email | VARCHAR |
| website | VARCHAR |

---

## 🚰 Pipelines

| Column | Type |
| --- | --- |
| id | BIGINT |
| locality | VARCHAR |
| installation_year | YEAR |
| capacity | INT |
| maintenance_date | DATE |

---

## 🏭 Treatment Plants

| Column | Type |
| --- | --- |
| id | BIGINT |
| name | VARCHAR |
| capacity_mld | DECIMAL |
| location | VARCHAR |

---

## 🌊 Lakes

| Column | Type |
| --- | --- |
| id | BIGINT |
| name | VARCHAR |
| restoration_status | VARCHAR |
| connected_stp | FK |

---

## 📧 Notifications

| Column | Type |
| --- | --- |
| id | BIGINT |
| user_id | FK |
| message | TEXT |
| status | ENUM |
| sent_at | TIMESTAMP |

---

# 5️⃣ Relationships

<aside>
🚀

### Complaint

- has → Images
- has → History

### User

- submits → Complaints
- registers → Events
- receives → Notifications

### Event

- has → Registrations

### Lake

- connected to → Treatment Plant
</aside>

---

# 6️⃣ Normalization

> 💡 **Normalization**
> 
> 
> <aside>
> 🚀
> 
> The database follows normalization principles to reduce redundancy, improve consistency, and maintain referential integrity through the use of primary keys and foreign keys.
> 
> </aside>
> 

---

# 7️⃣ Future Improvements

> 🚀 Future Database Enhancements
> 
> - Database per microservice
> - Read replicas
> - Redis caching
> - Elasticsearch
> - Database sharding