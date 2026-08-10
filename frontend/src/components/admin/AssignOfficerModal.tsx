import { useEffect, useState } from "react";
import { UserRoundCheck } from "lucide-react";
import { Modal } from "@/components/ui/Modal";
import { Button } from "@/components/ui/Button";
import { Field, Select } from "@/components/ui/Field";
import { Skeleton, EmptyState } from "@/components/ui/States";
import { complaintCode } from "@/lib/utils";
import type { Complaint, FieldOfficer } from "@/types";

export function AssignOfficerModal({
  open,
  onClose,
  complaint,
  officers,
  officersLoading,
  onAssign,
}: {
  open: boolean;
  onClose: () => void;
  complaint: Complaint | null;
  officers: FieldOfficer[];
  officersLoading: boolean;
  onAssign: (complaintId: number, fieldOfficerId: number) => Promise<void>;
}) {
  const [officerId, setOfficerId] = useState("");
  const [saving, setSaving] = useState(false);

  // Pre-select the currently assigned officer when the modal opens
  useEffect(() => {
    if (open) {
      setOfficerId(complaint?.assignedTo != null ? String(complaint.assignedTo) : "");
      setSaving(false);
    }
  }, [open, complaint]);

  const isReassign = Boolean(complaint?.assignedTo);

  const submit = async () => {
    if (!complaint || !officerId) return;
    setSaving(true);
    try {
      await onAssign(complaint.id, Number(officerId));
      onClose();
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isReassign ? "Reassign complaint" : "Assign complaint"}
      description={`${isReassign ? "Move" : "Assign"} ${complaint ? complaintCode(complaint.id) : ""} to a field officer.`}
    >
      <div className="space-y-5">
        {complaint && (
          <div className="rounded-xl border border-line bg-canvas px-4 py-3">
            <p className="text-sm font-semibold text-ink">{complaint.title}</p>
            <p className="mt-0.5 line-clamp-2 text-xs text-muted">{complaint.description}</p>
          </div>
        )}

        <Field label="Field Officer" required hint="Only users with the Field Officer role can be assigned.">
          {officersLoading ? (
            <div className="space-y-2">
              <Skeleton className="h-10 w-full rounded-xl" />
            </div>
          ) : officers.length === 0 ? (
            <EmptyState
              title="No field officers available"
              description="There are no assignable field officers right now. Please check back later."
            />
          ) : (
            <Select value={officerId} onChange={(e) => setOfficerId(e.target.value)} aria-label="Field officer">
              <option value="" disabled>
                Select field officer…
              </option>
              {officers.map((o) => (
                <option key={o.id} value={o.id}>
                  {o.name} — {o.email}
                </option>
              ))}
            </Select>
          )}
        </Field>

        <div className="flex justify-end gap-3">
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            onClick={() => void submit()}
            loading={saving}
            disabled={!officerId || officers.length === 0}
            icon={<UserRoundCheck className="h-4 w-4" />}
          >
            {isReassign ? "Reassign" : "Assign"}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
