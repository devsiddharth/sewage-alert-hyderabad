# 🖥️ SewageAlert Hyderabad — Frontend

A premium, citizen-first React + TypeScript frontend for the SewageAlert Hyderabad
microservices platform. Fully wired to the backend through the Spring Cloud Gateway.

## Stack

- **React 18 + TypeScript** (strict mode)
- **Vite 5** (dev proxy + production build)
- **Tailwind CSS 3** (design tokens from the product spec)
- **React Router 6** (public / citizen / admin route trees)
- **Recharts** (admin analytics)
- **Lucide** (icons)

## Quick start

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173
```

The Vite dev server proxies `/api/*` → `http://localhost:8080` (the API Gateway),
so no CORS configuration is needed locally. Start the backend stack first
(Eureka → services → gateway), then open the app.

## Connecting to a deployed gateway

**Vercel (production):** `vercel.json` rewrites `/api/:path*` to the EC2 Spring
Cloud Gateway (`http://16.113.76.178:8080/api/:path*`), so the browser only ever
talks to the Vercel domain over HTTPS — no mixed content, no CORS. Do **not** set
`VITE_API_URL` in the Vercel environment; leave the API base empty so the
frontend uses relative `/api/...` paths.

```bash
npm run build
npm run preview      # serves the production build
```

**Self-hosted / other hosts:** if the build is served somewhere without a proxy,
set the gateway URL at build time instead:

```bash
cp .env.example .env
# VITE_API_URL=http://<gateway-host>:8080
npm run build
```

## Scripts

| Command              | Description                          |
| -------------------- | ------------------------------------ |
| `npm run dev`        | Dev server with API proxy            |
| `npm run build`      | Typecheck + production bundle        |
| `npm run preview`    | Serve the production bundle          |
| `npm run typecheck`  | TypeScript check only                |

## Project structure

```
src/
├── components/
│   ├── admin/          # ResourceManager (generic CRUD) used by all community pages
│   ├── complaints/     # ComplaintCard, ComplaintDetail
│   ├── layout/         # PublicLayout, AppShell, RouteGuards, AuthLayout
│   └── ui/             # Button, Field, Card, Badge, Modal, Pagination, Timeline…
├── hooks/              # useComplaints, useComplaint, useNotifications, usePublicStats
├── lib/                # api client, auth context, toast, notifications, utils
├── pages/
│   ├── public/         # Home, Login, Register, ForgotPassword, TrackComplaint
│   ├── dashboard/      # Citizen area (report, my complaints, notifications, profile)
│   └── admin/          # Command centre, complaints, analytics, settings, community CRUD
└── types/              # DTO types mirroring the backend responses
```

## API integration notes

- Every request goes through `src/lib/api.ts`, which unwraps the backend
  `{ success, message, data, error }` envelope and throws typed `ApiError`s.
- Authenticated calls send **both** `Authorization: Bearer <jwt>` and
  `X-Auth-User-Id: <id>` headers (the services expect the latter).
- A 401 anywhere clears the session and redirects to login.
- **Field mapping:** the backend `ComplaintRequest` has no category/severity
  fields, so the report form embeds the selected *category* in the title and
  appends *severity* + *landmark* to the description. Photo uploads are
  compressed client-side and sent as **binary files** via `multipart/form-data`;
  the backend uploads them to Cloudinary object storage and persists only the
  returned URLs (no Base64 payloads in the database).
- Notifications are served by the **Notification Service** through
  `GET /api/v1/notifications` (read/unread state is server-side). Settings
  (categories/departments) persist in `localStorage` until configuration
  endpoints exist.

## Roles & routing

| Area     | Path prefix      | Access            |
| -------- | ---------------- | ----------------- |
| Public   | `/`, `/login`…   | Everyone          |
| Citizen  | `/dashboard/*`   | Authenticated     |
| Admin    | `/admin/*`       | ADMIN / AUTHORITY |
