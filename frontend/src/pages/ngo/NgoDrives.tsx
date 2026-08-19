import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Truck, Plus, CheckCircle2, MapPin, CalendarDays, Trash2 } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { formatDate } from "@/lib/utils";
import type { NgoDrive, DriveStatus } from "@/types";

const DRIVE_STATUS: Record<DriveStatus, { label: string; tone: "slate" | "blue" | "green" | "red" }> = {
  PLANNED: { label: "Planned", tone: "slate" },
  IN_PROGRESS: { label: "In Progress", tone: "blue" },
  COMPLETED: { label: "Completed", tone: "green" },
  CANCELLED: { label: "Cancelled", tone: "red" },
};

export function NgoDrives() {
  const { toast } = useToast();
  const [drives, setDrives] = useState<NgoDrive[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);

  const [form, setForm] = useState({
    title: "", description: "", driveType: "", location: "", startDate: "", endDate: "", totalTarget: "", progressNotes: "",
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<NgoDrive[]>("/api/v1/ngo/drives");
      setDrives(res);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const update = (field: string, value: string) => setForm((f) => ({ ...f, [field]: value }));

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.title || !form.location || !form.startDate) {
      toast("error", "Missing fields", "Title, location, and start date are required.");
      return;
    }
    setCreating(true);
    try {
      await api.post("/api/v1/ngo/drives", {
        title: form.title,
        description: form.description,
        driveType: form.driveType || null,
        location: form.location,
        startDate: form.startDate,
        endDate: form.endDate || null,
        totalTarget: form.totalTarget ? parseInt(form.totalTarget) : null,
        progressNotes: form.progressNotes || null,
      });
      toast("success", "Drive created", "Your drive has been created successfully.");
      setShowCreate(false);
      setForm({ title: "", description: "", driveType: "", location: "", startDate: "", endDate: "", totalTarget: "", progressNotes: "" });
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not create drive.");
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this drive?")) return;
    try {
      await api.del(`/api/v1/ngo/drives/${id}`);
      toast("success", "Deleted", "Drive deleted.");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not delete.");
    }
  };

  const handleStatusChange = async (id: number, status: DriveStatus) => {
    try {
      await api.put(`/api/v1/ngo/drives/${id}/status`, { status });
      toast("success", "Updated", `Drive status changed to ${status}.`);
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not update status.");
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink">Drives</h1>
          <p className="mt-1 text-muted">Manage cleanliness, plantation, and awareness drives.</p>
        </div>
        <Button onClick={() => setShowCreate(true)} icon={<Plus className="h-4 w-4" />}>Create Drive</Button>
      </div>

      {loading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map((i) => <Skeleton key={i} className="h-48 rounded-2xl" />)}
        </div>
      ) : drives.length === 0 ? (
        <Card className="p-12 text-center">
          <Truck className="mx-auto h-10 w-10 text-muted/40" />
          <h3 className="mt-3 text-base font-semibold text-ink">No drives yet</h3>
          <p className="mt-1 text-sm text-muted">Create your first drive to get started.</p>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {drives.map((d) => {
            const st = DRIVE_STATUS[d.status] ?? DRIVE_STATUS.PLANNED;
            return (
              <Card key={d.id} className="flex flex-col p-5 hover:shadow-lift transition-all">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="text-[15px] font-semibold text-ink line-clamp-1">{d.title}</h3>
                  <Badge tone={st.tone} className="shrink-0">{st.label}</Badge>
                </div>
                <p className="mt-2 line-clamp-2 text-sm text-muted">{d.description}</p>
                {d.driveType && <Badge tone="slate" className="mt-2 w-fit">{d.driveType}</Badge>}
                <div className="mt-auto pt-4 space-y-1.5 text-sm text-muted border-t border-line">
                  <p className="flex items-center gap-2"><MapPin className="h-3.5 w-3.5 shrink-0 text-brand" /> {d.location}</p>
                  <p className="flex items-center gap-2"><CalendarDays className="h-3.5 w-3.5 shrink-0 text-brand" /> {formatDate(d.startDate)}</p>
                  {d.totalTarget && <p className="text-xs">Participants: {d.currentParticipants}/{d.totalTarget}</p>}
                </div>
                <div className="mt-3 flex flex-wrap gap-2">
                  {d.status === "PLANNED" && (
                    <Button variant="outline" size="sm" onClick={() => handleStatusChange(d.id, "IN_PROGRESS")}>Start</Button>
                  )}
                  {d.status === "IN_PROGRESS" && (
                    <Button variant="outline" size="sm" onClick={() => handleStatusChange(d.id, "COMPLETED")}>Complete</Button>
                  )}
                  <Button variant="outline" size="sm" onClick={() => handleDelete(d.id)} icon={<Trash2 className="h-3.5 w-3.5" />}>Delete</Button>
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create Drive Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Create Drive">
          <form onSubmit={handleCreate} className="space-y-4">
            <Field label="Title" required>
              <Input value={form.title} onChange={(e) => update("title", e.target.value)} placeholder="e.g. Miyapur Cleanliness Drive" />
            </Field>
            <Field label="Description">
              <Textarea rows={3} value={form.description} onChange={(e) => update("description", e.target.value)} placeholder="Describe the drive..." />
            </Field>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Drive Type">
                <Input value={form.driveType} onChange={(e) => update("driveType", e.target.value)} placeholder="Cleanliness, Plantation, etc." />
              </Field>
              <Field label="Location" required>
                <Input value={form.location} onChange={(e) => update("location", e.target.value)} placeholder="Drive location" />
              </Field>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Start Date" required>
                <Input type="date" value={form.startDate} onChange={(e) => update("startDate", e.target.value)} />
              </Field>
              <Field label="End Date">
                <Input type="date" value={form.endDate} onChange={(e) => update("endDate", e.target.value)} />
              </Field>
            </div>
            <Field label="Target Participants">
              <Input type="number" value={form.totalTarget} onChange={(e) => update("totalTarget", e.target.value)} placeholder="e.g. 50" />
            </Field>
            <Field label="Progress Notes">
              <Textarea rows={2} value={form.progressNotes} onChange={(e) => update("progressNotes", e.target.value)} />
            </Field>
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" type="button" onClick={() => setShowCreate(false)}>Cancel</Button>
              <Button type="submit" loading={creating} icon={<CheckCircle2 className="h-4 w-4" />}>Create Drive</Button>
            </div>
          </form>
      </Modal>
    </div>
  );
}
