import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Filter, Plus, Search } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { ComplaintCard } from "@/components/complaints/ComplaintCard";
import { Pagination } from "@/components/ui/Pagination";
import { Skeleton, EmptyState } from "@/components/ui/States";
import { Select } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { useComplaints } from "@/hooks/useComplaints";
import type { ComplaintStatus } from "@/types";

const PAGE_SIZE = 6;

export function MyComplaints() {
  const { user } = useAuth();
  const { complaints, loading, error, reload } = useComplaints({ userId: user?.id });

  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<"ALL" | ComplaintStatus>("ALL");
  const [page, setPage] = useState(1);

  const filtered = useMemo(() => {
    const list = complaints ?? [];
    const q = query.trim().toLowerCase();
    return list.filter((c) => {
      const matchesQuery =
        !q || c.title.toLowerCase().includes(q) || c.description.toLowerCase().includes(q) || String(c.id).includes(q.replace("#sa-", "").replace("#", ""));
      const matchesStatus = status === "ALL" || c.status === status;
      return matchesQuery && matchesStatus;
    });
  }, [complaints, query, status]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage = Math.min(page, pageCount);
  const pageItems = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">My complaints</h1>
          <p className="mt-1 text-muted">
            {complaints ? `${complaints.length} total · ${filtered.length} shown` : "Loading your reports…"}
          </p>
        </div>
        <Link
          to="/dashboard/report"
          className="inline-flex h-11 items-center gap-2 rounded-xl bg-brand px-5 text-sm font-semibold text-white transition-colors hover:bg-brand-light"
        >
          <Plus className="h-4 w-4" /> New report
        </Link>
      </div>

      {/* Filters */}
      <Card className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
          <input
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(1); }}
            placeholder="Search by title, description or ID…"
            className="h-10 w-full rounded-xl border border-line bg-white pl-10 pr-3.5 text-sm transition-colors placeholder:text-muted/70 focus:border-accent focus:outline-none focus:ring-4 focus:ring-accent/15"
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter className="hidden h-4 w-4 text-muted sm:block" aria-hidden />
          <Select value={status} onChange={(e) => { setStatus(e.target.value as typeof status); setPage(1); }} className="w-full sm:w-44" aria-label="Filter by status">
            <option value="ALL">All statuses</option>
            <option value="PENDING">Submitted</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="RESOLVED">Resolved</option>
            <option value="REJECTED">Rejected</option>
          </Select>
        </div>
      </Card>

      {/* List */}
      {loading ? (
        <div className="space-y-3">
          {[0, 1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-32 w-full rounded-2xl" />
          ))}
        </div>
      ) : error ? (
        <Card>
          <EmptyState
            title="Couldn't load complaints"
            description={error}
            action={
              <button onClick={() => void reload()} className="rounded-xl border border-line bg-white px-4 py-2 text-sm font-semibold text-brand transition-colors hover:border-accent">
                Try again
              </button>
            }
          />
        </Card>
      ) : pageItems.length > 0 ? (
        <>
          <div className="space-y-3">
            {pageItems.map((c) => (
              <ComplaintCard key={c.id} complaint={c} basePath="/dashboard/complaints" />
            ))}
          </div>
          <Pagination page={safePage} pageCount={pageCount} onChange={setPage} total={filtered.length} />
        </>
      ) : (
        <Card>
          <EmptyState
            title={query || status !== "ALL" ? "No matching complaints" : "No complaints yet"}
            description={
              query || status !== "ALL"
                ? "Try changing your search or clearing the filter."
                : "Report your first issue and track it here."
            }
            action={
              query || status !== "ALL" ? undefined : (
                <Link to="/dashboard/report" className="inline-flex h-10 items-center gap-2 rounded-xl bg-brand px-4 text-sm font-semibold text-white transition-colors hover:bg-brand-light">
                  <Plus className="h-4 w-4" /> Report an issue
                </Link>
              )
            }
          />
        </Card>
      )}
    </div>
  );
}
