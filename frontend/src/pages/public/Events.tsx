import { useCallback, useEffect, useState } from "react";
import { CalendarDays, Clock, MapPin, Sparkles, Users } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/States";
import { api } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Event } from "@/types";

// Fallback events — used only while the events API is unreachable. The single
// source of truth is the community-service `events` table (seeded by EventSeeder),
// which the admin Events page and this public page both read from.
function fallbackEvents(): Event[] {
  const today = new Date();
  const at = (days: number) => {
    const d = new Date(today);
    d.setDate(d.getDate() + days);
    // Local date (yyyy-MM-dd) — toISOString() would be UTC and can shift the day.
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
  };
  return [
    {
      id: 0,
      title: "Musi River Clean-up Drive",
      description:
        "Join citizens, GHMC and partner NGOs for a morning of riverbank clean-up along the Musi. Gloves, bags and refreshments provided.",
      location: "Musi Riverbank, near Purana Pul",
      eventDate: at(14),
      organizerName: "GHMC · HMWS&SB",
      organizerId: 0,
      capacity: 300,
      registeredCount: 0,
      createdAt: "",
    },
    {
      id: 0,
      title: "Sewage Awareness Workshop",
      description:
        "Learn how Hyderabad's sewerage network works, what should never go down the drain, and how citizens can spot early signs of blockages.",
      location: "GHMC Head Office, Tank Bund Road",
      eventDate: at(28),
      organizerName: "HMWS&SB",
      organizerId: 0,
      capacity: 120,
      registeredCount: 0,
      createdAt: "",
    },
    {
      id: 0,
      title: "Lake Restoration Walk",
      description:
        "A guided heritage walk around the restored lakes of Hyderabad — understand the treatment, inlet control and green infrastructure keeping them alive.",
      location: "Durgam Cheruvu, Madhapur",
      eventDate: at(42),
      organizerName: "Hyderabad Lake Friends",
      organizerId: 0,
      capacity: 150,
      registeredCount: 0,
      createdAt: "",
    },
    {
      id: 0,
      title: "Citizen Feedback Townhall",
      description:
        "An open townhall with ward-level officials to discuss sewage complaints, response times and neighbourhood hotspots. Bring your complaints!",
      location: "Community Hall, Secunderabad",
      eventDate: at(56),
      organizerName: "SewageAlert + GHMC",
      organizerId: 0,
      capacity: 200,
      registeredCount: 0,
      createdAt: "",
    },
    {
      id: 0,
      title: "Water & Waste Poster Contest",
      description:
        "School students showcase artwork on saving water and keeping drains clean. Winning entries get featured on the SewageAlert platform.",
      location: "Public Library, Abids",
      eventDate: at(70),
      organizerName: "SewageAlert",
      organizerId: 0,
      capacity: null,
      registeredCount: 0,
      createdAt: "",
    },
    {
      id: 0,
      title: "Sewer Safety & Worker Appreciation Day",
      description:
        "A day to honour the sanitation workers who keep Hyderabad flowing — with safety demos, health camps and a community thank-you.",
      location: "Charminar Maidan",
      eventDate: at(84),
      organizerName: "GHMC Sanitation",
      organizerId: 0,
      capacity: null,
      registeredCount: 0,
      createdAt: "",
    },
  ];
}

