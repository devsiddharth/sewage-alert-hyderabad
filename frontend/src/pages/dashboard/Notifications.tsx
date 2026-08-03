import { useState } from "react";
import { Link } from "react-router-dom";
import { Bell, BellRing, CheckCheck, CheckCircle2, Circle, Clock, FilePlus2, XCircle } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/States";
import { Button } from "@/components/ui/Button";
import { useNotifications } from "@/hooks/useNotifications";
import { isNotificationRead, markAllRead, markNotificationRead } from "@/lib/notifications";
import { complaintCode, timeAgo } from "@/lib/utils";
import { cn } from "@/lib/cn";

const typeMeta = {
  created: { icon: FilePlus2, tone: "bg-blue-50 text-blue-600" },
  assigned: { icon: Clock, tone: "bg-amber-50 text-amber-600" },
  updated: { icon: Bell, tone: "bg-accent-soft text-brand" },
  resolved: { icon: CheckCircle2, tone: "bg-emerald-50 text-emerald-600" },
  rejected: { icon: XCircle, tone: "bg-red-50 text-red-600" },
};

export function Notifications() {
  const { notifications, unread, loading } = useNotifications();
  const [, force] = useState(0);

  const [filter, setFilter] = useState<"ALL" | "UNREAD">("ALL");
  const visible = filter === "ALL" ? notifications : notifications.filter((n) => !isNotificationRead(n.id));

  const handleRead = (id: string) => {
    if (!isNotificationRead(id)) {
      markNotificationRead(id);
      window.dispatchEvent(new Event("sa:notifications"));
      force((v) => v + 1);
    }
  };

  const handleAllRead = () => {
    markAllRead(notifications.map((n) => n.id));
    window.dispatchEvent(new Event("sa:notifications"));
    force((v) => v + 1);
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Notifications</h1>
          <p className="mt-1 text-muted">
            {unread > 0 ? `${unread} unread update${unread === 1 ? "" : "s"}` : "You're all caught up"}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex rounded-xl border border-line bg-white p-1">
            {(["ALL", "UNREAD"] as const).map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={cn(
                  "rounded-lg px-3 py-1.5 text-sm font-medium transition-colors",
                  filter === f ? "bg-brand text-white" : "text-muted hover:text-ink"
                )}
              >
                {f === "ALL" ? "All" : `Unread (${unread})`}
              </button>
            ))}
          </div>
          <Button variant="outline" size="sm" icon={<CheckCheck className="h-4 w-4" />} onClick={handleAllRead} disabled={unread === 0}>
            Mark all read
          </Button>
        </div>
      </div>

      {loading ? (
        <div className="space-y-3">
          {[0, 1, 2].map((i) => (
            <Skeleton key={i} className="h-20 w-full rounded-2xl" />
          ))}
        </div>
      ) : visible.length === 0 ? (
        <Card>
          <div className="flex flex-col items-center px-6 py-16 text-center">
            <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-canvas text-muted">
              <BellRing className="h-7 w-7" />
            </div>
            <h2 className="text-base font-semibold text-ink">{filter === "UNREAD" ? "No unread notifications" : "No notifications yet"}</h2>
            <p className="mt-1 max-w-sm text-sm text-muted">
              Status updates about your complaints — assigned, in progress, resolved — will land here.
            </p>
            {filter === "UNREAD" && (
              <button onClick={() => setFilter("ALL")} className="mt-5 text-sm font-semibold text-brand hover:underline">
                Show all
              </button>
            )}
          </div>
        </Card>
      ) : (
        <ul className="space-y-3">
          {visible.map((n) => {
            const meta = typeMeta[n.type];
            const read = isNotificationRead(n.id);
            return (
              <li key={n.id}>
                <Link
                  to={`/dashboard/complaints/${n.complaintId}`}
                  onClick={() => handleRead(n.id)}
                  className={cn(
                    "flex items-start gap-4 rounded-2xl border bg-white p-4 transition-all duration-200 hover:border-accent/60 hover:shadow-card sm:p-5",
                    read ? "border-line" : "border-accent/40 shadow-card"
                  )}
                >
                  <span className={cn("mt-0.5 flex h-10 w-10 shrink-0 items-center justify-center rounded-xl", meta.tone)}>
                    <meta.icon className="h-5 w-5" aria-hidden />
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="text-sm font-semibold text-ink">{n.title}</p>
                      {!read && <span className="h-2 w-2 rounded-full bg-accent" aria-label="Unread" />}
                    </div>
                    <p className="mt-0.5 line-clamp-2 text-sm text-muted">{n.description}</p>
                    <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted">
                      <span className="font-mono font-semibold text-brand">{complaintCode(n.complaintId)}</span>
                      <span>{timeAgo(n.at)}</span>
                    </div>
                  </div>
                  {read ? (
                    <CheckCircle2 className="mt-1 h-4 w-4 shrink-0 text-muted/40" aria-hidden />
                  ) : (
                    <Circle className="mt-1 h-4 w-4 shrink-0 text-accent" aria-hidden />
                  )}
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
