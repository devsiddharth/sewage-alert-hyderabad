import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Camera, CheckCircle2, Hammer, ImagePlus, X } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Textarea } from "@/components/ui/Field";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";
import { useComplaint } from "@/hooks/useComplaint";
import { useAuth } from "@/lib/auth";
import { useToast } from "@/lib/toast";
import { resolveAssignedComplaint, updateAssignedComplaintStatus } from "@/services/assignment";
import { complaintCode, fileToCompressedFile } from "@/lib/utils";

export function FieldOfficerComplaintDetail() {
  const { id } = useParams();
  const complaintId = Number(id);
  const { user } = useAuth();
  const { toast } = useToast();

  const { complaint, loading, reload } = useComplaint(complaintId);
  const [remarks, setRemarks] = useState("");
  const [proofFile, setProofFile] = useState<File | null>(null);
  const [proofError, setProofError] = useState<string | null>(null);
  const [saving, setSaving] = useState<"IN_PROGRESS" | "RESOLVED" | null>(null);
  const proofFileRef = useRef<HTMLInputElement>(null);

  // Object-URL preview for the selected proof photo (revoked whenever it changes or unmounts).
  const proofPreviewUrl = useMemo(() => (proofFile ? URL.createObjectURL(proofFile) : null), [proofFile]);
  useEffect(() => () => {
    if (proofPreviewUrl) URL.revokeObjectURL(proofPreviewUrl);
  }, [proofPreviewUrl]);

  // The officer may only update complaints assigned to them — the backend enforces
  // this too, so the UI merely surfaces the backend's decision.
  const isMine = complaint != null && complaint.assignedTo === user?.id;
  const canUpdate = isMine && complaint.status !== "RESOLVED" && complaint.status !== "REJECTED";

  const addProofImage = async (file: File | null) => {
    setProofError(null);
    if (!file) {
      setProofFile(null);
      return;
    }
    try {
      setProofFile(await fileToCompressedFile(file));
    } catch {
      setProofFile(null);
      setProofError("That file couldn't be read as an image. Please choose a JPG, PNG or WEBP photo.");
    }
  };

  const updateStatus = async (status: "IN_PROGRESS" | "RESOLVED") => {
    if (!complaint) return;

    // A resolution photo is mandatory before marking a complaint resolved — the backend
    // enforces this too, so a missing photo is blocked here with a clear message.
    if (status === "RESOLVED" && !proofFile) {
      setProofError("A resolution photo is required before this complaint can be marked as resolved.");
      return;
    }

    setSaving(status);
    try {
      if (status === "RESOLVED") {
        await resolveAssignedComplaint(complaint.id, {
          remarks: remarks.trim() || "Issue attended and fixed by field team.",
          priority: complaint.priority,
          proofImage: proofFile!,
        });
      } else {
        await updateAssignedComplaintStatus(complaint.id, {
          status,
          priority: complaint.priority,
          remarks: remarks.trim() || null,
        });
      }
      toast(
        "success",
        status === "RESOLVED" ? "Complaint resolved" : "Complaint marked in progress",
        complaintCode(complaint.id)
      );
      setRemarks("");
      setProofFile(null);
      setProofError(null);
      void reload();
    } catch (e) {
      toast("error", "Update failed", e instanceof Error ? e.message : undefined);
    } finally {
      setSaving(null);
    }
  };

  return (
    <div className="space-y-5 sm:space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <Link
          to="/officer/complaints"
          className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted transition-colors hover:text-brand"
        >
          <ArrowLeft className="h-4 w-4" /> Back to assigned complaints
        </Link>
      </div>

      {canUpdate && complaint && (
        <Card className="p-4 sm:p-6">
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
              {proofFile && (
                <div className="mt-4 flex items-center gap-3 rounded-xl border border-line bg-canvas p-3">
                  <img
                    src={proofPreviewUrl ?? undefined}
                    alt="Resolution proof preview"
                    className="h-14 w-14 shrink-0 rounded-lg object-cover"
                  />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-ink">{proofFile.name}</p>
                    <p className="text-xs text-muted">Proof photo ready to upload.</p>
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
              )}
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
                    disabled={!proofFile}
                    title={!proofFile ? "A resolution photo is required" : undefined}
                    onClick={() => void updateStatus("RESOLVED")}
                  >
                    Mark resolved
                  </Button>
                )}
              </div>
              {(complaint.status === "IN_PROGRESS" || complaint.status === "PENDING") && (
                <button
                  type="button"
                  onClick={() => proofFileRef.current?.click()}
                  className="mt-3 inline-flex items-center gap-1.5 rounded-lg border border-dashed border-line px-3 py-2 text-sm font-medium text-muted transition-colors hover:border-accent hover:text-brand"
                >
                  <ImagePlus className="h-4 w-4" />
                  {proofFile ? "Replace resolution photo" : "Attach resolution photo (required to resolve)"}
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
                <p className="mt-3 flex items-center gap-1.5 text-sm font-medium text-red-600">
                  <Camera className="h-4 w-4 shrink-0" aria-hidden />
                  {proofError}
                </p>
              )}
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
