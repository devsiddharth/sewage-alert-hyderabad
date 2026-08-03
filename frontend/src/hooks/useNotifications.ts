import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/lib/auth";
import { useComplaints } from "@/hooks/useComplaints";
import { buildNotifications, unreadCount, type AppNotification } from "@/lib/notifications";

export function useNotifications(): {
  notifications: AppNotification[];
  unread: number;
  loading: boolean;
  refresh: () => void;
} {
  const { user } = useAuth();
  const { complaints, loading, reload } = useComplaints({ userId: user?.id });
  const [bump, setBump] = useState(0);

  // Recompute when complaints change or read-state changes.
  const notifications = useMemo(
    () => (complaints ? buildNotifications(complaints) : []),
    [complaints, bump] // eslint-disable-line react-hooks/exhaustive-deps
  );

  const unread = useMemo(() => unreadCount(notifications.map((n) => n.id)), [notifications]);

  useEffect(() => {
    window.addEventListener("sa:notifications", () => setBump((v) => v + 1));
    return () => window.removeEventListener("sa:notifications", () => setBump((v) => v + 1));
  }, []);

  return { notifications, unread, loading, refresh: () => void reload() };
}
