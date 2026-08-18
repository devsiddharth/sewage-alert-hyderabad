import { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ArrowDown, ArrowUp, Camera, CheckCircle2, Eye, Filter, ImagePlus, MapPin, Search, UserRoundCheck, UserRoundX, X } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Badge, StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import { EmptyState, Skeleton } from "@/components/ui/States";
import { Pagination } from "@/components/ui/Pagination";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";
import { AssignOfficerModal } from "@/components/admin/AssignOfficerModal";
import { useComplaints } from "@/hooks/useComplaints";
import { api } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { assignComplaint, fetchFieldOfficers, resolveComplaint } from "@/services/assignment";
import { complaintCode, fileToCompressedFile, formatDateTime, timeAgo } from "@/lib/utils";
import { STATUS_META, type Complaint, type ComplaintPriority, type ComplaintStatus, type FieldOfficer } from "@/types";

const PAGE_SIZE = 8;
const STATUSES: ComplaintStatus[] = ["PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"];
const PRIORITIES: ComplaintPriority[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

type SortKey = "createdAt" | "updatedAt" | "priority";

// Defined at module scope (not inside the component) so its identity is stable
// across re-renders. A component declared inside the render body gets a new
// identity on every render, forcing React to unmount/remount the table header
// cells on each keystroke in the status-update modal.
function SortHeader({
  label,
  k,
  sort,
  sortDir,
  onToggle,
}: {
  label: string;
  k: SortKey;
  sort: SortKey;
  sortDir: "asc" | "desc";
  onToggle: (key: SortKey) => void;
}) {
  return (
    <th className="px-4 py-3 font-semibold sm:px-6">
      <button
        onClick={() => onToggle(k)}
        className="inline-flex items-center gap-1 uppercase tracking-wide hover:text-ink"
        aria-label={`Sort by ${label}`}
      >
        {label}
        {sort === k ? (sortDir === "asc" ? <ArrowUp className="h-3 w-3" /> : <ArrowDown className="h-3 w-3" />) : null}
      </button>
    </th>
  );
}

export function ManageComplaints() {
  const [params] = useSearchParams();
  const { toast } = useToast();
  const { complaints, loading, reload } = useComplaints();

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

  // Field officers for the assignment workflow
  const [officers, setOfficers] = useState<FieldOfficer[]>([]);
  const [officersLoading, setOfficersLoading] = useState(true);
  const [assignTarget, setAssignTarget] = useState<Complaint | null>(null);

  const officerName = useMemo(() => new Map(officers.map((o) => [o.id, o.name])), [officers]);

  useEffect(() => {
    let cancelled = false;
    fetchFieldOfficers()
      .then((list) => {
        if (!cancelled) setOfficers(list);
      })
      .catch(() => {
        // non-fatal — the table still renders; the modal will show an error state
      })
      .finally(() => {
        if (!cancelled) setOfficersLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const [statusVal, setStatusVal] = useState<ComplaintStatus>("IN_PROGRESS");
  const [priorityVal, setPriorityVal] = useState<ComplaintPriority | "">("");
  const [remarks, setRemarks] = useState("");
  const [proofFile, setProofFile] = useState<File | null>(null);
  const [proofError, setProofError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const proofFileRef = useRef<HTMLInputElement>(null);

  // Object-URL preview for the selected proof photo (revoked whenever it changes or unmounts).
  const proofPreviewUrl = useMemo(() => (proofFile ? URL.createObjectURL(proofFile) : null), [proofFile]);
  useEffect(() => () => {
    if (proofPreviewUrl) URL.revokeObjectURL(proofPreviewUrl);
  }, [proofPreviewUrl]);

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

  const openEdit = (c: Complaint, presetStatus?: ComplaintStatus) => {
    setEditing(c);
    setStatusVal(presetStatus ?? c.status);
    setPriorityVal(c.priority ?? "");
    setRemarks("");
    setProofFile(null);
    setProofError(null);
  };

  // Quick "Resolve" now opens the status modal pre-set to RESOLVED, where the admin must
  // attach the mandatory resolution-proof photo before the complaint can be resolved.
  const openResolve = (c: Complaint) => {
    openEdit(c, "RESOLVED");
  };

  const addProofImage = async (file: File | null) => {
    setProofError(null);
    if (!file) {
      setProofFile(null);
      return;
    }
    try {
      // Reuse the same client-side compression as complaint photos — the backend
      // accepts the compressed JPEG for the mandatory proof image too.
      setProofFile(await fileToCompressedFile(file));
    } catch {
      setProofFile(null);
      setProofError("That file couldn't be read as an image. Please choose a JPG, PNG or WEBP photo.");
    }
  };

  const submitStatus = async () => {
    if (!editing) return;

    // The resolution-proof photo is mandatory — the backend enforces this too, so a
    // missing photo here is blocked up-front with a clear message.
    if (statusVal === "RESOLVED" && !proofFile) {
      setProofError("A resolution photo is required before this complaint can be marked as resolved.");
      return;
    }

    setSaving(true);
    try {
      if (statusVal === "RESOLVED") {
        await resolveComplaint(editing.id, {
          remarks: remarks.trim() || "Resolved by field team. Issue attended and fixed.",
          priority: priorityVal || null,
          proofImage: proofFile!,
        });
        toast("success", "Complaint resolved", complaintCode(editing.id));
      } else {
        await api.patch(`/api/v1/complaints/${editing.id}/status`, {
          status: statusVal,
          priority: priorityVal || null,
          remarks: remarks.trim() || null,
        });
        toast("success", "Complaint updated", `${complaintCode(editing.id)} → ${STATUS_META[statusVal].label}`);
      }
      setEditing(null);
      void reload();
    } catch (e) {
      toast("error", "Update failed", e instanceof Error ? e.message : undefined);
    } finally {
      setSaving(false);
    }
  };

  const submitAssignment = async (complaintId: number, fieldOfficerId: number) => {
    try {
      const updated = await assignComplaint(complaintId, fieldOfficerId);
      const officer = officers.find((o) => o.id === fieldOfficerId);
      toast(
        "success",
        updated.assignedTo ? "Complaint reassigned" : "Complaint assigned",
        `${complaintCode(complaintId)} → ${officer?.name ?? `Officer #${fieldOfficerId}`}`
      );
      void reload();
    } catch (e) {
      toast("error", "Assignment failed", e instanceof Error ? e.message : undefined);
    }
  };

  return (
    <div className="space-y-5 sm:space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">Complaint management</h1>
        <p className="mt-1 text-muted">
          {complaints ? `${complaints.length} total · ${filtered.length} shown` : "Loading…"}
        </p>
      </div>

      {/* Filters */}
      <Card className="flex flex-col gap-3 p-3 sm:p-4 lg:flex-row lg:items-center">
        <div className="relative flex-1">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
          <Input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search by ID, title or description…" className="pl-10" />
        </div>
        <div className="flex items-center gap-2">
          <Filter className="hidden h-4 w-4 text-muted sm:block" aria-hidden />
          <Select value={status} onChange={(e) => setStatus(e.target.value as typeof status)} className="lg:w-40" aria-label="Filter by status">
            <option value="ALL">All statuses</option>
            {STATUSES.map((s) => (
              <option key={s} value={s}>{STATUS_META[s].label}</option>
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
            <table className="w-full min-w-[920px] text-left text-sm">
              <thead>
                <tr className="border-b border-line text-xs uppercase tracking-wide text-muted">
                  <SortHeader label="ID" k="createdAt" sort={sort} sortDir={sortDir} onToggle={toggleSort} />
                  <th className="px-3 py-3 font-semibold sm:px-4">Title</th>
                  <th className="hidden px-3 py-3 font-semibold sm:table-cell sm:px-4">Location</th>
                  <th className="px-3 py-3 font-semibold sm:px-4">Status</th>
                  <th className="hidden px-3 py-3 font-semibold sm:table-cell sm:px-4">Assigned to</th>
                  <SortHeader label="Priority" k="priority" sort={sort} sortDir={sortDir} onToggle={toggleSort} />
                  <th className="hidden px-3 py-3 font-semibold sm:table-cell sm:px-4">Reported</th>
                  <th className="px-3 py-3 text-right font-semibold sm:px-4">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {pageItems.map((c) => (
                  <tr key={c.id} className="transition-colors hover:bg-canvas/60">
                    <td className="px-3 py-3 font-mono text-xs font-semibold text-brand sm:px-4 sm:py-3.5 sm:text-sm">{complaintCode(c.id)}</td>
                    <td className="max-w-[160px] px-3 py-3 sm:max-w-[260px] sm:px-4 sm:py-3.5">
                      <p className="truncate text-sm font-medium text-ink">{c.title}</p>
                      <p className="truncate text-xs text-muted">{c.description}</p>
                    </td>
                    <td className="hidden px-3 py-3 sm:table-cell sm:px-4 sm:py-3.5">
                      <span className="inline-flex items-center gap-1.5 font-mono text-xs text-muted">
                        <MapPin className="h-3.5 w-3.5 shrink-0" aria-hidden />
                        {c.latitude.toFixed(4)}, {c.longitude.toFixed(4)}
                      </span>
                    </td>
                    <td className="px-3 py-3 sm:px-4 sm:py-3.5"><StatusBadge status={c.status} /></td>
                    <td className="hidden px-3 py-3 sm:table-cell sm:px-4 sm:py-3.5">
                      {officersLoading ? (
                        <Skeleton className="h-5 w-28 rounded-full" />
                      ) : c.assignedTo ? (
                        <span className="inline-flex items-center gap-1.5 text-sm font-medium text-ink">
                          <UserRoundCheck className="h-3.5 w-3.5 text-brand" aria-hidden />
                          {officerName.get(c.assignedTo) ?? `Officer #${c.assignedTo}`}
                        </span>
                      ) : (
                        <Badge tone="slate">
                          <UserRoundX className="h-3 w-3" aria-hidden /> Unassigned
                        </Badge>
                      )}
                    </td>
                    <td className="hidden px-3 py-3 sm:table-cell sm:px-4 sm:py-3.5"><PriorityBadge priority={c.priority} /></td>
                    <td className="hidden px-3 py-3 text-muted sm:table-cell sm:px-4 sm:py-3.5" title={formatDateTime(c.createdAt)}>{timeAgo(c.createdAt)}</td>
                    <td className="px-3 py-3 sm:px-4 sm:py-3.5">
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
                          <>
                            <Button
                              size="sm"
                              variant={c.assignedTo ? "ghost" : "outline"}
                              icon={<UserRoundCheck className="h-3.5 w-3.5" />}
                              onClick={() => setAssignTarget(c)}
                              title={c.assignedTo ? "Reassign to another officer" : "Assign a field officer"}
                            >
                              {c.assignedTo ? "Reassign" : "Assign"}
                            </Button>
                            <Button size="sm" variant="outline" onClick={() => openEdit(c)}>
                              Update status
                            </Button>
                          </>
                        )}
                        {c.status === "IN_PROGRESS" && (
                          <Button size="sm" variant="secondary" icon={<CheckCircle2 className="h-3.5 w-3.5" />} onClick={() => openResolve(c)}>
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

      {/* Assign / reassign modal */}
      <AssignOfficerModal
        open={assignTarget !== null}
        onClose={() => setAssignTarget(null)}
        complaint={assignTarget}
        officers={officers}
        officersLoading={officersLoading}
        onAssign={submitAssignment}
      />

      {/* Status update modal */}
      <Modal
        open={editing !== null}
        onClose={() => setEditing(null)}
        title={`${statusVal === "RESOLVED" ? "Resolve" : "Update"} ${editing ? complaintCode(editing.id) : ""}`}
        description={
          statusVal === "RESOLVED"
            ? "A resolution photo is required before this complaint can be marked as resolved."
            : "Assign priority and move the complaint forward."
        }
      >
        <div className="space-y-5">
          <Field label="Status" required>
            <Select
              value={statusVal}
              onChange={(e) => {
                setStatusVal(e.target.value as ComplaintStatus);
                setProofError(null);
              }}
            >
              {STATUSES.map((s) => (
                <option key={s} value={s}>{STATUS_META[s].label}</option>
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

          {statusVal === "RESOLVED" && (
            <Field
              label="Resolution photo"
              required
              hint="A clear photo of the fixed issue. JPG, PNG or WEBP."
            >
              {proofFile ? (
                <div className="flex items-center gap-3 rounded-xl border border-line bg-canvas p-3">
                  <img
                    src={proofPreviewUrl ?? undefined}
                    alt="Resolution proof preview"
                    className="h-16 w-16 shrink-0 rounded-lg object-cover"
                  />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-ink">{proofFile.name}</p>
                    <p className="text-xs text-muted">Ready to upload with the resolution.</p>
                  </div>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    aria-label="Remove resolution photo"
                    onClick={() => {
                      setProofFile(null);
                      setProofError(null);
                    }}
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              ) : (
                <button
                  type="button"
                  onClick={() => proofFileRef.current?.click()}
                  className="flex w-full items-center justify-center gap-2 rounded-xl border-2 border-dashed border-line bg-canvas px-4 py-5 text-sm font-medium text-muted transition-colors hover:border-accent hover:text-brand"
                >
                  <ImagePlus className="h-5 w-5" />
                  Upload resolution photo
                </button>
              )}
              <input
                ref={proofFileRef}
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="hidden"
                onChange={(e) => {
                  void addProofImage(e.target.files?.[0] ?? null);
                  e.target.value = "";
                }}
              />
              {proofError && (
                <p className="mt-1.5 flex items-center gap-1.5 text-sm font-medium text-red-600">
                  <Camera className="h-4 w-4 shrink-0" aria-hidden />
                  {proofError}
                </p>
              )}
            </Field>
          )}

          <div className="flex justify-end gap-3">
            <Button variant="outline" onClick={() => setEditing(null)}>Cancel</Button>
            <Button
              onClick={() => void submitStatus()}
              loading={saving}
              disabled={statusVal === "RESOLVED" && !proofFile}
              title={statusVal === "RESOLVED" && !proofFile ? "A resolution photo is required" : undefined}
            >
              {statusVal === "RESOLVED" ? "Resolve complaint" : "Save update"}
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
