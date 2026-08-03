import { Link } from "react-router-dom";
import { MapPin, ArrowRight } from "lucide-react";
import { complaintCode, formatDate, timeAgo } from "@/lib/utils";
import { StatusBadge, PriorityBadge } from "@/components/ui/Badge";
import type { Complaint } from "@/types";

export function ComplaintCard({ complaint, basePath }: { complaint: Complaint; basePath: string }) {
  const hasImage = complaint.imageUrls && complaint.imageUrls.length > 0;

  return (
    <Link
      to={`${basePath}/${complaint.id}`}
      className="group flex gap-4 rounded-2xl border border-line bg-white p-4 shadow-card transition-all duration-200 hover:-translate-y-0.5 hover:border-accent/60 hover:shadow-lift sm:p-5"
    >
      {hasImage && (
        <div className="hidden h-20 w-20 shrink-0 overflow-hidden rounded-xl sm:block">
          <img
            src={complaint.imageUrls[0]}
            alt=""
            className="h-full w-full object-cover transition-transform duration-200 group-hover:scale-105"
          />
        </div>
      )}
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-mono text-xs font-semibold text-brand">{complaintCode(complaint.id)}</span>
          <StatusBadge status={complaint.status} />
          <PriorityBadge priority={complaint.priority} />
        </div>
        <h3 className="mt-2 truncate text-[15px] font-semibold text-ink">{complaint.title}</h3>
        <p className="mt-1 line-clamp-2 text-sm text-muted">{complaint.description}</p>
        <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted">
          <span className="inline-flex items-center gap-1">
            <MapPin className="h-3.5 w-3.5" aria-hidden />
            {complaint.latitude.toFixed(4)}, {complaint.longitude.toFixed(4)}
          </span>
          <span>Reported {timeAgo(complaint.createdAt)}</span>
          {complaint.updatedAt !== complaint.createdAt && (
            <span className="inline-flex items-center gap-1 text-brand">
              Updated {formatDate(complaint.updatedAt)}
            </span>
          )}
        </div>
      </div>
      <ArrowRight
        className="mt-1 h-4 w-4 shrink-0 self-center text-muted transition-all duration-200 group-hover:translate-x-0.5 group-hover:text-brand"
        aria-hidden
      />
    </Link>
  );
}
