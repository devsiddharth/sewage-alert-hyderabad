import { useCallback, useEffect, useState } from "react";
import {
  CheckCircle2, XCircle, Mail, Phone, MapPin, Building2,
  Ban, RotateCcw, Globe, Users,
  ChevronRight, Search, AlertCircle, RefreshCw,
} from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import type { NgoOrganization, NgoApplicationStatus } from "@/types";

/* ── Status Configuration ────────────────────────────────────────────────── */

const STATUS_CONFIG: Record<NgoApplicationStatus, {
  label: string;
  tone: "slate" | "blue" | "green" | "red" | "amber";
  dotClass: string;
}> = {
  PENDING: { label: "Pending Review", tone: "slate", dotClass: "bg-slate-400" },
  UNDER_REVIEW: { label: "Under Review", tone: "blue", dotClass: "bg-blue-500" },
  APPROVED: { label: "Approved", tone: "green", dotClass: "bg-emerald-500" },
  REJECTED: { label: "Rejected", tone: "red", dotClass: "bg-red-500" },
  SUSPENDED: { label: "Suspended", tone: "amber", dotClass: "bg-amber-500" },
};

type FilterTab = "ALL" | NgoApplicationStatus;
const TABS: { key: FilterTab; label: string }[] = [
  { key: "ALL", label: "All" },
  { key: "PENDING", label: "Pending" },
  { key: "APPROVED", label: "Approved" },
  { key: "REJECTED", label: "Rejected" },
  { key: "SUSPENDED", label: "Suspended" },
];

/* ── Main Component ──────────────────────────────────────────────────────── */

