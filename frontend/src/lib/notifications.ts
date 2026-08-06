import { api } from "./api";
import type { AppNotification, PagedResponse } from "@/types";

// ---------------------------------------------------------------------------
// Notification service — server-backed.
//
// The Notification Service (backend) is the single source of truth for in-app
// notifications: it consumes RabbitMQ domain events, stores them, and serves
// them through the gateway at /api/v1/notifications. Read state now lives on
// the server (Notification.read / readAt), not in localStorage.
//
// Cross-component refresh: components dispatch the NOTIFICATIONS_EVENT window
// event after mutating read state so the nav badge and the feed stay in sync.
// ---------------------------------------------------------------------------

export const NOTIFICATIONS_EVENT = "sa:notifications";

const BASE = "/api/v1/notifications";

/** Default page size for the feed (server clamps to 1..100). */
export const NOTIFICATIONS_PAGE_SIZE = 50;

/** GET /api/v1/notifications?page=&size= — paginated, newest first. */
export async function fetchNotifications(page = 0, size = NOTIFICATIONS_PAGE_SIZE): Promise<PagedResponse<AppNotification>> {
  return api.get<PagedResponse<AppNotification>>(`${BASE}?page=${page}&size=${size}`);
}

/** GET /api/v1/notifications/unread-count — powers the nav badge. */
export async function fetchUnreadCount(): Promise<number> {
  return api.get<number>(`${BASE}/unread-count`);
}

/** PATCH /api/v1/notifications/{id}/read — marks a single notification as read. */
export async function markNotificationRead(id: number): Promise<AppNotification> {
  return api.patch<AppNotification>(`${BASE}/${id}/read`);
}

/** PATCH /api/v1/notifications/read-all — bulk marks every notification as read. */
export async function markAllNotificationsRead(): Promise<number> {
  return api.patch<number>(`${BASE}/read-all`);
}

/** Broadcasts a change so every mounted consumer (badge, feed) refetches. */
export function notifyNotificationsChanged() {
  window.dispatchEvent(new Event(NOTIFICATIONS_EVENT));
}
