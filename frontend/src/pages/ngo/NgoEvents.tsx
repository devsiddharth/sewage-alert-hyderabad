import { useCallback, useEffect, useState, type FormEvent } from "react";
import { CalendarDays, Clock, MapPin, Plus, Trash2, CheckCircle2, XCircle } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { formatDate } from "@/lib/utils";
import type { NgoEvent, EventApprovalStatus } from "@/types";

const STATUS_BADGE: Record<EventApprovalStatus, { label: string; tone: "slate" | "blue" | "green" | "red" | "amber" }> = {
  PENDING_APPROVAL: { label: "Pending Review", tone: "slate" },
  APPROVED: { label: "Approved", tone: "green" },
  PUBLISHED: { label: "Published", tone: "green" },
  REJECTED: { label: "Rejected", tone: "red" },
  CANCELLED: { label: "Cancelled", tone: "amber" },
};

export function NgoEvents() {
  const { toast } = useToast();
  const [events, setEvents] = useState<NgoEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);

  const [form, setForm] = useState({
    title: "", description: "", location: "", eventDate: "", eventTime: "", capacity: "", category: "",
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<NgoEvent[]>("/api/v1/ngo/events");
      setEvents(res);
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
    if (!form.title || !form.location || !form.eventDate) {
      toast("error", "Missing fields", "Title, location, and date are required.");
      return;
    }
    setCreating(true);
    try {
      await api.post("/api/v1/ngo/events", {
        title: form.title,
        description: form.description,
        location: form.location,
        eventDate: form.eventDate,
        eventTime: form.eventTime || null,
        capacity: form.capacity ? parseInt(form.capacity) : null,
        category: form.category || null,
      });
      toast("success", "Event created", "Your event has been submitted for admin approval.");
      setShowCreate(false);
      setForm({ title: "", description: "", location: "", eventDate: "", eventTime: "", capacity: "", category: "" });
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not create event.");
    } finally {
      setCreating(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this event?")) return;
    try {
      await api.del(`/api/v1/ngo/events/${id}`);
      toast("success", "Deleted", "Event deleted successfully.");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not delete event.");
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink">Events</h1>
          <p className="mt-1 text-muted">Create and manage your NGO events. Events require admin approval before being published.</p>
        </div>
        <Button onClick={() => setShowCreate(true)} icon={<Plus className="h-4 w-4" />}>
          Create Event
        </Button>
      </div>

      {loading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map((i) => <Skeleton key={i} className="h-48 rounded-2xl" />)}
        </div>
      ) : events.length === 0 ? (
        <Card className="p-12 text-center">
          <CalendarDays className="mx-auto h-10 w-10 text-muted/40" />
          <h3 className="mt-3 text-base font-semibold text-ink">No events yet</h3>
          <p className="mt-1 text-sm text-muted">Create your first event to get started.</p>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {events.map((ev) => {
            const st = STATUS_BADGE[ev.approvalStatus] ?? STATUS_BADGE.PENDING_APPROVAL;
            return (
              <Card key={ev.id} className="flex flex-col p-5 hover:shadow-lift transition-all">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="text-[15px] font-semibold text-ink line-clamp-1">{ev.title}</h3>
                  <Badge tone={st.tone} className="shrink-0">{st.label}</Badge>
                </div>
                <p className="mt-2 line-clamp-2 text-sm text-muted">{ev.description}</p>
                <div className="mt-auto pt-4 space-y-1.5 text-sm text-muted border-t border-line">
                  <p className="flex items-center gap-2"><CalendarDays className="h-3.5 w-3.5 shrink-0 text-brand" /> {formatDate(ev.eventDate)}</p>
                  <p className="flex items-center gap-2"><MapPin className="h-3.5 w-3.5 shrink-0 text-brand" /> {ev.location}</p>
                  {ev.capacity && <p className="flex items-center gap-2"><Clock className="h-3.5 w-3.5 shrink-0 text-brand" /> {ev.registeredCount}/{ev.capacity} registered</p>}
                </div>
                {ev.rejectionReason && (
                  <div className="mt-2 rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">
                    <XCircle className="mr-1 inline h-3 w-3" /> {ev.rejectionReason}
                  </div>
                )}
                <div className="mt-3 flex gap-2">
                  {ev.approvalStatus === "PENDING_APPROVAL" && (
                    <Button variant="outline" size="sm" onClick={() => handleDelete(ev.id)} icon={<Trash2 className="h-3.5 w-3.5" />}>
                      Cancel
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}

      {/* Create Event Modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Create Event">
          <form onSubmit={handleCreate} className="space-y-4">
            <Field label="Title" required>
              <Input value={form.title} onChange={(e) => update("title", e.target.value)} placeholder="e.g. Sewage Awareness Workshop" />
            </Field>
            <Field label="Description">
              <Textarea rows={3} value={form.description} onChange={(e) => update("description", e.target.value)} placeholder="Describe the event..." />
            </Field>
            <Field label="Location" required>
              <Input value={form.location} onChange={(e) => update("location", e.target.value)} placeholder="GHMC Head Office, Tank Bund Road" />
            </Field>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Date" required>
                <Input type="date" value={form.eventDate} onChange={(e) => update("eventDate", e.target.value)} />
              </Field>
              <Field label="Time">
                <Input type="time" value={form.eventTime} onChange={(e) => update("eventTime", e.target.value)} />
              </Field>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Capacity">
                <Input type="number" value={form.capacity} onChange={(e) => update("capacity", e.target.value)} placeholder="e.g. 200" />
              </Field>
              <Field label="Category">
                <Input value={form.category} onChange={(e) => update("category", e.target.value)} placeholder="e.g. Awareness, Clean-up" />
              </Field>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" type="button" onClick={() => setShowCreate(false)}>Cancel</Button>
              <Button type="submit" loading={creating} icon={<CheckCircle2 className="h-4 w-4" />}>Submit for Approval</Button>
            </div>
          </form>
      </Modal>
    </div>
  );
}
