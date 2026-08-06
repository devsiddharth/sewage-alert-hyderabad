import { useCallback, useEffect, useRef, useState } from "react";
import { useAuth } from "@/lib/auth";
import {
  fetchNotifications,
  fetchUnreadCount,
  NOTIFICATIONS_EVENT,
  NOTIFICATIONS_PAGE_SIZE,
} from "@/lib/notifications";
import type { AppNotification } from "@/types";

/**
 * useNotifications: Server-backed, paginated notification feed.
 *
 * Fetches the logged-in user's notifications (newest first) plus the unread
 * count from the Notification Service. `refresh()` resets to the first page;
 * `loadMore()` appends the next page. Refreshes on mount, on every
 * NOTIFICATIONS_EVENT, and on a light 60s poll so notifications arriving via
 * RabbitMQ on the backend surface without a manual reload.
 */
export function useNotifications(): {
  notifications: AppNotification[];
  unread: number;
  total: number;
  loading: boolean;
  loadingMore: boolean;
  hasMore: boolean;
  refresh: () => void;
  loadMore: () => void;
} {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [unread, setUnread] = useState(0);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const nextPageRef = useRef(1); // next page to append when loadMore() is called

  const load = useCallback(
    async (page: number, append: boolean) => {
      if (!user?.id) return;
      try {
        const [result, count] = await Promise.all([
          fetchNotifications(page, NOTIFICATIONS_PAGE_SIZE),
          fetchUnreadCount(),
        ]);
        setUnread(count);
        setTotal(result.totalElements);
        setNotifications((prev) => {
          if (!append) return result.content;
          const seen = new Set(prev.map((n) => n.id));
          return [...prev, ...result.content.filter((n) => !seen.has(n.id))];
        });
      } catch {
        // Transient failure (e.g. notification service down) — keep the last known state.
      }
    },
    [user?.id]
  );

  const refresh = useCallback(() => {
    nextPageRef.current = 1;
    void load(0, false);
  }, [load]);

  const loadMore = useCallback(() => {
    if (loadingMore || notifications.length >= total) return;
    setLoadingMore(true);
    void load(nextPageRef.current, true).finally(() => {
      nextPageRef.current += 1;
      setLoadingMore(false);
    });
  }, [load, loadingMore, notifications.length, total]);

  // Initial load with skeleton state + one-time cleanup of the legacy
  // localStorage read-state key (read state now lives on the server).
  useEffect(() => {
    localStorage.removeItem("sa_read_notifications");
    setLoading(true);
    void load(0, false).finally(() => setLoading(false));
  }, [load]);

  // Refetch when any consumer mutates read state (mark read / mark all read)
  useEffect(() => {
    const handler = () => refresh();
    window.addEventListener(NOTIFICATIONS_EVENT, handler);
    return () => window.removeEventListener(NOTIFICATIONS_EVENT, handler);
  }, [refresh]);

  // Light polling — keeps the badge fresh as events arrive asynchronously
  useEffect(() => {
    const id = window.setInterval(() => refresh(), 60_000);
    return () => window.clearInterval(id);
  }, [refresh]);

  return {
    notifications,
    unread,
    total,
    loading,
    loadingMore,
    hasMore: notifications.length < total,
    refresh,
    loadMore,
  };
}
