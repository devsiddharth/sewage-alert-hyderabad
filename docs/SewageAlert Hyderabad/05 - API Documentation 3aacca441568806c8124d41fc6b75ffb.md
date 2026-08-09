# 05 - API Documentation

# 1️⃣ API Overview

> 🌐 **API Overview**
> 
> 
> The SewageAlert Hyderabad platform follows RESTful API principles. All client requests are routed through the API Gateway, which forwards them to the appropriate microservice. APIs exchange data in JSON format and are secured using JWT authentication.
> 

---

# 2️⃣ Authentication APIs

| Method | Endpoint | Description | Access |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a new user | Public |
| POST | `/api/v1/auth/login` | Login and receive JWT | Public |
| GET | `/api/v1/auth/profile` | Get logged-in user | Authenticated |

---

# 3️⃣ User APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/v1/users/{id}` | Get user profile |
| PUT | `/api/v1/users/{id}` | Update profile |
| DELETE | `/api/v1/users/{id}` | Delete account |

---

# 4️⃣ Complaint APIs

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/v1/complaints` | Create complaint |
| GET | `/api/v1/complaints` | Get all complaints |
| GET | `/api/v1/complaints/{id}` | Get complaint details |
| PUT | `/api/v1/complaints/{id}` | Update complaint |
| PATCH | `/api/v1/complaints/{id}/status` | Update complaint status |
| DELETE | `/api/v1/complaints/{id}` | Delete complaint |

### Create Complaint (multipart/form-data)

`POST /api/v1/complaints` consumes `multipart/form-data` (not JSON). Headers: `Authorization: Bearer <jwt>` and `X-Auth-User-Id: <id>`.

| Form field | Type | Required | Description |
| --- | --- | --- | --- |
| `title` | string | ✅ | Complaint title |
| `description` | string | ✅ | Complaint details |
| `latitude` | number | ✅ | GPS latitude |
| `longitude` | number | ✅ | GPS longitude |
| `images` | file(s) | ❌ | Image files — repeat the field for multiple images (max 4). JPG/PNG/WEBP only. |

Images are uploaded to Cloudinary object storage; only the returned URLs are persisted in MySQL.
The response `data.imageUrls` contains those public URLs (e.g. `https://res.cloudinary.com/.../complaints/photo.jpg`).

```
POST /api/v1/complaints
Content-Type: multipart/form-data; boundary=----x

------x
Content-Disposition: form-data; name="title"

Sewage overflow near road
------x
Content-Disposition: form-data; name="description"

Blocking the drain for 2 days
------x
Content-Disposition: form-data; name="latitude"

17.3850
------x
Content-Disposition: form-data; name="longitude"

78.4867
------x
Content-Disposition: form-data; name="images"; filename="photo.jpg"
Content-Type: image/jpeg

<binary>
------x--
```

---

# 5️⃣ Community APIs

### Events

| Method | Endpoint |
| --- | --- |
| GET | `/api/v1/events` |
| POST | `/api/v1/events` |
| PUT | `/api/v1/events/{id}` |
| DELETE | `/api/v1/events/{id}` |

### Event Registration

| Method | Endpoint |
| --- | --- |
| POST | `/api/v1/events/{id}/register` |
| GET | `/api/v1/events/{id}/registrations` |

### Articles

| Method | Endpoint |
| --- | --- |
| GET | `/api/v1/articles` |
| POST | `/api/v1/articles` |
| PUT | `/api/v1/articles/{id}` |
| DELETE | `/api/v1/articles/{id}` |

### NGOs

| Method | Endpoint |
| --- | --- |
| GET | `/api/v1/ngos` |
| POST | `/api/v1/ngos` |
| PUT | `/api/v1/ngos/{id}` |
| DELETE | `/api/v1/ngos/{id}` |

---

# 6️⃣ Notification APIs

| Method | Endpoint |
| --- | --- |
| GET | `/api/v1/notifications` |
| POST | `/api/v1/notifications/send` |

---

# 7️⃣ API Response Standard

Use the same response format across all services.

### Success

```
{
  "success":true,
  "message":"Complaint created successfully.",
  "data": {}
}
```

### Error

```
{
  "success":false,
  "message":"Complaint not found.",
  "error": {}
}
```

---

# 8️⃣ HTTP Status Codes

| Code | Meaning |
| --- | --- |
| 200 | OK |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 500 | Internal Server Error |