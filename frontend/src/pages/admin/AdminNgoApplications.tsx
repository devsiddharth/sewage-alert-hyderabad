import { useCallback, useEffect, useState } from "react";
import {
  CheckCircle2, XCircle, Eye, Mail, Phone, MapPin, Building2,
} from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Field, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import type { NgoOrganization, NgoApplicationStatus } from "@/types";

const STATUS_BADGE: Record<NgoApplicationStatus, { label: string; tone: "slate" | "blue" | "green" | "red" | "amber" }> = {
  PENDING: { label: "Pending", tone: "slate" },
  UNDER_REVIEW: { label: "Under Review", tone: "blue" },
  APPROVED: { label: "Approved", tone: "green" },
  REJECTED: { label: "Rejected", tone: "red" },
  SUSPENDED: { label: "Suspended", tone: "amber" },
};

type FilterTab = "ALL" | NgoApplicationStatus;
const TABS: { key: FilterTab; label: string }[] = [
  { key: "ALL", label: "All" },
  { key: "PENDING", label: "Pending" },
  { key: "APPROVED", label: "Approved" },
  { key: "REJECTED", label: "Rejected" },
  { key: "SUSPENDED", label: "Suspended" },
];

export function AdminNgoApplications() {
  const { toast } = useToast();
  const [apps, setApps] = useState<NgoOrganization[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<FilterTab>("ALL");
  const [selected, setSelected] = useState<NgoOrganization | null>(null);
  const [showDetail, setShowDetail] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [actionLoading, setActionLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<NgoOrganization[]>("/api/v1/admin/ngo-applications");
      setApps(res);
    } catch { /* silent */ } finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const filtered = filter === "ALL" ? apps : apps.filter((a) => a.status === filter);

  const handleApprove = async (id: number) => {
    setActionLoading(true);
    try {
      await api.put(`/api/v1/admin/ngo-applications/${id}/approve`, {});
      toast("success", "Approved", "NGO application approved. Representative can now log in.");
      setShowDetail(false);
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not approve.");
    } finally { setActionLoading(false); }
  };

  const handleReject = async (id: number) => {
    if (!rejectReason.trim()) { toast("error", "Required", "Please provide a rejection reason."); return; }
    setActionLoading(true);
    try {
      await api.put(`/api/v1/admin/ngo-applications/${id}/reject`, { rejectionReason: rejectReason });
      toast("success", "Rejected", "NGO application has been rejected.");
      setShowDetail(false);
      setRejectReason("");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not reject.");
    } finally { setActionLoading(false); }
  };

  const handleSuspend = async (id: number) => {
    if (!confirm("Suspend this NGO? They will lose access to the dashboard.")) return;
    setActionLoading(true);
    try {
      await api.put(`/api/v1/admin/ngo-applications/${id}/suspend`, {});
      toast("success", "Suspended", "NGO has been suspended.");
      setShowDetail(false);
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not suspend.");
    } finally { setActionLoading(false); }
  };

  const openDetail = (app: NgoOrganization) => {
    setSelected(app);
    setRejectReason(app.rejectionReason || "");
    setShowDetail(true);
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink">NGO Applications</h1>
        <p className="mt-1 text-muted">Review and manage NGO registration applications.</p>
      </div>

      {/* Filter tabs */}
      <div className="flex flex-wrap gap-2">
        {TABS.map((tab) => {
          const count = tab.key === "ALL" ? apps.length : apps.filter((a) => a.status === tab.key).length;
          return (
            <button
              key={tab.key}
              onClick={() => setFilter(tab.key)}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${filter === tab.key ? "bg-brand text-white" : "border border-line text-muted hover:bg-canvas"}`}
            >
              {tab.label}
              <span className="ml-1.5 text-xs opacity-70">({count})</span>
            </button>
          );
        })}
      </div>

      {loading ? (
        <div className="space-y-3">{[0, 1, 2].map((i) => <Skeleton key={i} className="h-20 rounded-xl" />)}</div>
      ) : filtered.length === 0 ? (
        <Card className="p-12 text-center">
          <Building2 className="mx-auto h-10 w-10 text-muted/40" />
          <h3 className="mt-3 text-base font-semibold text-ink">No applications</h3>
          <p className="mt-1 text-sm text-muted">No NGO applications match this filter.</p>
        </Card>
      ) : (
        <div className="space-y-3">
          {filtered.map((app) => {
            const st = STATUS_BADGE[app.status] ?? STATUS_BADGE.PENDING;
            return (
              <Card key={app.id} className="flex flex-wrap items-center justify-between gap-4 p-4 hover:shadow-lift transition-all">
                <div className="flex items-center gap-4 min-w-0">
                  <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                    <Building2 className="h-5 w-5" />
                  </span>
                  <div className="min-w-0">
                    <p className="text-[15px] font-semibold text-ink truncate">{app.organizationName}</p>
                    <p className="text-xs text-muted flex items-center gap-1"><Mail className="h-3 w-3" /> {app.officialEmail} · <Phone className="h-3 w-3" /> {app.officialPhone || "—"}</p>
                    <p className="text-xs text-muted flex items-center gap-1"><MapPin className="h-3 w-3" /> {app.address || "No address"}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Badge tone={st.tone}>{st.label}</Badge>
                  <Button variant="outline" size="sm" onClick={() => openDetail(app)} icon={<Eye className="h-3.5 w-3.5" />}>Review</Button>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Detail Modal */}
      <Modal open={showDetail} onClose={() => setShowDetail(false)} title={selected ? selected.organizationName : "NGO Application"}>
        {selected && (
          <div className="space-y-4 text-sm">
              <div className="grid gap-3 sm:grid-cols-2">
                <div><span className="text-muted">Official Email:</span> <span className="font-medium text-ink">{selected.officialEmail}</span></div>
                <div><span className="text-muted">Phone:</span> <span className="font-medium text-ink">{selected.officialPhone || "—"}</span></div>
                <div><span className="text-muted">Registration #:</span> <span className="font-medium text-ink">{selected.registrationNumber || "—"}</span></div>
                <div><span className="text-muted">Website:</span> {selected.website ? <a href={selected.website} target="_blank" rel="noopener noreferrer" className="font-medium text-brand hover:underline">{selected.website}</a> : <span className="font-medium text-ink">—</span>}</div>
                <div className="sm:col-span-2"><span className="text-muted">Address:</span> <span className="font-medium text-ink">{selected.address || "—"}</span></div>
                <div className="sm:col-span-2"><span className="text-muted">Registration Details:</span> <span className="font-medium text-ink">{selected.registrationDetails || "—"}</span></div>
              </div>

              <div className="border-t border-line pt-4">
                <h3 className="font-semibold text-ink mb-2">Mission & Focus</h3>
                <p className="text-muted">{selected.mission || "No mission statement provided."}</p>
                <div className="mt-2 grid gap-2 sm:grid-cols-2">
                  <div><span className="text-muted">Areas of Focus:</span> {selected.areasOfFocus || "—"}</div>
                  <div><span className="text-muted">Operating Areas:</span> {selected.operatingAreas || "—"}</div>
                  <div><span className="text-muted">Communities Served:</span> {selected.communitiesServed || "—"}</div>
                </div>
              </div>

              <div className="border-t border-line pt-4">
                <h3 className="font-semibold text-ink mb-2">Contact Person</h3>
                <div className="grid gap-2 sm:grid-cols-3">
                  <div><span className="text-muted">Name:</span> {selected.contactPersonName || "—"}</div>
                  <div><span className="text-muted">Email:</span> {selected.contactPersonEmail || "—"}</div>
                  <div><span className="text-muted">Phone:</span> {selected.contactPersonPhone || "—"}</div>
                </div>
              </div>

              {selected.rejectionReason && (
                <div className="rounded-xl bg-red-50 border border-red-200 p-4">
                  <h4 className="text-sm font-semibold text-red-700">Rejection Reason</h4>
                  <p className="mt-1 text-sm text-red-600">{selected.rejectionReason}</p>
                </div>
              )}

              {selected.status === "PENDING" && (
                <div className="border-t border-line pt-4 space-y-4">
                  <Field label="Rejection Reason (if rejecting)">
                    <Textarea
                      rows={2}
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                      placeholder="Provide a reason if rejecting..."
                    />
                  </Field>
                  <div className="flex gap-3">
                    <Button
                      loading={actionLoading}
                      onClick={() => handleApprove(selected.id)}
                      icon={<CheckCircle2 className="h-4 w-4" />}
                      className="bg-emerald-600 hover:bg-emerald-700"
                    >
                      Approve
                    </Button>
                    <Button
                      variant="outline"
                      loading={actionLoading}
                      onClick={() => handleReject(selected.id)}
                      icon={<XCircle className="h-4 w-4" />}
                      className="text-red-600 border-red-300 hover:bg-red-50"
                    >
                      Reject
                    </Button>
                  </div>
                </div>
              )}

              {selected.status === "APPROVED" && (
                <div className="border-t border-line pt-4">
                  <Button variant="outline" loading={actionLoading} onClick={() => handleSuspend(selected.id)} className="text-amber-600 border-amber-300">
                    Suspend NGO
                  </Button>
                </div>
              )}
          </div>
        )}
      </Modal>
    </div>
  );
}
