import { Link, useParams } from "react-router-dom";
import { ChevronLeft } from "lucide-react";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";

export function ComplaintDetailPage() {
  const { id } = useParams<{ id: string }>();
  const numericId = Number(id);

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <Link
        to="/dashboard/complaints"
        className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted transition-colors hover:text-brand"
      >
        <ChevronLeft className="h-4 w-4" /> My complaints
      </Link>
      {Number.isFinite(numericId) ? (
        <ComplaintDetailView complaintId={numericId} />
      ) : (
        <p className="py-10 text-center text-sm text-muted">Invalid complaint ID.</p>
      )}
    </div>
  );
}
