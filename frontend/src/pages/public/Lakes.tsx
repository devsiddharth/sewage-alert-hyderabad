import { useCallback, useEffect, useState } from "react";
import { Droplets, MapPin, RefreshCw, Waves } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton, ErrorState } from "@/components/ui/States";
import { api } from "@/lib/api";
import type { Lake, LakeGeoData } from "@/types";

export function LakesPage() {
  const [external, setExternal] = useState<LakeGeoData[] | null>(null);
  const [local, setLocal] = useState<Lake[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [found, localLakes] = await Promise.allSettled([
        api.get<LakeGeoData[]>("/api/v1/lakes/external"),
        api.get<Lake[]>("/api/v1/lakes"),
      ]);
      const foundLakes = found.status === "fulfilled" ? found.value : [];
      setExternal(foundLakes.length > 0 ? foundLakes : null);
      setLocal(localLakes.status === "fulfilled" ? localLakes.value : []);
    } catch {
      setError("Couldn't load lake information right now.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  const hasContent = (external?.length ?? 0) > 0 || local.length > 0;

  return (
    <>
      <LakesHero />
      <section className="border-t border-line bg-white py-12 sm:py-16">
        <div className="container-page">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-wider text-accent">Lakes of Hyderabad</p>
              <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">Explore our water bodies</h2>
              <p className="mt-2 max-w-xl text-muted">
                Lake locations and geometry sourced from OpenStreetMap, alongside restoration status maintained by authorities.
              </p>
            </div>
            <button
              onClick={() => void load()}
              className="inline-flex h-10 items-center gap-1.5 rounded-xl border border-line bg-white px-4 text-sm font-semibold text-ink transition-colors hover:border-accent hover:text-brand"
            >
              <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} aria-hidden /> Refresh
            </button>
          </div>

          {loading ? (
            <div className="mt-8 grid gap-5 md:grid-cols-2 lg:grid-cols-3">
              {[0, 1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-44 w-full rounded-2xl" />
              ))}
            </div>
          ) : error && !hasContent ? (
            <Card className="mt-8">
              <ErrorState message={error} onRetry={() => void load()} />
            </Card>
          ) : (
            <>
              {external && external.length > 0 && (
                <div className="mt-8">
                  <p className="mb-4 inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-brand">
                    <MapPin className="h-3.5 w-3.5" aria-hidden /> From OpenStreetMap
                  </p>
                  <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {external.map((l, i) => (
                      <Card
                        key={`${l.name}-${i}`}
                        className="group flex flex-col p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift"
                      >
                        <div className="flex items-center gap-3">
                          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-cyan-50 text-cyan-600">
                            <Waves className="h-5 w-5" aria-hidden />
                          </span>
                          <div className="min-w-0">
                            <h3 className="truncate text-[15px] font-semibold text-ink">{l.name}</h3>
                            <p className="truncate text-xs text-muted">
                              {l.address?.formatted ?? ([l.address?.city, l.address?.state].filter(Boolean).join(", ") || "Hyderabad")}
                            </p>
                          </div>
                        </div>
                        <div className="mt-4 flex items-center gap-2 rounded-xl bg-canvas px-3 py-2 text-xs text-muted">
                          <Droplets className="h-3.5 w-3.5 text-cyan-600" aria-hidden />
                          {l.latitude.toFixed(4)}, {l.longitude.toFixed(4)}
                          <span className="ml-auto rounded-full bg-white px-2 py-0.5 font-medium text-cyan-700 ring-1 ring-inset ring-cyan-200">
                            {l.geometry?.length ?? 0} pts
                          </span>
                        </div>
                      </Card>
                    ))}
                  </div>
                </div>
              )}

              {local.length > 0 && (
                <div className="mt-12">
                  <p className="mb-4 inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-brand">
                    <Waves className="h-3.5 w-3.5" aria-hidden /> Restoration status
                  </p>
                  <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {local.map((l) => (
                      <Card key={l.id} className="group flex flex-col p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift">
                        <div className="flex items-center gap-3">
                          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-cyan-50 text-cyan-600">
                            <Waves className="h-5 w-5" aria-hidden />
                          </span>
                          <div className="min-w-0">
                            <h3 className="truncate text-[15px] font-semibold text-ink">{l.name}</h3>
                            <p className="truncate text-xs text-muted">{l.location}</p>
                          </div>
                        </div>
                        {l.restorationStatus && (
                          <Badge tone="green" className="mt-4 w-fit">
                            <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" aria-hidden /> {l.restorationStatus}
                          </Badge>
                        )}
                        {l.description && <p className="mt-3 line-clamp-2 text-sm text-muted">{l.description}</p>}
                        {l.environmentalUpdates && (
                          <p className="mt-2 text-xs text-cyan-700">{l.environmentalUpdates}</p>
                        )}
                      </Card>
                    ))}
                  </div>
                </div>
              )}

              {!hasContent && (
                <Card className="mt-8">
                  <div className="flex flex-col items-center px-6 py-14 text-center">
                    <Waves className="h-10 w-10 text-muted/50" aria-hidden />
                    <h3 className="mt-3 text-base font-semibold text-ink">No lake data available</h3>
                    <p className="mt-1 max-w-sm text-sm text-muted">
                      Lake information will appear here once the external feed is reachable.
                    </p>
                  </div>
                </Card>
              )}
            </>
          )}
        </div>
      </section>
    </>
  );
}

function LakesHero() {
  return (
    <section className="border-b border-line bg-white">
      <div className="container-page py-10 text-center sm:py-14 lg:py-20">
        <span className="inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent-soft px-3 py-1 text-xs font-semibold text-brand">
          <Waves className="h-3.5 w-3.5" aria-hidden /> Lakes & water bodies
        </span>
        <h1 className="mx-auto mt-5 max-w-2xl text-3xl font-extrabold tracking-tight text-ink sm:text-4xl lg:text-5xl">
          The lakes that keep <span className="text-brand">Hyderabad alive</span>
        </h1>
        <p className="mx-auto mt-4 max-w-xl text-base text-muted sm:text-lg">
          From Hussain Sagar to Durgam Cheruvu — discover the city's water bodies and how restoration is bringing them back.
        </p>
      </div>
    </section>
  );
}