export function AdminNgoApplications() {
  const { toast } = useToast();
  const [apps, setApps] = useState<NgoOrganization[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<FilterTab>("ALL");
  const [selected, setSelected] = useState<NgoOrganization | null>(null);
  const [showDetail, setShowDetail] = useState(false);
  const [rejectReason, setRejectReason] = useState("");
  const [actionLoading, setActionLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<NgoOrganization[]>("/api/v1/ngo/admin/all");
      setApps(res);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load applications");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const filtered = apps.filter((a) => {
    const matchesFilter = filter === "ALL" || a.status === filter;
    const q = searchQuery.toLowerCase();
    const matchesSearch = !q ||
      a.organizationName.toLowerCase().includes(q) ||
      a.officialEmail.toLowerCase().includes(q) ||
      (a.address && a.address.toLowerCase().includes(q)) ||
      (a.contactPersonName && a.contactPersonName.toLowerCase().includes(q));
    return matchesFilter && matchesSearch;
  });

  const statusCounts: Record<string, number> = {
    ALL: apps.length,
    PENDING: apps.filter((a) => a.status === "PENDING").length,
    UNDER_REVIEW: apps.filter((a) => a.status === "UNDER_REVIEW").length,
    APPROVED: apps.filter((a) => a.status === "APPROVED").length,
    REJECTED: apps.filter((a) => a.status === "REJECTED").length,
    SUSPENDED: apps.filter((a) => a.status === "SUSPENDED").length,
  };

  /* ── Actions ─────────────────────────────────────────────────────────── */

  const handleApprove = async (id: number) => {
    setActionLoading(true);
    try {
      await api.post(`/api/v1/ngo/admin/${id}/approve`);
      toast("success", "Approved", "NGO account created. They can now log in with their credentials.");
      setShowDetail(false);
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not approve.");
    } finally { setActionLoading(false); }
  };

  const handleReject = async (id: number) => {
    if (!rejectReason.trim()) {
      toast("error", "Reason Required", "Please enter a reason for rejection.");
      return;
    }
    setActionLoading(true);
    try {
      await api.post(`/api/v1/ngo/admin/${id}/reject`, { rejectionReason: rejectReason });
      toast("success", "Rejected", "NGO application has been rejected.");
      setShowDetail(false);
      setRejectReason("");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not reject.");
    } finally { setActionLoading(false); }
  };

  const handleSuspend = async (id: number) => {
    if (!confirm("Suspend this NGO? They will lose dashboard access.")) return;
    setActionLoading(true);
    try {
      await api.post(`/api/v1/ngo/admin/${id}/suspend`, {});
      toast("success", "Suspended", "NGO has been suspended.");
      setShowDetail(false);
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not suspend.");
    } finally { setActionLoading(false); }
  };

  const handleReactivate = async (id: number) => {
    setActionLoading(true);
    try {
      await api.post(`/api/v1/ngo/admin/${id}/reactivate`);
      toast("success", "Reactivated", "NGO has been reactivated.");
      setShowDetail(false);
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not reactivate.");
    } finally { setActionLoading(false); }
  };

  const openDetail = (app: NgoOrganization) => {
    setSelected(app);
    setRejectReason(app.rejectionReason || "");
    setShowDetail(true);
  };

  /* ── Render ──────────────────────────────────────────────────────────── */

  return (
    <div className="min-h-full">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-ink">NGO Applications</h1>
            <p className="mt-1 text-sm text-muted">Review and manage NGO registration applications.</p>
          </div>
          <span className="text-sm text-muted">{apps.length} total</span>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {[
            { key: "PENDING", label: "Pending", color: "text-amber-600", border: "border-amber-200", bg: "bg-amber-50/50" },
            { key: "APPROVED", label: "Approved", color: "text-emerald-600", border: "border-emerald-200", bg: "bg-emerald-50/50" },
            { key: "REJECTED", label: "Rejected", color: "text-red-600", border: "border-red-200", bg: "bg-red-50/50" },
            { key: "SUSPENDED", label: "Suspended", color: "text-amber-600", border: "border-amber-200", bg: "bg-amber-50/50" },
          ].map((s) => (
            <button
              key={s.key}
              onClick={() => setFilter(s.key as FilterTab)}
              className={`rounded-xl border p-4 text-left transition-all hover:shadow-sm ${s.border} ${s.bg} ${filter === s.key ? "ring-2 ring-brand/20" : ""}`}
            >
              <p className="text-xs font-medium text-muted uppercase tracking-wider">{s.label}</p>
              <p className={`mt-1 text-2xl font-bold ${s.color}`}>{statusCounts[s.key] ?? 0}</p>
            </button>
          ))}
        </div>

        {/* Search + Tabs */}
        <div className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted" />
            <input
              type="text"
              placeholder="Search by name, email, or address..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full rounded-xl border border-line bg-white pl-10 pr-4 py-2.5 text-sm text-ink placeholder:text-muted/60 focus:outline-none focus:ring-2 focus:ring-brand/20 focus:border-brand/40 transition-colors"
            />
          </div>
          <div className="flex gap-1.5 overflow-x-auto pb-0.5">
            {TABS.map((tab) => (
              <button
                key={tab.key}
                onClick={() => setFilter(tab.key)}
                className={`shrink-0 rounded-lg px-3 py-2 text-sm font-medium transition-all ${
                  filter === tab.key
                    ? "bg-brand text-white shadow-sm"
                    : "border border-line text-muted hover:bg-canvas hover:text-ink"
                }`}
              >
                {tab.label}
                <span className={`ml-1 text-xs ${filter === tab.key ? "opacity-70" : "opacity-40"}`}>
                  ({statusCounts[tab.key] ?? 0})
                </span>
              </button>
            ))}
          </div>
        </div>

        {/* Error State */}
        {error && (
          <Card className="p-8 text-center">
            <AlertCircle className="mx-auto h-10 w-10 text-red-400" />
            <h3 className="mt-3 text-base font-semibold text-ink">Unable to load applications</h3>
            <p className="mt-1 text-sm text-muted">{error}</p>
            <Button variant="outline" size="sm" className="mt-4" onClick={() => void load()} icon={<RefreshCw className="h-4 w-4" />}>
              Retry
            </Button>
          </Card>
        )}

        {/* Loading */}
        {loading && !error && (
          <div className="space-y-3">
            {[0, 1, 2].map((i) => (
              <Card key={i} className="p-0">
                <div className="flex items-center gap-4 p-5">
                  <Skeleton className="h-11 w-11 rounded-xl shrink-0" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-44 rounded" />
                    <Skeleton className="h-3.5 w-64 rounded" />
                  </div>
                  <Skeleton className="h-7 w-24 rounded-lg shrink-0" />
                </div>
              </Card>
            ))}
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && filtered.length === 0 && (
          <Card className="p-12 text-center">
            <Building2 className="mx-auto h-10 w-10 text-muted/30" />
            <h3 className="mt-3 text-base font-semibold text-ink">No applications found</h3>
            <p className="mt-1 text-sm text-muted">
              {searchQuery
                ? `No results for "${searchQuery}". Try a different search.`
                : filter === "ALL"
                  ? "No NGO applications have been submitted yet."
                  : `No applications with "${TABS.find((t) => t.key === filter)?.label}" status.`}
            </p>
          </Card>
        )}

        {/* Application List */}
        {!loading && !error && filtered.length > 0 && (
          <div className="space-y-2">
            {filtered.map((app) => {
              const st = STATUS_CONFIG[app.status] ?? STATUS_CONFIG.PENDING;
              return (
                <button
                  key={app.id}
                  onClick={() => openDetail(app)}
                  className="w-full text-left group"
                >
                  <Card className="p-0 hover:shadow-lift transition-all cursor-pointer overflow-hidden group-hover:border-brand/20">
                    <div className="flex items-center gap-4 p-4 sm:p-5">
                      <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600 group-hover:bg-emerald-100 transition-colors">
                        <Building2 className="h-5 w-5" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="text-sm font-semibold text-ink truncate">{app.organizationName}</span>
                          <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-medium ring-1 ring-inset ${
                            st.tone === "green" ? "bg-emerald-50 text-emerald-700 ring-emerald-200" :
                            st.tone === "red" ? "bg-red-50 text-red-700 ring-red-200" :
                            st.tone === "amber" ? "bg-amber-50 text-amber-700 ring-amber-200" :
                            st.tone === "blue" ? "bg-blue-50 text-blue-700 ring-blue-200" :
                            "bg-slate-100 text-slate-700 ring-slate-200"
                          }`}>
                            <span className={`h-1.5 w-1.5 rounded-full ${st.dotClass}`} />
                            {st.label}
                          </span>
                        </div>
                        <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-0.5 text-xs text-muted">
                          <span className="inline-flex items-center gap-1">
                            <Mail className="h-3 w-3 shrink-0" /> {app.officialEmail}
                          </span>
                          {app.officialPhone && (
                            <span className="inline-flex items-center gap-1">
                              <Phone className="h-3 w-3 shrink-0" /> {app.officialPhone}
                            </span>
                          )}
                          {app.address && (
                            <span className="inline-flex items-center gap-1">
                              <MapPin className="h-3 w-3 shrink-0" /> {app.address}
                            </span>
                          )}
                        </div>
                      </div>
                      <ChevronRight className="h-4 w-4 text-muted/30 group-hover:text-muted shrink-0 transition-colors" />
                    </div>
                  </Card>
                </button>
              );
            })}
          </div>
        )}
      </div>

      {/* ── Detail Modal ────────────────────────────────────────────────── */}
      <Modal
        open={showDetail}
        onClose={() => setShowDetail(false)}
        title={selected?.organizationName ?? "NGO Application"}
        description="NGO Registration Application"
        size="xl"
        footer={selected ? (
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            <div className="flex-1">
              {selected.status === "PENDING" && (
                <Field label="" hint="Provide a reason if rejecting this application.">
                  <Textarea
                    rows={2}
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value)}
                    placeholder="Rejection reason (optional unless rejecting)..."
                    className="text-sm"
                  />
                </Field>
              )}
              {selected.status === "APPROVED" && (
                <p className="text-xs text-muted">This NGO is active and can access their dashboard.</p>
              )}
              {selected.status === "SUSPENDED" && (
                <p className="text-xs text-muted">This NGO cannot access their dashboard while suspended.</p>
              )}
              {selected.status === "REJECTED" && selected.rejectionReason && (
                <p className="text-xs text-red-600">Rejection reason: {selected.rejectionReason}</p>
              )}
            </div>
            <div className="flex gap-2 shrink-0">
              <Button variant="ghost" size="sm" onClick={() => setShowDetail(false)}>
                Close
              </Button>
              {selected.status === "PENDING" && (
                <>
                  <Button
                    variant="danger"
                    size="sm"
                    loading={actionLoading}
                    onClick={() => handleReject(selected.id)}
                    icon={<XCircle className="h-4 w-4" />}
                  >
                    Reject
                  </Button>
                  <Button
                    size="sm"
                    loading={actionLoading}
                    onClick={() => handleApprove(selected.id)}
                    icon={<CheckCircle2 className="h-4 w-4" />}
                  >
                    Approve
                  </Button>
                </>
              )}
              {selected.status === "APPROVED" && (
                <Button
                  variant="outline"
                  size="sm"
                  loading={actionLoading}
                  onClick={() => handleSuspend(selected.id)}
                  icon={<Ban className="h-4 w-4" />}
                  className="text-amber-600 border-amber-300 hover:bg-amber-50"
                >
                  Suspend
                </Button>
              )}
              {selected.status === "SUSPENDED" && (
                <Button
                  size="sm"
                  loading={actionLoading}
                  onClick={() => handleReactivate(selected.id)}
                  icon={<RotateCcw className="h-4 w-4" />}
                >
                  Reactivate
                </Button>
              )}
            </div>
          </div>
        ) : undefined}
      >
        {selected && (
          <div className="space-y-6">
            {/* Status Banner */}
            <div className={`flex items-center gap-2.5 rounded-xl px-4 py-3 ${
              selected.status === "APPROVED" ? "bg-emerald-50 text-emerald-700" :
              selected.status === "REJECTED" ? "bg-red-50 text-red-700" :
              selected.status === "SUSPENDED" ? "bg-amber-50 text-amber-700" :
              "bg-slate-50 text-slate-700"
            }`}>
              <span className={`h-2 w-2 rounded-full ${STATUS_CONFIG[selected.status]?.dotClass ?? "bg-slate-400"}`} />
              <span className="text-sm font-medium">{STATUS_CONFIG[selected.status]?.label ?? selected.status}</span>
              <span className="text-xs opacity-60 ml-auto">
                Applied {new Date(selected.createdAt).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}
              </span>
            </div>

            {/* Organization Info */}
            <section>
              <h3 className="text-[11px] font-semibold uppercase tracking-wider text-muted mb-3">Organization Information</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-4">
                <InfoField label="Official Email" value={selected.officialEmail} icon={<Mail className="h-3.5 w-3.5 text-muted shrink-0" />} />
                <InfoField label="Phone" value={selected.officialPhone} icon={<Phone className="h-3.5 w-3.5 text-muted shrink-0" />} />
                <InfoField label="Registration Number" value={selected.registrationNumber} />
                <InfoField label="Website" value={selected.website} isLink />
                <div className="sm:col-span-2">
                  <InfoField label="Address" value={selected.address} icon={<MapPin className="h-3.5 w-3.5 text-muted shrink-0" />} />
                </div>
                {selected.registrationDetails && (
                  <div className="sm:col-span-2">
                    <InfoField label="Registration Details" value={selected.registrationDetails} />
                  </div>
                )}
              </div>
            </section>

            <hr className="border-line" />

            {/* Mission & Focus */}
            <section>
              <h3 className="text-[11px] font-semibold uppercase tracking-wider text-muted mb-3">Mission & Focus</h3>
              <p className="text-sm text-ink leading-relaxed">
                {selected.mission || <span className="text-muted italic">No mission statement provided.</span>}
              </p>
              {(selected.areasOfFocus || selected.operatingAreas || selected.communitiesServed) && (
                <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 gap-x-8 gap-y-3">
                  {selected.areasOfFocus && <InfoField label="Areas of Focus" value={selected.areasOfFocus} />}
                  {selected.operatingAreas && <InfoField label="Operating Areas" value={selected.operatingAreas} />}
                  {selected.communitiesServed && (
                    <div className="sm:col-span-2">
                      <InfoField label="Communities Served" value={selected.communitiesServed} />
                    </div>
                  )}
                </div>
              )}
            </section>

            <hr className="border-line" />

            {/* Contact Person */}
            <section>
              <h3 className="text-[11px] font-semibold uppercase tracking-wider text-muted mb-3">Contact Person</h3>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-x-8 gap-y-3">
                <InfoField label="Name" value={selected.contactPersonName} icon={<Users className="h-3.5 w-3.5 text-muted shrink-0" />} />
                <InfoField label="Email" value={selected.contactPersonEmail} />
                <InfoField label="Phone" value={selected.contactPersonPhone} />
              </div>
            </section>

            {/* Rejection Reason (if rejected) */}
            {selected.status === "REJECTED" && selected.rejectionReason && (
              <>
                <hr className="border-line" />
                <div className="rounded-xl bg-red-50 border border-red-200 p-4">
                  <div className="flex items-center gap-2">
                    <XCircle className="h-4 w-4 text-red-500 shrink-0" />
                    <h4 className="text-xs font-semibold uppercase tracking-wider text-red-700">Rejection Reason</h4>
                  </div>
                  <p className="mt-2 text-sm text-red-600 leading-relaxed">{selected.rejectionReason}</p>
                </div>
              </>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}

/* ── Info Field Component ──────────────────────────────────────────────────── */

function InfoField({
  label,
  value,
  icon,
  isLink,
}: {
  label: string;
  value?: string | null;
  icon?: React.ReactNode;
  isLink?: boolean;
}) {
  return (
    <div>
      <p className="text-[11px] font-medium text-muted/70 uppercase tracking-wider mb-0.5">{label}</p>
      {value ? (
        isLink ? (
          <a
            href={value}
            target="_blank"
            rel="noopener noreferrer"
            className="text-sm text-brand hover:underline inline-flex items-center gap-1"
          >
            <Globe className="h-3 w-3 shrink-0" />
            {value}
          </a>
        ) : (
          <p className="text-sm text-ink flex items-center gap-1.5">
            {icon}
            {value}
          </p>
        )
      ) : (
        <p className="text-sm text-muted">—</p>
      )}
    </div>
  );
}
