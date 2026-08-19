import { useCallback, useEffect, useState } from "react";
import { CalendarDays, MapPin, Clock, CheckCircle2, XCircle, CalendarCheck } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { formatDate } from "@/lib/utils";
import type { NgoEvent } from "@/types";

type Tab = "upcoming" | "registered" | "completed";

const TABS: { key: Tab; label: string }[] = [
  { key: "upcoming", label: "Upcoming" },
  { key: "registered", label: "Registered" },
  { key: "completed", label: "Completed" },
];

export function MyEventsPage() {
  const { toast } = useToast();
  const [events, setEvents] = useState<NgoEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<Tab>("upcoming");

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<NgoEvent[]>("/api/v1/events/published");
      setEvents(res);
    } catch {
      try {
        const res = await api.get<NgoEvent[]>("/api/v1/ngo/events/public");
        setEvents(res);
      } catch { /* silent */ }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const handleJoin = async (eventId: number) => {
    try {
      await api.post(`/api/v1/ngo/events/${eventId}/register`);
      toast("success", "Registered!", "You've been registered for this event.");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not register.");
    }
  };

  const handleCancel = async (eventId: number) => {
    if (!confirm("Cancel your registration?")) return;
    try {
      await api.del(`/api/v1/ngo/events/${eventId}/register`);
      toast("success", "Cancelled", "Your registration has been cancelled.");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not cancel.");
    }
  };

  const now = new Date().toISOString().split("T")[0];

  const filtered = events.filter((e) => {
    if (activeTab === "registered") return e.isRegisteredByCurrentUser;
    if (activeTab === "completed") return e.eventDate < now && !e.isRegisteredByCurrentUser;
    return e.eventDate >= now && e.approvalStatus === "PUBLISHED";
  });

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink">My Events</h1>
        <p className="mt-1 text-muted">Discover and register for community events.</p>
      </div>

      <div className="flex gap-2 border-b border-line pb-3">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${activeTab === tab.key ? "bg-brand text-white" : "text-muted hover:bg-canvas"}`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2].map((i) => <Skeleton key={i} className="h-48 rounded-2xl" />)}
        </div>
      ) : filtered.length === 0 ? (
        <Card className="p-12 text-center">
          <CalendarDays className="mx-auto h-10 w-10 text-muted/40" />
          <h3 className="mt-3 text-base font-semibold text-ink">No events</h3>
          <p className="mt-1 text-sm text-muted">
            {activeTab === "registered" ? "You haven't registered for any events yet." : "No events to show in this category."}
          </p>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((ev) => {
            const isFull = ev.capacity != null && ev.registeredCount >= ev.capacity;
            return (
              <Card key={ev.id} className="flex flex-col p-5 hover:shadow-lift transition-all">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="text-[15px] font-semibold text-ink line-clamp-1">{ev.title}</h3>
                  {ev.isRegisteredByCurrentUser && <Badge tone="green">Registered</Badge>}
                </div>
                <p className="mt-2 line-clamp-2 text-sm text-muted">{ev.description}</p>
                <div className="mt-auto pt-4 space-y-1.5 text-sm text-muted border-t border-line">
                  <p className="flex items-center gap-2"><MapPin className="h-3.5 w-3.5 shrink-0 text-brand" /> {ev.location}</p>
                  <p className="flex items-center gap-2"><Clock className="h-3.5 w-3.5 shrink-0 text-brand" /> {formatDate(ev.eventDate)}</p>
                  <p className="flex items-center gap-2 text-xs">
                    <CalendarCheck className="h-3.5 w-3.5 shrink-0 text-brand" />
                    {ev.registeredCount}{ev.capacity != null ? `/${ev.capacity}` : ""} registered · {ev.ngoOrganizationName}
                  </p>
                </div>
                <div className="mt-3">
                  {ev.isRegisteredByCurrentUser ? (
                    <Button variant="outline" size="sm" fullWidth onClick={() => handleCancel(ev.id)} icon={<XCircle className="h-3.5 w-3.5" />}>
                      Cancel Registration
                    </Button>
                  ) : (
                    <Button size="sm" fullWidth disabled={isFull} onClick={() => handleJoin(ev.id)} icon={<CheckCircle2 className="h-3.5 w-3.5" />}>
                      {isFull ? "Event Full" : "Join Event"}
                    </Button>
                  )}
                </div>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
