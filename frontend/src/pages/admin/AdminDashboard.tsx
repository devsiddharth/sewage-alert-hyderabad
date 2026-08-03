import { useMemo } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, CheckCircle2, Clock, FileText, Inbox, Zap } from "lucide-react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Card } from "@/components/ui/Card";
import { StatCard } from "@/components/ui/StatCard";
import { StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/States";
import { useComplaints } from "@/hooks/useComplaints";
import { complaintCode, formatDateTime, timeAgo } from "@/lib/utils";

export function AdminDashboard() {
  const { complaints, loading } = useComplaints();
  const all = complaints ?? [];

  const stats = useMemo(() => {
    const resolved = all.filter((c) => c.status === "RESOLVED");
    const durations = resolved.map(
      (c) => (new Date(c.updatedAt).getTime() - new Date(c.createdAt).getTime()) / 86_400_000
    );
    return {
      total: all.length,
      resolved: resolved.length,
      inProgress: all.filter((c) => c.status === "IN_PROGRESS").length,
      pending: all.filter((c) => c.status === "PENDING").length,
      rejected: all.filter((c) => c.status === "REJECTED").length,
      avg: durations.length ? durations.reduce((a, b) => a + b, 0) / durations.length : null,
    };
  }, [all]);

  const trend = useMemo(() => {
    const days: { label: string; reports: number }[] = [];
    for (let i = 13; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const key = d.toDateString();
      days.push({
        label: d.toLocaleDateString("en-IN", { day: "numeric", month: "short" }),
        reports: all.filter((c) => new Date(c.createdAt).toDateString() === key).length,
      });
    }
    return days;
  }, [all]);

  const recent = [...all]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5);

  const pendingList = all.filter((c) => c.status === "PENDING").slice(0, 5);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Command centre</h1>
        <p className="mt-1 text-muted">Live overview of complaints across Hyderabad.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <StatCard label="Total complaints" value={loading ? "…" : stats.total} icon={<FileText className="h-5 w-5" />} tone="brand" />
        <StatCard label="Resolved" value={loading ? "…" : stats.resolved} icon={<CheckCircle2 className="h-5 w-5" />} tone="green" />
        <StatCard label="In progress" value={loading ? "…" : stats.inProgress} icon={<Clock className="h-5 w-5" />} tone="blue" />
        <StatCard label="Pending" value={loading ? "…" : stats.pending} icon={<Inbox className="h-5 w-5" />} tone="amber" />
        <StatCard
          label="Avg. resolution"
          value={loading || !stats.avg ? "—" : `${stats.avg.toFixed(1)}d`}
          icon={<Zap className="h-5 w-5" />}
          tone="red"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.6fr_1fr]">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-base font-semibold text-ink">Complaint trends</h2>
              <p className="mt-0.5 text-sm text-muted">New reports per day, last 14 days</p>
            </div>
          </div>
          <div className="mt-6 h-64">
            {loading ? (
              <Skeleton className="h-full w-full" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={trend} margin={{ top: 4, right: 4, left: -18, bottom: 0 }}>
                  <defs>
                    <linearGradient id="trendFill" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#7692FF" stopOpacity={0.35} />
                      <stop offset="100%" stopColor="#7692FF" stopOpacity={0.02} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" vertical={false} />
                  <XAxis dataKey="label" tick={{ fontSize: 11, fill: "#6B7280" }} tickLine={false} axisLine={false} interval={1} />
                  <YAxis tick={{ fontSize: 11, fill: "#6B7280" }} tickLine={false} axisLine={false} allowDecimals={false} />
                  <Tooltip
                    cursor={{ stroke: "#7692FF", strokeWidth: 1 }}
                    contentStyle={{ borderRadius: 12, border: "1px solid #E5E7EB", fontSize: 12, boxShadow: "0 8px 24px -8px rgb(10 36 99 / 0.16)" }}
                  />
                  <Area type="monotone" dataKey="reports" stroke="#0A2463" strokeWidth={2} fill="url(#trendFill)" />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>

        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">Needs attention</h2>
          <p className="mt-0.5 text-sm text-muted">Latest pending complaints</p>
          {loading ? (
            <div className="mt-4 space-y-3">
              {[0, 1, 2].map((i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : pendingList.length === 0 ? (
            <p className="mt-8 text-center text-sm text-muted">No pending complaints. 🎉</p>
          ) : (
            <ul className="mt-4 space-y-3">
              {pendingList.map((c) => (
                <li key={c.id} className="rounded-xl border border-line p-3.5">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-mono text-xs font-semibold text-brand">{complaintCode(c.id)}</span>
                    <StatusBadge status={c.status} />
                  </div>
                  <p className="mt-1.5 line-clamp-1 text-sm font-medium text-ink">{c.title}</p>
                  <p className="mt-1 text-xs text-muted">{timeAgo(c.createdAt)}</p>
                </li>
              ))}
            </ul>
          )}
          <Link to="/admin/complaints?status=PENDING" className="mt-4 inline-flex items-center gap-1 text-sm font-semibold text-brand hover:underline">
            Open queue <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </Card>
      </div>

      <Card>
        <div className="flex items-center justify-between border-b border-line px-6 py-4">
          <div>
            <h2 className="text-base font-semibold text-ink">Latest complaints</h2>
            <p className="mt-0.5 text-sm text-muted">Most recently reported</p>
          </div>
          <Link to="/admin/complaints" className="inline-flex items-center gap-1 text-sm font-semibold text-brand hover:underline">
            Manage all <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>
        {loading ? (
          <div className="space-y-3 p-6">
            {[0, 1, 2].map((i) => (
              <Skeleton key={i} className="h-14 w-full" />
            ))}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-line text-xs uppercase tracking-wide text-muted">
                  <th className="px-6 py-3 font-semibold">ID</th>
                  <th className="px-6 py-3 font-semibold">Title</th>
                  <th className="px-6 py-3 font-semibold">Status</th>
                  <th className="px-6 py-3 font-semibold">Priority</th>
                  <th className="px-6 py-3 font-semibold">Reported</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {recent.map((c) => (
                  <tr key={c.id} className="transition-colors hover:bg-canvas/60">
                    <td className="px-6 py-3.5 font-mono font-semibold text-brand">{complaintCode(c.id)}</td>
                    <td className="max-w-xs truncate px-6 py-3.5 font-medium text-ink">{c.title}</td>
                    <td className="px-6 py-3.5"><StatusBadge status={c.status} /></td>
                    <td className="px-6 py-3.5"><PriorityBadge priority={c.priority} /></td>
                    <td className="px-6 py-3.5 text-muted" title={formatDateTime(c.createdAt)}>{timeAgo(c.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}
