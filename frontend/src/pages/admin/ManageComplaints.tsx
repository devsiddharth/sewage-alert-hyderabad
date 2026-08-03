import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ArrowDown, ArrowUp, CheckCircle2, Eye, Filter, Search } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import { EmptyState, Skeleton } from "@/components/ui/States";
import { Pagination } from "@/components/ui/Pagination";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";
import { useComplaints } from "@/hooks/useComplaints";
import { useAuth } from "@/lib/auth";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { complaintCode, formatDateTime, timeAgo } from "@/lib/utils";
import type { Complaint, ComplaintPriority, ComplaintStatus } from "@/types";

const PAGE_SIZE = 8;
const STATUSES: ComplaintStatus[] = ["PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"];
const PRIORITIES: ComplaintPriority[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

type SortKey = "createdAt" | "updatedAt" | "priority";

export function ManageComplaints() {
  const [params] = useSearchParams();
  const { toast } = useToast();
  const { complaints, loading, reload } = useComplaints();
  const user = useAuth().user;

  const [query, setQuery] = useState("");
  const [status, setStatus] = useState<"ALL" | ComplaintStatus>(
    (params.get("status") as ComplaintStatus) || "ALL"
  );

  // Keep the status filter in sync with ?status= deep links (e.g. from the admin dashboard).
  useEffect(() => {
    const fromParam = params.get("status") as ComplaintStatus | null;
    if (fromParam) setStatus(fromParam);
  }, [params]);
  const [priority, setPriority] = useState<"ALL" | ComplaintPriority>("ALL");
  const [sort, setSort] = useState<SortKey>("createdAt");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("desc");
  const [page, setPage] = useState(1);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [editing, setEditing] = useState<Complaint | null>(null);

  const [statusVal, setStatusVal] = useState<ComplaintStatus>("IN_PROGRESS");
  const [priorityVal, setPriorityVal] = useState<ComplaintPriority | "">("");
  const [remarks, setRemarks] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setPage(1);
  }, [query, status, priority]);

  const filtered = useMemo(() => {
    const list = complaints ?? [];
    const q = query.trim().toLowerCase();
    return list
      .filter((c) => {
        const matchesQuery =
          !q ||
          c.title.toLowerCase().includes(q) ||
          c.description.toLowerCase().includes(q) ||
          String(c.id).includes(q.replace("#sa-", "").replace("#", ""));
        const matchesStatus = status === "ALL" || c.status === status;
        const matchesPriority = priority === "ALL" || c.priority === priority;
        return matchesQuery && matchesStatus && matchesPriority;
      })
      .sort((a, b) => {
        let diff = 0;
        if (sort === "priority") {
          const order: Record<string, number> = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
          diff = (order[a.priority ?? ""] ?? 0) - (order[b.priority ?? ""] ?? 0);
        } else {
          diff = new Date(a[sort]).getTime() - new Date(b[sort]).getTime();
        }
        return sortDir === "asc" ? diff : -diff;
      });
  }, [complaints, query, status, priority, sort, sortDir]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const safePage = Math.min(page, pageCount);
  const pageItems = filtered.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const toggleSort = (key: SortKey) => {
    if (sort === key) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSort(key);
      setSortDir("desc");
    }
  };

  const openEdit = (c: Complaint) => {
    setEditing(c);
    setStatusVal(c.status);
    setPriorityVal(c.priority ?? "");
    setRemarks("");
  };

  const submitStatus = async () => {
    if (!editing) return;
    setSaving(true);
    try {
      await api.patch(`/api/v1/complaints/${editing.id}/status`, {
        status: statusVal,
        priority: priorityVal || null,
        remarks: remarks.trim() || null,
      });
      toast("success", "Complaint updated", `${complaintCode(editing.id)} → ${statusVal.replace("_", " ")}`);
      setEditing(null);
      void reload();
    } catch (e) {
      toast("error", "Update failed", e instanceof Error ? e.message : undefined);
    } finally {
      setSaving(false);
    }
  };

  const quickResolve = async (c: Complaint) => {
    try {
      await api.patch(`/api/v1/complaints/${c.id}/status`, {
        status: "RESOLVED",
        priority: c.priority,
        remarks: "Resolved by field team. Issue attended and fixed.",
      });
      toast("success", "Complaint resolved", complaintCode(c.id));
      void reload();
    } catch {
      toast("error", "Couldn't resolve complaint");
    }
  };

  const SortHeader = ({ label, k }: { label: string; k: SortKey }) => (
    <th className="px-4 py-3 font-semibold sm:px-6">
      <button onClick={() => toggleSort(k)} className="inline-flex items-center gap-1 uppercase tracking-wide hover:text-ink" aria-label={`Sort by ${label}`}>
        {label}
        {sort === k ? (sortDir === "asc" ? <ArrowUp className="h-3 w-3" /> : <ArrowDown className="h-3 w-3" />) : null}
      </button>
    </th>
  );

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Complaint management</h1>
        <p className="mt-1 text-muted">
          {complaints ? `${complaints.length} total · ${filtered.length} shown` : "Loading…"}
        </p>
      </div>

      {/* Filters */}
      <Card className="flex flex-col gap-3 p-4 lg:flex-row lg:items-center">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
          <Input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search by ID, title or description…" className="pl-10" />
        </div>
        <div className="flex items-center gap-2">
          <Filter className="hidden h-4 w-4 text-muted sm:block" aria-hidden />
          <Select value={status} onChange={(e) => setStatus(e.target.value as typeof status)} className="lg:w-40" aria-label="Filter by status">
            <option value="ALL">All statuses</option>
            {STATUSES.map((s) => (
              <option key={s} value={s}>{s.replace("_", " ")}</option>
            ))}
          </Select>
          <Select value={priority} onChange={(e) => setPriority(e.target.value as typeof priority)} className="lg:w-40" aria-label="Filter by priority">
            <option value="ALL">All priorities</option>
            {PRIORITIES.map((p) => (
              <option key={p} value={p}>{p}</option>
            ))}
          </Select>
        </div>
      </Card>

      {/* Table */}
      <Card className="overflow-hidden">
        {loading ? (
          <div className="space-y-3 p-6">
            {[0, 1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-14 w-full" />
            ))}
          </div>
        ) : pageItems.length === 0 ? (
          <EmptyState
            title={query || status !== "ALL" || priority !== "ALL" ? "No matching complaints" : "No complaints yet"}
            description="Adjust filters or wait for citizens to report."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead>
                <tr className="border-b border-line text-xs uppercase tracking-wide text-muted">
                  <SortHeader label="ID" k="createdAt" />
                  <th className="px-4 py-3 font-semibold sm:px-6">Title</th>
                  <th className="px-4 py-3 font-semibold sm:px-6">Status</th>
                  <SortHeader label="Priority" k="priority" />
                  <th className="px-4 py-3 font-semibold sm:px-6">Reported</th>
                  <th className="px-4 py-3 text-right font-semibold sm:px-6">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {pageItems.map((c) => (
                  <tr key={c.id} className="transition-colors hover:bg-canvas/60">
                    <td className="px-4 py-3.5 font-mono font-semibold text-brand sm:px-6">{complaintCode(c.id)}</td>
                    <td className="max-w-[260px] px-4 py-3.5 sm:px-6">
                      <p className="truncate font-medium text-ink">{c.title}</p>
                      <p className="truncate text-xs text-muted">{c.description}</p>
                    </td>
                    <td className="px-4 py-3.5 sm:px-6"><StatusBadge status={c.status} /></td>
                    <td className="px-4 py-3.5 sm:px-6"><PriorityBadge priority={c.priority} /></td>
                    <td className="px-4 py-3.5 text-muted sm:px-6" title={formatDateTime(c.createdAt)}>{timeAgo(c.createdAt)}</td>
                    <td className="px-4 py-3.5 sm:px-6">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => setDetailId(c.id)}
                          className="rounded-lg p-2 text-muted transition-colors hover:bg-canvas hover:text-brand"
                          title="View details"
                          aria-label={`View ${complaintCode(c.id)} details`}
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        {c.status !== "RESOLVED" && c.status !== "REJECTED" && (
                          <Button size="sm" variant="outline" onClick={() => openEdit(c)}>
                            Update status
                          </Button>
                        )}
                        {c.status === "IN_PROGRESS" && (
                          <Button size="sm" variant="secondary" icon={<CheckCircle2 className="h-3.5 w-3.5" />} onClick={() => void quickResolve(c)}>
                            Resolve
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        <Pagination page={safePage} pageCount={pageCount} onChange={setPage} total={filtered.length} />
      </Card>

      {/* Detail modal */}
      <Modal open={detailId !== null} onClose={() => setDetailId(null)} title="Complaint details" size="lg">
        {detailId !== null && <ComplaintDetailView complaintId={detailId} onNotFound={() => setDetailId(null)} />}
      </Modal>

      {/* Status update modal */}
      <Modal
        open={editing !== null}
        onClose={() => setEditing(null)}
        title={`Update ${editing ? complaintCode(editing.id) : ""}`}
        description={`Assign priority and move the complaint forward. Acting as ${user?.name ?? "authority"}.`}
      >
        <div className="space-y-5">
          <Field label="Status" required>
            <Select value={statusVal} onChange={(e) => setStatusVal(e.target.value as ComplaintStatus)}>
              {STATUSES.map((s) => (
                <option key={s} value={s}>{s.replace("_", " ")}</option>
              ))}
            </Select>
          </Field>
          <Field label="Priority" hint="Leave unassigned if not yet triaged.">
            <Select value={priorityVal} onChange={(e) => setPriorityVal(e.target.value as ComplaintPriority | "")}>
              <option value="">Unassigned</option>
              {PRIORITIES.map((p) => (
                <option key={p} value={p}>{p}</option>
              ))}
            </Select>
          </Field>
          <Field label="Remarks" hint="Internal note visible to the citizen on the timeline.">
            <Textarea rows={3} value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="e.g. Assigned to Zone-5 field team." />
          </Field>
          <div className="flex justify-end gap-3">
            <Button variant="outline" onClick={() => setEditing(null)}>Cancel</Button>
            <Button onClick={() => void submitStatus()} loading={saving}>Save update</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
