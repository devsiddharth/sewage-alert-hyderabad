import type { Complaint } from "@/types";

export interface AppNotification {
  id: string;
  complaintId: number;
  type: "created" | "assigned" | "updated" | "resolved" | "rejected";
  title: string;
  description: string;
  at: string;
}

const READ_KEY = "sa_read_notifications";

function getReadSet(): Set<string> {
  try {
    return new Set(JSON.parse(localStorage.getItem(READ_KEY) ?? "[]") as string[]);
  } catch {
    return new Set();
  }
}

function saveReadSet(set: Set<string>) {
  localStorage.setItem(READ_KEY, JSON.stringify(Array.from(set)));
}

/** Derives a notification feed from complaint status history (no backend notification service yet). */
export function buildNotifications(complaints: Complaint[]): AppNotification[] {
  const list: AppNotification[] = [];
  for (const c of complaints) {
    const id = (s: string, at: string) => `${c.id}-${s}-${at}`;

    list.push({
      id: id("CREATED", c.createdAt),
      complaintId: c.id,
      type: "created",
      title: `Complaint #${c.id} submitted`,
      description: c.title,
      at: c.createdAt,
    });

    for (const h of c.history) {
      const type =
        h.status === "RESOLVED" ? "resolved" : h.status === "REJECTED" ? "rejected" : h.status === "IN_PROGRESS" ? "assigned" : "updated";
      list.push({
        id: id(h.status, h.updatedAt),
        complaintId: c.id,
        type,
        title:
          type === "resolved"
            ? `Complaint #${c.id} resolved`
            : type === "rejected"
              ? `Complaint #${c.id} rejected`
              : type === "assigned"
                ? `Complaint #${c.id} is being worked on`
                : `Complaint #${c.id} updated`,
        description: h.remarks ?? `Status changed to ${h.status.replace("_", " ")}.`,
        at: h.updatedAt,
      });
    }
  }
  return list.sort((a, b) => new Date(b.at).getTime() - new Date(a.at).getTime());
}

export function isNotificationRead(id: string): boolean {
  return getReadSet().has(id);
}

export function markNotificationRead(id: string) {
  const set = getReadSet();
  set.add(id);
  saveReadSet(set);
}

export function markAllRead(ids: string[]) {
  const set = getReadSet();
  ids.forEach((id) => set.add(id));
  saveReadSet(set);
}

export function unreadCount(ids: string[]): number {
  const read = getReadSet();
  return ids.filter((id) => !read.has(id)).length;
}
