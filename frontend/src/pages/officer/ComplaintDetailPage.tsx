import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, CheckCircle2, Hammer } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Textarea } from "@/components/ui/Field";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";
import { useComplaint } from "@/hooks/useComplaint";
import { useAuth } from "@/lib/auth";
import { useToast } from "@/lib/toast";
import { updateAssignedComplaintStatus } from "@/services/assignment";
import { complaintCode } from "@/lib/utils";

export function FieldOfficerComplaintDetail() {
  const { id } = useParams();
  const complaintId = Number(id);
  const { user } = useAuth();
  const { toast } = useToast();

  const { complaint, loading, reload } = useComplaint(complaintId);
  const [remarks, setRemarks] = useState("");
  const [saving, setSaving] = useState<"IN_PROGRESS" | "RESOLVED" | null>(null);

  // The officer may only update complaints assigned to them — the backend enforces
  // this too, so the UI merely surfaces the backend's decision.
  const isMine = complaint != null && complaint.assignedTo === user?.id;
  const canUpdate = isMine && complaint.status !== "RESOLVED" && complaint.status !== "REJECTED";

  const updateStatus = async (status: "IN_PROGRESS" | "RESOLVED") => {
    if (!complaint) return;
    setSaving(status);
    try {
      await updateAssignedComplaintStatus(complaint.id, {
        status,
        priority: complaint.priority,
        remarks: remarks.trim() || (status === "RESOLVED" ? "Issue attended and fixed by field team." : null),
      });
      toast(
        "success",
        status === "RESOLVED" ? "Complaint resolved" : "Complaint marked in progress",
        complaintCode(complaint.id)
      );
      setRemarks("");
      void reload();
    } catch (e) {
      toast("error", "Update failed", e instanceof Error ? e.message : undefined);
    } finally {
      setSaving(null);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          to="/officer/complaints"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted transition-colors hover:text-brand"
        >
          <ArrowLeft className="h-4 w-4" /> Back to assigned complaints
        </Link>
      </div>

      {canUpdate && complaint && (
        <Card className="p-6">
          <div className="flex items-start gap-3">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-accent-soft text-brand">
              <Hammer className="h-5 w-5" />
            </span>
            <div className="min-w-0 flex-1">
              <h2 className="text-base font-semibold text-ink">Update status</h2>
              <p className="mt-0.5 text-sm text-muted">
                Move this complaint forward as you complete the field work.
              </p>
              <div className="mt-4">
                <Field label="Remarks (optional)" hint="Shared with the citizen on the timeline.">
                  <Textarea
                    rows={2}
                    value={remarks}
                    onChange={(e) => setRemarks(e.target.value)}
                    placeholder="e.g. Reached the site, clearing the blockage."
                  />
                </Field>
              </div>
              <div className="mt-4 flex flex-wrap gap-3">
                {complaint.status === "PENDING" && (
                  <Button
                    icon={<Hammer className="h-4 w-4" />}
                    loading={saving === "IN_PROGRESS"}
                    onClick={() => void updateStatus("IN_PROGRESS")}
                  >
                    Start work (In progress)
                  </Button>
                )}
                {(complaint.status === "IN_PROGRESS" || complaint.status === "PENDING") && (
                  <Button
                    variant="secondary"
                    icon={<CheckCircle2 className="h-4 w-4" />}
                    loading={saving === "RESOLVED"}
                    onClick={() => void updateStatus("RESOLVED")}
                  >
                    Mark resolved
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Card>
      )}

      {!loading && complaint && !isMine && (
        <Card className="border-red-200 bg-red-50 p-5">
          <p className="text-sm font-semibold text-red-700">
            This complaint is not assigned to you. You can view it, but updates are not permitted.
          </p>
        </Card>
      )}

      <ComplaintDetailView complaintId={complaintId} />
    </div>
  );
}
