import { useEffect, useState } from "react";
import { CalendarDays, ExternalLink, MapPin, UserRound } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import { ComplaintTimeline } from "@/components/ui/Timeline";
import { Skeleton } from "@/components/ui/States";
import { useComplaint } from "@/hooks/useComplaint";
import { complaintCode, formatDateTime } from "@/lib/utils";
import type { Complaint } from "@/types";

export function ComplaintDetailView({ complaintId, onNotFound }: { complaintId: number; onNotFound?: () => void }) {
  const { complaint, loading, error, notFound, reload } = useComplaint(complaintId);

  // Notify the parent (e.g. to close a modal) only once, outside the render phase.
  useEffect(() => {
    if (notFound) onNotFound?.();
  }, [notFound, onNotFound]);

  if (loading) {
    return (
      <div className="space-y-5">
        <Skeleton className="h-8 w-56" />
        <Skeleton className="h-32 w-full rounded-2xl" />
        <Skeleton className="h-64 w-full rounded-2xl" />
      </div>
    );
  }

  if (notFound || !complaint) {
    return (
      <Card className="p-10 text-center">
        <h2 className="text-lg font-semibold text-ink">Complaint not found</h2>
        <p className="mx-auto mt-2 max-w-sm text-sm text-muted">
          We couldn&apos;t find a complaint with that ID. Double-check the number — it looks like
          “#SA-1042”.
        </p>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className="p-10 text-center">
        <p className="text-sm font-medium text-red-600">{error}</p>
        <button onClick={() => void reload()} className="mt-4 rounded-xl border border-line px-4 py-2 text-sm font-semibold text-brand">
          Retry
        </button>
      </Card>
    );
  }

  return <ComplaintDetailBody complaint={complaint} />;
}

function ComplaintDetailBody({ complaint }: { complaint: Complaint }) {
  const [activeImage, setActiveImage] = useState(0);
  const images = complaint.imageUrls ?? [];

  return (
    <div className="grid gap-5 sm:gap-6 lg:grid-cols-[1.6fr_1fr]">
      <div className="space-y-6">
        {/* Main info */}
        <Card className="p-4 sm:p-6 lg:p-7">
          <div className="flex flex-wrap items-center gap-2">
            <span className="font-mono text-sm font-bold text-brand">{complaintCode(complaint.id)}</span>
            <StatusBadge status={complaint.status} />
            <PriorityBadge priority={complaint.priority} />
          </div>
          <h1 className="mt-3 text-xl font-bold tracking-tight text-ink sm:text-2xl">{complaint.title}</h1>
          <p className="mt-3 whitespace-pre-line text-[15px] leading-relaxed text-muted">
            {complaint.description}
          </p>

          {complaint.resolutionRemarks && (
            <div className="mt-5 rounded-xl bg-success-soft px-4 py-3 text-sm text-emerald-800">
              <p className="font-semibold">Resolution remarks</p>
              <p className="mt-1">{complaint.resolutionRemarks}</p>
            </div>
          )}

          {/* Resolution proof photo — shown to the citizen once the complaint is resolved */}
          {complaint.resolutionProofImageUrl && (
            <div className="mt-5">
              <h2 className="text-base font-semibold text-ink">Resolution proof</h2>
              <p className="mt-1 text-sm text-muted">Photo of the completed fix, uploaded by the field team.</p>
              <a
                href={complaint.resolutionProofImageUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-3 block overflow-hidden rounded-xl border border-line bg-canvas"
              >
                <img
                  src={complaint.resolutionProofImageUrl}
                  alt="Resolution proof photo"
                  className="mx-auto max-h-80 w-full object-contain"
                />
              </a>
            </div>
          )}
        </Card>

        {/* Images */}
        {images.length > 0 && (
          <Card className="p-6">
            <h2 className="text-base font-semibold text-ink">Photos</h2>
            <div className="mt-4 overflow-hidden rounded-xl border border-line bg-canvas">
              <img
                src={images[activeImage]}
                alt="Complaint evidence"
                className="mx-auto max-h-96 w-full object-contain"
              />
            </div>
            {images.length > 1 && (
              <div className="mt-3 flex gap-2">
                {images.map((img, i) => (
                  <button
                    key={img.slice(0, 40) + i}
                    onClick={() => setActiveImage(i)}
                    className={`h-16 w-16 overflow-hidden rounded-lg border-2 transition-all duration-200 ${
                      i === activeImage ? "border-brand" : "border-transparent opacity-70 hover:opacity-100"
                    }`}
                    aria-label={`View photo ${i + 1}`}
                  >
                    <img src={img} alt="" className="h-full w-full object-cover" />
                  </button>
                ))}
              </div>
            )}
          </Card>
        )}

        {/* Timeline */}
        <Card className="p-4 sm:p-6">
          <h2 className="mb-4 text-base font-semibold text-ink sm:mb-6">Progress</h2>
          <ComplaintTimeline status={complaint.status} history={complaint.history} createdAt={complaint.createdAt} />
        </Card>
      </div>

      {/* Sidebar */}
      <div className="space-y-6">
        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">Location</h2>
          <div className="mt-4 flex items-start gap-3">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-accent-soft text-brand">
              <MapPin className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <p className="text-sm font-medium text-ink">GPS coordinates</p>
              <p className="mt-0.5 break-all font-mono text-sm text-muted">
                {complaint.latitude.toFixed(6)}, {complaint.longitude.toFixed(6)}
              </p>
            </div>
          </div>
          <a
            href={`https://www.google.com/maps?q=${complaint.latitude},${complaint.longitude}`}
            target="_blank"
            rel="noopener noreferrer"
            className="mt-4 inline-flex items-center gap-1.5 text-sm font-semibold text-brand hover:underline"
          >
            Open in Google Maps <ExternalLink className="h-3.5 w-3.5" />
          </a>
        </Card>

        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">Details</h2>
          <dl className="mt-4 space-y-4 text-sm">
            <div className="flex items-start gap-3">
              <CalendarDays className="mt-0.5 h-4 w-4 shrink-0 text-muted" />
              <div>
                <dt className="font-medium text-muted">Reported on</dt>
                <dd className="mt-0.5 font-medium text-ink">{formatDateTime(complaint.createdAt)}</dd>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <UserRound className="mt-0.5 h-4 w-4 shrink-0 text-muted" />
              <div>
                <dt className="font-medium text-muted">Assigned to</dt>
                <dd className="mt-0.5 font-medium text-ink">
                  {complaint.assignedTo ? `Field Officer #${complaint.assignedTo}` : "Not yet assigned"}
                </dd>
              </div>
            </div>
            {complaint.updatedAt && (
              <div className="flex items-start gap-3">
                <CalendarDays className="mt-0.5 h-4 w-4 shrink-0 text-muted" />
                <div>
                  <dt className="font-medium text-muted">Last updated</dt>
                  <dd className="mt-0.5 font-medium text-ink">{formatDateTime(complaint.updatedAt)}</dd>
                </div>
              </div>
            )}
          </dl>
        </Card>
      </div>
    </div>
  );
}