export function EventsPage() {
  const [events, setEvents] = useState<Event[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [usingFallback, setUsingFallback] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // Truthful source: the database (shared with the admin Events page).
      // A successful empty response means no events exist — show the empty state.
      setEvents(await api.get<Event[]>("/api/v1/events"));
      setUsingFallback(false);
    } catch {
      // Only on fetch failure do we fall back to the preview list.
      setEvents(fallbackEvents());
      setUsingFallback(true);
      setError("The events service is temporarily unreachable — showing a preview of upcoming activities.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const sorted = events ? [...events].sort((a, b) => a.eventDate.localeCompare(b.eventDate)) : [];

  return (
    <>
      <EventsHero />
      <section className="border-t border-line bg-white py-12 sm:py-16">
        <div className="container-page">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-wider text-accent">Mark your calendar</p>
              <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">Upcoming events</h2>
              <p className="mt-2 max-w-xl text-muted">
                Clean-ups, workshops and townhalls organised by GHMC, HMWS&SB and partner NGOs across Hyderabad.
              </p>
            </div>
            <Badge tone="blue" className="px-3 py-1">
              <Sparkles className="h-3.5 w-3.5" aria-hidden /> Updated regularly
            </Badge>
          </div>

          {error && usingFallback && (
            <p className="mt-5 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              {error}
            </p>
          )}

          {loading ? (
            <div className="mt-10 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
              {[0, 1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-72 w-full rounded-2xl" />
              ))}
            </div>
          ) : sorted.length === 0 ? (
            <Card className="mt-8">
              <div className="flex flex-col items-center px-6 py-14 text-center">
                <CalendarDays className="h-10 w-10 text-muted/50" aria-hidden />
                <h3 className="mt-3 text-base font-semibold text-ink">No events scheduled yet</h3>
                <p className="mt-1 max-w-sm text-sm text-muted">
                  Events will appear here as soon as they are announced.
                </p>
              </div>
            </Card>
          ) : (
            <>
              <div className="mt-8 grid gap-4 sm:mt-10 sm:grid-cols-2 sm:gap-5 lg:grid-cols-3">
                {sorted.map((e) => (
                  <Card
                    key={`${e.title}-${e.eventDate}`}
                    className="group flex flex-col p-0 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift"
                  >
                    <div className="relative overflow-hidden rounded-t-2xl bg-gradient-to-br from-brand via-accent to-emerald-500 px-6 pb-6 pt-7">
                      <div className="flex items-start justify-between gap-3">
                        <div className="rounded-2xl bg-white/15 px-4 py-3 text-center backdrop-blur">
                          <p className="text-2xl font-extrabold leading-none text-white">
                            {new Date(e.eventDate + "T00:00:00").getDate()}
                          </p>
                          <p className="mt-1 text-[11px] font-semibold uppercase tracking-wide text-white/80">
                            {new Date(e.eventDate + "T00:00:00").toLocaleDateString("en-IN", { month: "short" })}
                          </p>
                        </div>
                        <Badge className="bg-white/90 text-brand ring-0">{e.organizerName}</Badge>
                      </div>
                    </div>
                    <div className="flex flex-1 flex-col p-5">
                      <h3 className="text-lg font-semibold text-ink transition-colors group-hover:text-brand">{e.title}</h3>
                      <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-muted">{e.description}</p>
                      <div className="mt-5 space-y-2 border-t border-line pt-4 text-sm text-muted">
                        <p className="flex items-center gap-2">
                          <MapPin className="h-4 w-4 shrink-0 text-brand" aria-hidden /> {e.location}
                        </p>
                        <p className="flex items-center gap-2">
                          <Clock className="h-4 w-4 shrink-0 text-brand" aria-hidden /> {formatDate(e.eventDate)}
                        </p>
                        <p className="flex items-center gap-2">
                          <Users className="h-4 w-4 shrink-0 text-brand" aria-hidden />
                          {e.registeredCount} registered{e.capacity ? ` of ${e.capacity}` : ""} · by {e.organizerName}
                        </p>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>

              <div className="mt-12 rounded-2xl bg-canvas p-6 text-center sm:p-8">
                <CalendarDays className="mx-auto h-8 w-8 text-brand" aria-hidden />
                <h3 className="mt-3 text-lg font-semibold text-ink">Want to organise an event?</h3>
                <p className="mx-auto mt-1 max-w-md text-sm text-muted">
                  Authorities and partner NGOs can add awareness events through the admin console — citizens can then see and join them here.
                </p>
              </div>
            </>
          )}
        </div>
      </section>
    </>
  );
}

function EventsHero() {
  return (
    <section className="border-b border-line bg-white">
      <div className="container-page py-10 text-center sm:py-14 lg:py-20">
        <span className="inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent-soft px-3 py-1 text-xs font-semibold text-brand">
          <CalendarDays className="h-3.5 w-3.5" aria-hidden /> Community events
        </span>
        <h1 className="mx-auto mt-5 max-w-2xl text-3xl font-extrabold tracking-tight text-ink sm:text-4xl lg:text-5xl">
          Events that keep <span className="text-brand">Hyderabad flowing</span>
        </h1>
        <p className="mx-auto mt-4 max-w-xl text-base text-muted sm:text-lg">
          From Musi clean-ups to sewer-safety workshops — join citizens and officials working together on a cleaner city.
        </p>
      </div>
    </section>
  );
}
