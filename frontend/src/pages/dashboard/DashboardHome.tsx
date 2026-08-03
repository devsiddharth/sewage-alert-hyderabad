import { Link } from "react-router-dom";
import {
  ArrowRight,
  Camera,
  CheckCircle2,
  Clock,
  FileText,
  Inbox,
  MapPin,
  Plus,
  Search,
} from "lucide-react";
import { Card } from "@/components/ui/Card";
import { StatCard } from "@/components/ui/StatCard";
import { ComplaintCard } from "@/components/complaints/ComplaintCard";
import { StatusBadge } from "@/components/ui/Badge";
import { Skeleton, EmptyState } from "@/components/ui/States";
import { useAuth } from "@/lib/auth";
import { useComplaints } from "@/hooks/useComplaints";
import { complaintCode, formatDateTime, timeAgo } from "@/lib/utils";
import { STATUS_META, type Complaint } from "@/types";

export function DashboardHome() {
  const { user } = useAuth();
  const { complaints, loading } = useComplaints({ userId: user?.id });

  const my = complaints ?? [];
  const resolved = my.filter((c) => c.status === "RESOLVED").length;
  const pending = my.filter((c) => c.status === "PENDING").length;
  const inProgress = my.filter((c) => c.status === "IN_PROGRESS").length;

  // Derive an activity feed from complaint history (status changes).
  const activity = my
    .flatMap((c) =>
      c.history.map((h) => ({
        at: h.updatedAt,
        complaint: c,
        status: h.status,
        remarks: h.remarks,
      }))
    )
    .sort((a, b) => new Date(b.at).getTime() - new Date(a.at).getTime())
    .slice(0, 6);

  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

  return (
    <div className="space-y-8">
      {/* Welcome */}
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">
            {greeting}, {user?.name.split(" ")[0]}
          </h1>
          <p className="mt-1 text-muted">Here&apos;s what&apos;s happening with your reports.</p>
        </div>
        <Link
          to="/dashboard/report"
          className="inline-flex h-11 items-center gap-2 rounded-xl bg-brand px-5 text-sm font-semibold text-white transition-all duration-200 hover:bg-brand-light"
        >
          <Plus className="h-4 w-4" /> Report New Issue
        </Link>
      </div>

      {/* Quick actions */}
      <div className="grid gap-4 sm:grid-cols-3">
        {[
          { to: "/dashboard/report", icon: <Camera className="h-5 w-5" />, title: "Report new issue", desc: "Photo + GPS in under a minute" },
          { to: "/track", icon: <Search className="h-5 w-5" />, title: "Track a complaint", desc: "Enter an ID to see live status" },
          { to: "/dashboard/complaints", icon: <FileText className="h-5 w-5" />, title: "My complaints", desc: "Search, filter and follow up" },
        ].map((a) => (
          <Link
            key={a.to}
            to={a.to}
            className="group flex items-center gap-4 rounded-2xl border border-line bg-white p-5 shadow-card transition-all duration-200 hover:-translate-y-0.5 hover:border-accent/60 hover:shadow-lift"
          >
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-accent-soft text-brand transition-colors duration-200 group-hover:bg-brand group-hover:text-white">
              {a.icon}
            </span>
            <span className="min-w-0 flex-1">
              <span className="block text-[15px] font-semibold text-ink">{a.title}</span>
              <span className="mt-0.5 block truncate text-sm text-muted">{a.desc}</span>
            </span>
            <ArrowRight className="h-4 w-4 shrink-0 text-muted transition-transform duration-200 group-hover:translate-x-0.5 group-hover:text-brand" />
          </Link>
        ))}
      </div>

      {/* Stats */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Total reports" value={loading ? "…" : my.length} icon={<FileText className="h-5 w-5" />} tone="brand" />
        <StatCard label="Resolved" value={loading ? "…" : resolved} icon={<CheckCircle2 className="h-5 w-5" />} tone="green" />
        <StatCard label="In progress" value={loading ? "…" : inProgress} icon={<Clock className="h-5 w-5" />} tone="blue" />
        <StatCard label="Pending" value={loading ? "…" : pending} icon={<Inbox className="h-5 w-5" />} tone="amber" />
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.5fr_1fr]">
        {/* Recent complaints */}
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-ink">Recent complaints</h2>
            <Link to="/dashboard/complaints" className="inline-flex items-center gap-1 text-sm font-semibold text-brand hover:underline">
              View all <ArrowRight className="h-3.5 w-3.5" />
            </Link>
          </div>
          {loading ? (
            <div className="space-y-3">
              {[0, 1, 2].map((i) => (
                <Skeleton key={i} className="h-32 w-full rounded-2xl" />
              ))}
            </div>
          ) : my.length > 0 ? (
            <div className="space-y-3">
              {my.slice(0, 4).map((c) => (
                <ComplaintCard key={c.id} complaint={c} basePath="/dashboard/complaints" />
              ))}
            </div>
          ) : (
            <Card>
              <EmptyState
                icon={<MapPin className="h-6 w-6" />}
                title="No complaints yet"
                description="Spotted an issue? Report it — it takes less than a minute."
                action={
                  <Link to="/dashboard/report" className="inline-flex h-10 items-center gap-2 rounded-xl bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-light">
                    <Plus className="h-4 w-4" /> Report your first issue
                  </Link>
                }
              />
            </Card>
          )}
        </div>

        {/* Activity */}
        <div>
          <h2 className="mb-4 text-lg font-semibold text-ink">Recent activity</h2>
          <Card className="p-5">
            {loading ? (
              <div className="space-y-3">
                {[0, 1, 2].map((i) => (
                  <Skeleton key={i} className="h-12 w-full" />
                ))}
              </div>
            ) : activity.length > 0 ? (
              <ul className="space-y-5">
                {activity.map((a, i) => (
                  <ActivityRow key={i} complaint={a.complaint} status={a.status} at={a.at} remarks={a.remarks} />
                ))}
              </ul>
            ) : (
              <p className="py-8 text-center text-sm text-muted">Updates on your complaints will appear here.</p>
            )}
          </Card>
        </div>
      </div>
    </div>
  );
}

function ActivityRow({
  complaint,
  status,
  at,
  remarks,
}: {
  complaint: Complaint;
  status: string;
  at: string;
  remarks: string | null;
}) {
  const meta = STATUS_META[status as keyof typeof STATUS_META] ?? { label: status };
  return (
    <li className="flex gap-3">
      <span className="mt-1 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-accent-soft">
        <Clock className="h-4 w-4 text-brand" aria-hidden />
      </span>
      <div className="min-w-0 flex-1">
        <p className="text-sm text-ink">
          <span className="font-mono font-semibold text-brand">{complaintCode(complaint.id)}</span>{" "}
          moved to <span className="font-semibold">{meta.label}</span>
        </p>
        {remarks && <p className="mt-0.5 line-clamp-1 text-xs text-muted">{remarks}</p>}
        <p className="mt-0.5 text-xs text-muted" title={formatDateTime(at)}>{timeAgo(at)}</p>
      </div>
      <StatusBadge status={status} className="shrink-0" />
    </li>
  );
}
