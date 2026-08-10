import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Eye, MapPin, RefreshCw } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { StatusBadge } from "@/components/ui/Badge";
import { EmptyState, Skeleton } from "@/components/ui/States";
import { Pagination } from "@/components/ui/Pagination";
import { useAuth } from "@/lib/auth";
import { fetchAssignedComplaints } from "@/services/assignment";
import { complaintCode, formatDateTime } from "@/lib/utils";
import type { Complaint } from "@/types";

const PAGE_SIZE = 8;

export function AssignedComplaints() {
  const { user } = useAuth();
  const [complaints, setComplaints] = useState<Complaint[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      setComplaints(await fetchAssignedComplaints());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load assigned complaints");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const stats = useMemo(() => {
    const list = complaints ?? [];
    return {
      total: list.length,
      inProgress: list.filter((c) => c.status === "IN_PROGRESS").length,
      pending: list.filter((c) => c.status === "PENDING").length,
      resolved: list.filter((c) => c.status === "RESOLVED").length,
    };
  }, [complaints]);

  const pageCount = Math.max(1, Math.ceil((complaints?.length ?? 0) / PAGE_SIZE));
  const safePage = Math.min(page, pageCount);
  const pageItems = (complaints ?? []).slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Assigned complaints</h1>
          <p className="mt-1 text-muted">
            {complaints ? `${complaints.length} complaint${complaints.length === 1 ? "" : "s"} assigned to you` : "Loading…"}
          </p>
        </div>
        <Button variant="outline" icon={<RefreshCw className="h-4 w-4" />} onClick={() => void load()} disabled={loading}>
          Refresh
        </Button>
      </div>

      {/* Summary strip */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {[
          { label: "Assigned", value: stats.total, tone: "text-ink" },
          { label: "Pending review", value: stats.pending, tone: "text-amber-600" },
          { label: "In progress", value: stats.inProgress, tone: "text-blue-600" },
          { label: "Resolved", value: stats.resolved, tone: "text-emerald-600" },
        ].map((s) => (
          <Card key={s.label} className="p-4">
            <p className={`text-2xl font-bold ${s.tone}`}>{s.value}</p>
            <p className="mt-0.5 text-xs font-medium uppercase tracking-wide text-muted">{s.label}</p>
          </Card>
        ))}
      </div>

      {/* Table */}
      <Card className="overflow-hidden">
        {loading ? (
          <div className="space-y-3 p-6">
            {[0, 1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-14 w-full" />
            ))}
          </div>
        ) : error ? (
          <EmptyState
            title="Couldn't load assigned complaints"
            description={error}
            action={
              <button
                onClick={() => void load()}
                className="rounded-xl border border-line bg-white px-4 py-2 text-sm font-semibold text-brand transition-colors hover:border-accent"
              >
                Try again
              </button>
            }
          />
        ) : pageItems.length === 0 ? (
          <EmptyState
            title="No complaints assigned yet"
            description="When an administrator assigns a complaint to you, it will appear here."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead>
                <tr className="border-b border-line text-xs uppercase tracking-wide text-muted">
                  <th className="px-4 py-3 font-semibold sm:px-6">Complaint</th>
                  <th className="px-4 py-3 font-semibold sm:px-6">Location</th>
                  <th className="px-4 py-3 font-semibold sm:px-6">Status</th>
                  <th className="px-4 py-3 text-right font-semibold sm:px-6">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {pageItems.map((c) => (
                  <tr key={c.id} className="transition-colors hover:bg-canvas/60">
                    <td className="px-4 py-3.5 sm:px-6">
                      <p className="font-mono font-semibold text-brand">{complaintCode(c.id)}</p>
                      <p className="mt-0.5 max-w-[240px] truncate text-xs text-muted" title={c.description}>
                        {c.title}
                      </p>
                    </td>
                    <td className="px-4 py-3.5 text-muted sm:px-6">
                      <span className="inline-flex items-center gap-1.5 font-mono text-xs">
                        <MapPin className="h-3.5 w-3.5 text-muted" aria-hidden />
                        {c.latitude.toFixed(4)}, {c.longitude.toFixed(4)}
                      </span>
                    </td>
                    <td className="px-4 py-3.5 sm:px-6">
                      <StatusBadge status={c.status} />
                      <p className="mt-1 text-[11px] text-muted" title={formatDateTime(c.updatedAt)}>
                        Updated {formatDateTime(c.updatedAt)}
                      </p>
                    </td>
                    <td className="px-4 py-3.5 sm:px-6">
                      <div className="flex justify-end">
                        <Link
                          to={`/officer/complaints/${c.id}`}
                          className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-semibold text-brand transition-colors hover:bg-canvas"
                        >
                          <Eye className="h-4 w-4" /> View
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {!loading && !error && complaints && complaints.length > 0 && (
          <Pagination page={safePage} pageCount={pageCount} onChange={setPage} total={complaints.length} />
        )}
      </Card>

      <p className="text-center text-xs text-muted">
        Signed in as <span className="font-semibold text-ink">{user?.name}</span> · You can only see complaints
        assigned to you.
      </p>
    </div>
  );
}
