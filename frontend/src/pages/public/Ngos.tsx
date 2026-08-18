import { useCallback, useEffect, useState } from "react";
import { ExternalLink, Globe, HeartHandshake, MapPin, Phone, RefreshCw, Search } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton, ErrorState } from "@/components/ui/States";
import { api } from "@/lib/api";
import type { Ngo, NgoSearchResult } from "@/types";

const KEYWORDS = ["Water", "Environment", "Sanitation"] as const;
type Keyword = (typeof KEYWORDS)[number];

export function NgosPage() {
  const [keyword, setKeyword] = useState<Keyword>("Water");
  const [search, setSearch] = useState("");
  const [external, setExternal] = useState<NgoSearchResult[] | null>(null);
  const [local, setLocal] = useState<Ngo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [found, localNgos] = await Promise.allSettled([
        api.get<NgoSearchResult[]>(`/api/v1/ngos/search?city=Hyderabad&keyword=${search || keyword}`),
        api.get<Ngo[]>("/api/v1/ngos"),
      ]);
      setExternal(found.status === "fulfilled" && found.value.length > 0 ? found.value : null);
      setLocal(localNgos.status === "fulfilled" ? localNgos.value : []);
    } catch {
      setError("Couldn't load organisations right now.");
    } finally {
      setLoading(false);
    }
  }, [keyword, search]);

  useEffect(() => {
    void load();
  }, [load]);

  const hasContent = (external?.length ?? 0) > 0 || local.length > 0;

  return (
    <>
      <NgosHero />
      <section className="border-t border-line bg-white py-12 sm:py-16">
        <div className="container-page">
          <div className="flex flex-wrap items-end justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-wider text-accent">Together for a cleaner city</p>
              <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">NGOs & partner organisations</h2>
              <p className="mt-2 max-w-xl text-muted">
                Non-profits working on water, sanitation and the environment across Hyderabad.
              </p>
            </div>
            <form
              className="flex w-full max-w-sm items-center gap-2"
              onSubmit={(e) => {
                e.preventDefault();
                void load();
              }}
            >
              <div className="relative flex-1">
                <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
                <input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder="Search organisations…"
                  className="w-full rounded-xl border border-line bg-white py-2.5 pl-10 pr-3.5 text-sm transition-colors placeholder:text-muted/70 focus:border-accent focus:outline-none focus:ring-4 focus:ring-accent/15"
                />
              </div>
              <button
                type="submit"
                className="inline-flex h-10 items-center gap-1.5 rounded-xl bg-brand px-4 text-sm font-semibold text-white transition-opacity hover:opacity-90"
              >
                <RefreshCw className={`h-4 w-4 ${loading ? "animate-spin" : ""}`} aria-hidden />
              </button>
            </form>
          </div>

          <div className="mt-5 flex flex-wrap gap-2">
            {KEYWORDS.map((k) => (
              <button
                key={k}
                onClick={() => {
                  setKeyword(k);
                  setSearch("");
                }}
                className={`rounded-xl px-4 py-2 text-sm font-semibold transition-all duration-200 ${
                  keyword === k && !search
                    ? "bg-brand text-white shadow-card"
                    : "border border-line bg-white text-muted hover:border-accent hover:text-brand"
                }`}
              >
                {k}
              </button>
            ))}
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
                    <Globe className="h-3.5 w-3.5" aria-hidden /> Discovered in Hyderabad
                  </p>
                  <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {external.map((o, i) => (
                      <Card
                        key={`${o.name}-${i}`}
                        className="group flex flex-col p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift"
                      >
                        <div className="flex items-start gap-3">
                          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                            <HeartHandshake className="h-5 w-5" aria-hidden />
                          </span>
                          <div className="min-w-0">
                            <h3 className="truncate text-[15px] font-semibold text-ink">{o.name}</h3>
                            {o.rating != null && (
                              <Badge tone="amber" className="mt-1">
                                ★ {o.rating.toFixed(1)}
                              </Badge>
                            )}
                          </div>
                        </div>
                        {o.address && (
                          <p className="mt-3 flex items-start gap-1.5 text-sm text-muted">
                            <MapPin className="mt-0.5 h-4 w-4 shrink-0 text-brand" aria-hidden />
                            <span className="line-clamp-2">{o.address}</span>
                          </p>
                        )}
                        <div className="mt-auto flex flex-wrap gap-3 pt-4 text-xs text-muted">
                          {o.phone && (
                            <span className="inline-flex items-center gap-1">
                              <Phone className="h-3 w-3" aria-hidden /> {o.phone}
                            </span>
                          )}
                          {o.website && (
                            <a
                              href={o.website}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="inline-flex items-center gap-1 font-semibold text-brand hover:underline"
                            >
                              <ExternalLink className="h-3 w-3" aria-hidden /> Website
                            </a>
                          )}
                        </div>
                      </Card>
                    ))}
                  </div>
                </div>
              )}

              {local.length > 0 && (
                <div className="mt-12">
                  <p className="mb-4 inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-brand">
                    <HeartHandshake className="h-3.5 w-3.5" aria-hidden /> Our partners
                  </p>
                  <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {local.map((o) => (
                      <Card key={o.id} className="group flex flex-col p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift">
                        <div className="flex items-center gap-3">
                          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
                            <HeartHandshake className="h-5 w-5" aria-hidden />
                          </span>
                          <div className="min-w-0">
                            <h3 className="truncate text-[15px] font-semibold text-ink">{o.name}</h3>
                            <p className="truncate text-xs text-muted">{o.contactPerson}</p>
                          </div>
                        </div>
                        {o.description && (
                          <p className="mt-3 line-clamp-2 text-sm text-muted">{o.description}</p>
                        )}
                        <div className="mt-auto flex flex-wrap gap-3 pt-4 text-xs text-muted">
                          {o.phone && (
                            <span className="inline-flex items-center gap-1">
                              <Phone className="h-3 w-3" aria-hidden /> {o.phone}
                            </span>
                          )}
                          {o.email && <span className="truncate">{o.email}</span>}
                          {o.website && (
                            <a
                              href={o.website}
                              target="_blank"
                              rel="noopener noreferrer"
                              className="inline-flex items-center gap-1 font-semibold text-brand hover:underline"
                            >
                              <ExternalLink className="h-3 w-3" aria-hidden /> Website
                            </a>
                          )}
                        </div>
                      </Card>
                    ))}
                  </div>
                </div>
              )}

              {!hasContent && (
                <Card className="mt-8">
                  <div className="flex flex-col items-center px-6 py-14 text-center">
                    <HeartHandshake className="h-10 w-10 text-muted/50" aria-hidden />
                    <h3 className="mt-3 text-base font-semibold text-ink">No organisations found</h3>
                    <p className="mt-1 max-w-sm text-sm text-muted">
                      Try a different keyword, or check back soon as we add more partners.
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

function NgosHero() {
  return (
    <section className="border-b border-line bg-white">
      <div className="container-page py-10 text-center sm:py-14 lg:py-20">
        <span className="inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent-soft px-3 py-1 text-xs font-semibold text-brand">
          <HeartHandshake className="h-3.5 w-3.5" aria-hidden /> NGOs & partners
        </span>
        <h1 className="mx-auto mt-5 max-w-2xl text-3xl font-extrabold tracking-tight text-ink sm:text-4xl lg:text-5xl">
          Organisations making <span className="text-brand">clean water</span> a reality
        </h1>
        <p className="mx-auto mt-4 max-w-xl text-base text-muted sm:text-lg">
          Discover non-profits working across Hyderabad on sanitation, water conservation and environmental restoration.
        </p>
      </div>
    </section>
  );
}
