import { useCallback, useEffect, useState } from "react";
import { Users, CalendarDays, Mail } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/States";
import { api } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { NgoEvent, NgoParticipant } from "@/types";

export function NgoParticipants() {
  const [events, setEvents] = useState<NgoEvent[]>([]);
  const [selectedEventId, setSelectedEventId] = useState<number | null>(null);
  const [participants, setParticipants] = useState<NgoParticipant[]>([]);
  const [loadingEvents, setLoadingEvents] = useState(true);
  const [loadingParticipants, setLoadingParticipants] = useState(false);

  const loadEvents = useCallback(async () => {
    setLoadingEvents(true);
    try {
      const res = await api.get<NgoEvent[]>("/api/v1/ngo/events");
      setEvents(res.filter((e) => e.approvalStatus === "PUBLISHED" || e.approvalStatus === "APPROVED"));
    } catch { /* silent */ } finally { setLoadingEvents(false); }
  }, []);

  useEffect(() => { void loadEvents(); }, [loadEvents]);

  useEffect(() => {
    if (selectedEventId === null) return;
    setLoadingParticipants(true);
    api.get<NgoParticipant[]>(`/api/v1/ngo/events/${selectedEventId}/participants`)
      .then(setParticipants)
      .catch(() => setParticipants([]))
      .finally(() => setLoadingParticipants(false));
  }, [selectedEventId]);

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink">Participants</h1>
        <p className="mt-1 text-muted">View registered participants for your events.</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Event list */}
        <div className="lg:col-span-1 space-y-3">
          <h2 className="text-sm font-semibold text-muted uppercase tracking-wider">Your Events</h2>
          {loadingEvents ? (
            <div className="space-y-3">{[0, 1].map((i) => <Skeleton key={i} className="h-20 rounded-xl" />)}</div>
          ) : events.length === 0 ? (
            <Card className="p-6 text-center text-sm text-muted">No published events.</Card>
          ) : (
            <div className="space-y-2">
              {events.map((ev) => (
                <button
                  key={ev.id}
                  onClick={() => setSelectedEventId(ev.id)}
                  className={`w-full rounded-xl border p-4 text-left transition-all ${selectedEventId === ev.id ? "border-brand bg-brand/5 shadow-sm" : "border-line hover:border-brand/30"}`}
                >
                  <p className="text-sm font-semibold text-ink line-clamp-1">{ev.title}</p>
                  <p className="mt-1 text-xs text-muted flex items-center gap-1"><CalendarDays className="h-3 w-3" /> {formatDate(ev.eventDate)} · {ev.registeredCount} registered</p>
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Participant list */}
        <div className="lg:col-span-2">
          {selectedEventId === null ? (
            <Card className="p-12 text-center">
              <Users className="mx-auto h-10 w-10 text-muted/40" />
              <h3 className="mt-3 text-base font-semibold text-ink">Select an event</h3>
              <p className="mt-1 text-sm text-muted">Choose an event from the left to view participants.</p>
            </Card>
          ) : loadingParticipants ? (
            <div className="space-y-3">{[0, 1, 2, 3].map((i) => <Skeleton key={i} className="h-16 rounded-xl" />)}</div>
          ) : participants.length === 0 ? (
            <Card className="p-12 text-center">
              <Users className="mx-auto h-10 w-10 text-muted/40" />
              <h3 className="mt-3 text-base font-semibold text-ink">No participants</h3>
              <p className="mt-1 text-sm text-muted">No one has registered for this event yet.</p>
            </Card>
          ) : (
            <Card className="p-5">
              <h2 className="text-sm font-semibold text-muted uppercase tracking-wider mb-3">Participants ({participants.length})</h2>
              <div className="divide-y divide-line">
                {participants.map((p) => (
                  <div key={p.userId} className="flex items-center justify-between py-3">
                    <div>
                      <p className="text-sm font-medium text-ink">{p.name}</p>
                      <p className="text-xs text-muted flex items-center gap-1"><Mail className="h-3 w-3" /> {p.email}</p>
                    </div>
                    <div className="flex gap-2">
                      <Badge tone="slate">{p.registrationStatus}</Badge>
                      {p.attendanceStatus && <Badge tone="green">{p.attendanceStatus}</Badge>}
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
