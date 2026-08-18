import { useCallback, useEffect, useState } from "react";
import { BookOpen, ExternalLink, Newspaper, RefreshCw, Sparkles } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton, ErrorState } from "@/components/ui/States";
import { api } from "@/lib/api";
import { formatDate, timeAgo } from "@/lib/utils";
import type { Article, ArticleFeedItem } from "@/types";

const TOPICS = ["Water", "Sewage", "Hyderabad", "Environment"] as const;
type Topic = (typeof TOPICS)[number];

export function ArticlesPage() {
  const [topic, setTopic] = useState<Topic>("Water");
  const [feed, setFeed] = useState<ArticleFeedItem[] | null>(null);
  const [local, setLocal] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [external, localArticles] = await Promise.allSettled([
        api.get<ArticleFeedItem[]>(`/api/v1/articles/latest?keyword=${topic}`),
        api.get<Article[]>(`/api/v1/articles`),
      ]);
      setFeed(external.status === "fulfilled" && external.value.length > 0 ? external.value : null);
      setLocal(localArticles.status === "fulfilled" ? localArticles.value : []);
    } catch {
      setError("Couldn't load articles right now.");
    } finally {
      setLoading(false);
    }
  }, [topic]);

  useEffect(() => {
    void load();
  }, [load]);

  const hasContent = (feed?.length ?? 0) > 0 || local.length > 0;

  return (
    <>
      <ArticlesHero />
      <section className="border-t border-line bg-white py-12 sm:py-16">
        <div className="container-page">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="text-sm font-semibold uppercase tracking-wider text-accent">Learn & stay informed</p>
              <h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">News & articles</h2>
            </div>
            <div className="flex flex-wrap gap-2">
              {TOPICS.map((t) => (
                <button
                  key={t}
                  onClick={() => setTopic(t)}
                  className={`rounded-xl px-4 py-2 text-sm font-semibold transition-all duration-200 ${
                    topic === t
                      ? "bg-brand text-white shadow-card"
                      : "border border-line bg-white text-muted hover:border-accent hover:text-brand"
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>
          </div>

          <div className="mt-6 flex items-center justify-between gap-3">
            <p className="text-sm text-muted">
              {feed
                ? "Live news curated from trusted sources (GNews)."
                : "Latest reading from the SewageAlert library."}
            </p>
            <button
              onClick={() => void load()}
              className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-semibold text-brand transition-colors hover:bg-accent-soft"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${loading ? "animate-spin" : ""}`} aria-hidden /> Refresh
            </button>
          </div>

          {loading ? (
            <div className="mt-8 grid gap-5 md:grid-cols-2 lg:grid-cols-3">
              {[0, 1, 2, 3, 4, 5].map((i) => (
                <Skeleton key={i} className="h-56 w-full rounded-2xl" />
              ))}
            </div>
          ) : error && !hasContent ? (
            <Card className="mt-8">
              <ErrorState message={error} onRetry={() => void load()} />
            </Card>
          ) : (
            <>
              {feed && feed.length > 0 && (
                <div className="mt-8">
                  <p className="mb-4 inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-brand">
                    <Sparkles className="h-3.5 w-3.5" aria-hidden /> Live from the news
                  </p>
                  <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {feed.map((a, i) => (
                      <a
                        key={`${a.title}-${i}`}
                        href={a.articleUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="group flex flex-col overflow-hidden rounded-2xl border border-line bg-white shadow-card transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift"
                      >
                        {a.imageUrl ? (
                          <div className="relative h-40 overflow-hidden bg-slate-100">
                            <img
                              src={a.imageUrl}
                              alt=""
                              loading="lazy"
                              className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                            />
                          </div>
                        ) : (
                          <div className="flex h-40 items-center justify-center bg-gradient-to-br from-accent-soft to-emerald-50 text-brand">
                            <Newspaper className="h-10 w-10" aria-hidden />
                          </div>
                        )}
                        <div className="flex flex-1 flex-col p-5">
                          <div className="flex items-center gap-2 text-xs text-muted">
                            <span className="font-semibold text-brand">{a.source}</span>
                            <span aria-hidden>·</span>
                            <span>{a.publishedAt ? timeAgo(a.publishedAt) : "recent"}</span>
                          </div>
                          <h3 className="mt-2 line-clamp-3 text-[15px] font-semibold text-ink group-hover:text-brand">
                            {a.title}
                          </h3>
                          {a.description && (
                            <p className="mt-2 line-clamp-2 text-sm text-muted">{a.description}</p>
                          )}
                          <span className="mt-auto inline-flex items-center gap-1 pt-4 text-xs font-semibold text-brand">
                            Read article <ExternalLink className="h-3.5 w-3.5" aria-hidden />
                          </span>
                        </div>
                      </a>
                    ))}
                  </div>
                </div>
              )}

              {local.length > 0 && (
                <div className="mt-12">
                  <p className="mb-4 inline-flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide text-brand">
                    <BookOpen className="h-3.5 w-3.5" aria-hidden /> From our library
                  </p>
                  <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">
                    {local.map((a) => (
                      <Card key={a.id} className="group flex flex-col p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift">
                        <div className="flex items-center gap-2">
                          <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-accent-soft text-brand">
                            <BookOpen className="h-4 w-4" aria-hidden />
                          </span>
                          <Badge>{a.category}</Badge>
                        </div>
                        <h3 className="mt-3 line-clamp-2 text-[15px] font-semibold text-ink group-hover:text-brand">{a.title}</h3>
                        <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-muted">{a.content}</p>
                        <p className="mt-auto pt-4 text-xs text-muted">
                          {a.authorName} · {formatDate(a.publishedAt)}
                        </p>
                      </Card>
                    ))}
                  </div>
                </div>
              )}

              {!hasContent && (
                <Card className="mt-8">
                  <div className="flex flex-col items-center px-6 py-14 text-center">
                    <BookOpen className="h-10 w-10 text-muted/50" aria-hidden />
                    <h3 className="mt-3 text-base font-semibold text-ink">No articles yet</h3>
                    <p className="mt-1 max-w-sm text-sm text-muted">
                      Articles will appear here once published or once the live news feed is connected.
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

function ArticlesHero() {
  return (
    <section className="border-b border-line bg-white">
      <div className="container-page py-10 text-center sm:py-14 lg:py-20">
        <span className="inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent-soft px-3 py-1 text-xs font-semibold text-brand">
          <BookOpen className="h-3.5 w-3.5" aria-hidden /> Articles & news
        </span>
        <h1 className="mx-auto mt-5 max-w-2xl text-3xl font-extrabold tracking-tight text-ink sm:text-4xl lg:text-5xl">
          Water, sewage & <span className="text-brand">sustainability</span> — explained
        </h1>
        <p className="mx-auto mt-4 max-w-xl text-base text-muted sm:text-lg">
          Fresh news from trusted sources, plus in-depth guides on how Hyderabad treats its water and keeps its drains clean.
        </p>
      </div>
    </section>
  );
}
