import { useMemo, useState } from "react";
import { CheckCircle2, Clock, FileText, MapPin, TrendingUp } from "lucide-react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Card } from "@/components/ui/Card";
import { StatCard } from "@/components/ui/StatCard";
import { Skeleton } from "@/components/ui/States";
import { useComplaints } from "@/hooks/useComplaints";
import { STATUS_META, type ComplaintStatus } from "@/types";
import { cn } from "@/lib/cn";

type Range = "daily" | "weekly" | "monthly";

const PIE_COLORS: Record<ComplaintStatus, string> = {
  PENDING: "#F59E0B",
  IN_PROGRESS: "#7692FF",
  RESOLVED: "#55D6BE",
  REJECTED: "#EF4444",
};

export function AnalyticsPage() {
  const { complaints, loading } = useComplaints();
  const all = complaints ?? [];
  const [range, setRange] = useState<Range>("daily");

  const stats = useMemo(() => {
    const resolved = all.filter((c) => c.status === "RESOLVED").length;
    const rate = all.length ? Math.round((resolved / all.length) * 100) : 0;
    const durations = all
      .filter((c) => c.status === "RESOLVED")
      .map((c) => (new Date(c.updatedAt).getTime() - new Date(c.createdAt).getTime()) / 86_400_000)
      .filter((d) => d >= 0);
    return {
      total: all.length,
      resolved,
      rate,
      avg: durations.length ? durations.reduce((a, b) => a + b, 0) / durations.length : null,
    };
  }, [all]);

  const rangeData = useMemo(() => {
    const buckets: { label: string; count: number }[] = [];
    const now = new Date();
    if (range === "daily") {
      for (let i = 29; i >= 0; i--) {
        const d = new Date(now);
        d.setDate(d.getDate() - i);
        buckets.push({
          label: d.toLocaleDateString("en-IN", { day: "numeric", month: "short" }),
          count: all.filter((c) => new Date(c.createdAt).toDateString() === d.toDateString()).length,
        });
      }
    } else if (range === "weekly") {
      for (let i = 11; i >= 0; i--) {
        const end = new Date(now);
        end.setDate(end.getDate() - i * 7);
        const start = new Date(end);
        start.setDate(start.getDate() - 6);
        buckets.push({
          label: `${start.toLocaleDateString("en-IN", { day: "numeric", month: "short" })} – ${end.toLocaleDateString("en-IN", { day: "numeric", month: "short" })}`,
          count: all.filter((c) => {
            const t = new Date(c.createdAt).getTime();
            return t >= start.getTime() && t <= end.getTime() + 86_400_000;
          }).length,
        });
      }
    } else {
      for (let i = 11; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
        buckets.push({
          label: d.toLocaleDateString("en-IN", { month: "short", year: "2-digit" }),
          count: all.filter((c) => {
            const t = new Date(c.createdAt);
            return t.getMonth() === d.getMonth() && t.getFullYear() === d.getFullYear();
          }).length,
        });
      }
    }
    return buckets;
  }, [all, range]);

  const statusDist = useMemo(() => {
    const counts = { PENDING: 0, IN_PROGRESS: 0, RESOLVED: 0, REJECTED: 0 };
    all.forEach((c) => {
      counts[c.status] = (counts[c.status] ?? 0) + 1;
    });
    return (Object.keys(counts) as ComplaintStatus[])
      .map((s) => ({ name: STATUS_META[s].label, value: counts[s], color: PIE_COLORS[s] }))
      .filter((d) => d.value > 0);
  }, [all]);

  const hotspots = useMemo(() => {
    const map = new Map<string, number>();
    all.forEach((c) => {
      const key = `${c.latitude.toFixed(2)}, ${c.longitude.toFixed(2)}`;
      map.set(key, (map.get(key) ?? 0) + 1);
    });
    return Array.from(map.entries())
      .map(([area, count]) => ({ area, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 5);
  }, [all]);

  const maxCount = Math.max(1, ...rangeData.map((d) => d.count));

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Analytics</h1>
        <p className="mt-1 text-muted">Resolution performance and reporting patterns.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Resolution rate" value={loading ? "…" : `${stats.rate}%`} icon={<TrendingUp className="h-5 w-5" />} tone="green" />
        <StatCard label="Avg. resolution time" value={loading || !stats.avg ? "—" : `${stats.avg.toFixed(1)} days`} icon={<Clock className="h-5 w-5" />} tone="blue" />
        <StatCard label="Total complaints" value={loading ? "…" : stats.total} icon={<FileText className="h-5 w-5" />} tone="brand" />
        <StatCard label="Resolved" value={loading ? "…" : stats.resolved} icon={<CheckCircle2 className="h-5 w-5" />} tone="red" />
      </div>

      <div className="grid gap-6 lg:grid-cols-[1.7fr_1fr]">
        <Card className="p-6">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold text-ink">Reports over time</h2>
              <p className="mt-0.5 text-sm text-muted">New complaints per {range.replace("ly", "")}</p>
            </div>
            <div className="flex rounded-xl border border-line bg-white p-1">
              {(["daily", "weekly", "monthly"] as const).map((r) => (
                <button
                  key={r}
                  onClick={() => setRange(r)}
                  className={cn(
                    "rounded-lg px-3 py-1.5 text-sm font-medium capitalize transition-colors",
                    range === r ? "bg-brand text-white" : "text-muted hover:text-ink"
                  )}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>
          <div className="mt-6 h-72">
            {loading ? (
              <Skeleton className="h-full w-full" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={rangeData} margin={{ top: 4, right: 4, left: -18, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E5E7EB" vertical={false} />
                  <XAxis
                    dataKey="label"
                    tick={{ fontSize: 10, fill: "#6B7280" }}
                    tickLine={false}
                    axisLine={false}
                    interval={range === "daily" ? 4 : 0}
                    angle={range === "weekly" ? -25 : 0}
                    textAnchor={range === "weekly" ? "end" : "middle"}
                    height={range === "weekly" ? 55 : 30}
                  />
                  <YAxis tick={{ fontSize: 11, fill: "#6B7280" }} tickLine={false} axisLine={false} allowDecimals={false} />
                  <Tooltip cursor={{ fill: "#F1F5F9" }} contentStyle={{ borderRadius: 12, border: "1px solid #E5E7EB", fontSize: 12 }} />
                  <Bar dataKey="count" name="Complaints" radius={[6, 6, 0, 0]} maxBarSize={36}>
                    {rangeData.map((d, i) => (
                      <Cell key={i} fill={d.count >= maxCount * 0.8 ? "#0A2463" : "#7692FF"} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>

        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">Status distribution</h2>
          <p className="mt-0.5 text-sm text-muted">All complaints by current status</p>
          <div className="mt-4 h-56">
            {loading ? (
              <Skeleton className="h-full w-full rounded-full" />
            ) : statusDist.length === 0 ? (
              <p className="pt-16 text-center text-sm text-muted">No data yet</p>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={statusDist} dataKey="value" nameKey="name" innerRadius={55} outerRadius={85} paddingAngle={3} strokeWidth={0}>
                    {statusDist.map((entry) => (
                      <Cell key={entry.name} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ borderRadius: 12, border: "1px solid #E5E7EB", fontSize: 12 }} />
                  <Legend iconType="circle" iconSize={8} formatter={(v) => <span className="text-xs text-ink">{v}</span>} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </Card>
      </div>

      <Card className="p-6">
        <h2 className="text-base font-semibold text-ink">Most reported areas</h2>
        <p className="mt-0.5 text-sm text-muted">Hotspots by GPS coordinates (rounded to ~1 km)</p>
        {loading ? (
          <Skeleton className="mt-4 h-40 w-full" />
        ) : hotspots.length === 0 ? (
          <p className="py-10 text-center text-sm text-muted">No complaints to analyse yet.</p>
        ) : (
          <ul className="mt-5 space-y-3">
            {hotspots.map((h, i) => (
              <li key={h.area} className="flex items-center gap-4">
                <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-accent-soft text-xs font-bold text-brand">
                  {i + 1}
                </span>
                <MapPin className="h-4 w-4 shrink-0 text-muted" aria-hidden />
                <span className="w-32 shrink-0 font-mono text-sm font-medium text-ink">{h.area}</span>
                <div className="h-2.5 flex-1 overflow-hidden rounded-full bg-canvas">
                  <div
                    className="h-full rounded-full bg-brand transition-all duration-500"
                    style={{ width: `${(h.count / hotspots[0].count) * 100}%` }}
                  />
                </div>
                <span className="w-10 text-right text-sm font-semibold text-ink">{h.count}</span>
              </li>
            ))}
          </ul>
        )}
      </Card>
    </div>
  );
}
