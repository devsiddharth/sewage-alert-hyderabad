import { useCallback, useEffect, useState } from "react";
import { CalendarDays, MapPin, Clock, CheckCircle2 } from "lucide-react";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { formatDate } from "@/lib/utils";
import type { NgoEvent } from "@/types";

export function PublicEventsV2() {
  const { toast } = useToast();
  const [events, setEvents] = useState<NgoEvent[]>([]);
  const [loading, setLoading] = useState(true);

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

  const sorted = [...events].sort((a, b) => a.eventDate.localeCompare(b.eventDate));

  return (
    <section className="border-t border-line bg-white py-12 sm:py-16">
      <div className="container-page">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-accent">Community Events</p>
            <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">Upcoming events</h2>
            <p className="mt-2 max-w-xl text-muted">Events hosted by verified NGOs across Hyderabad.</p>
          </div>
          <Link to="/dashboard/my-events">
            <Badge tone="blue" className="px-3 py-1">My Events →</Badge>
          </Link>
        </div>

        {loading ? (
          <div className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {[0, 1, 2].map((i) => <Skeleton key={i} className="h-72 w-full rounded-2xl" />)}
          </div>
        ) : sorted.length === 0 ? (
          <Card className="mt-8">
            <div className="flex flex-col items-center px-6 py-14 text-center">
              <CalendarDays className="h-10 w-10 text-muted/50" aria-hidden />
              <h3 className="mt-3 text-base font-semibold text-ink">No events scheduled yet</h3>
              <p className="mt-1 max-w-sm text-sm text-muted">Events will appear here as soon as NGOs publish them.</p>
            </div>
          </Card>
        ) : (
          <div className="mt-8 grid gap-4 sm:mt-10 sm:grid-cols-2 sm:gap-5 lg:grid-cols-3">
            {sorted.map((ev) => {
              const isFull = ev.capacity != null && ev.registeredCount >= ev.capacity;
              return (
                <Card key={ev.id} className="group flex flex-col p-0 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift">
                  <div className="relative overflow-hidden rounded-t-2xl bg-gradient-to-br from-brand via-accent to-emerald-500 px-6 pb-6 pt-7">
                    <div className="flex items-start justify-between gap-3">
                      <div className="rounded-2xl bg-white/15 px-4 py-3 text-center backdrop-blur">
                        <p className="text-2xl font-extrabold leading-none text-white">
                          {new Date(ev.eventDate + "T00:00:00").getDate()}
                        </p>
                        <p className="mt-1 text-[11px] font-semibold uppercase tracking-wide text-white/80">
                          {new Date(ev.eventDate + "T00:00:00").toLocaleDateString("en-IN", { month: "short" })}
                        </p>
                      </div>
                      <Badge className="bg-white/90 text-brand ring-0">{ev.ngoOrganizationName}</Badge>
                    </div>
                  </div>
                  <div className="flex flex-1 flex-col p-5">
                    <h3 className="text-lg font-semibold text-ink transition-colors group-hover:text-brand">{ev.title}</h3>
                    <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-muted">{ev.description}</p>
                    <div className="mt-5 space-y-2 border-t border-line pt-4 text-sm text-muted">
                      <p className="flex items-center gap-2"><MapPin className="h-4 w-4 shrink-0 text-brand" aria-hidden /> {ev.location}</p>
                      <p className="flex items-center gap-2"><Clock className="h-4 w-4 shrink-0 text-brand" aria-hidden /> {formatDate(ev.eventDate)}</p>
                    </div>
                    <div className="mt-4">
                      {ev.isRegisteredByCurrentUser ? (
                        <Badge tone="green" className="w-full justify-center py-2">
                          <CheckCircle2 className="h-3.5 w-3.5 mr-1" /> Registered
                        </Badge>
                      ) : (
                        <Button size="sm" fullWidth disabled={isFull} onClick={() => handleJoin(ev.id)}>
                          {isFull ? "Event Full" : "Join Event"}
                        </Button>
                      )}
                    </div>
                  </div>
                </Card>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
}
