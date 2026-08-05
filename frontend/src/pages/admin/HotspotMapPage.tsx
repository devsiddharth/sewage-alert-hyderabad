import { useCallback, useEffect, useMemo, useState } from "react";
import { AlertTriangle, Moon, RefreshCw, Sun } from "lucide-react";
import { useComplaintsLive } from "@/hooks/useComplaintsLive";
import { useHeatmapFilters } from "@/hooks/useHeatmapFilters";
import { computeComplaintAnalytics } from "@/utils/complaintAnalytics";
import { reverseGeocodeArea } from "@/services/geocode";
import { HotspotStats } from "@/components/maps/HotspotStats";
import { HotspotFilters } from "@/components/maps/HotspotFilters";
import { HotspotMap } from "@/components/maps/HotspotMap";
import { Modal } from "@/components/ui/Modal";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";
import { Button } from "@/components/ui/Button";
import { cn } from "@/lib/cn";
import type { Complaint } from "@/types";

const EMPTY_LIST: Complaint[] = [];

function hasValidCoords(c: Complaint): boolean {
  return (
    Number.isFinite(c.latitude) &&
    Number.isFinite(c.longitude) &&
    !(c.latitude === 0 && c.longitude === 0)
  );
}

export function HotspotMapPage() {
  const { complaints, loading, error, lastUpdated, reload } = useComplaintsLive();
  const {
    filters,
    filtered,
    toggleStatus,
    togglePriority,
    setPreset,
    setCustomRange,
    reset,
    activeCount,
    visibleOf,
  } = useHeatmapFilters(complaints);

  // Page-level theme (defaults to the OS preference, toggleable).
  const [dark, setDark] = useState<boolean>(
    () => typeof window !== "undefined" && (window.matchMedia?.("(prefers-color-scheme: dark)").matches ?? false)
  );
  const [detailId, setDetailId] = useState<number | null>(null);

  // Heat points — only lat/lng, memoized so the canvas layer never re-renders
  // the React tree when filters change.
  const heatPoints = useMemo(
    () => (filtered ?? EMPTY_LIST).filter(hasValidCoords).map((c) => ({ lat: c.latitude, lng: c.longitude })),
    [filtered]
  );

  const analytics = useMemo(() => computeComplaintAnalytics(complaints ?? []), [complaints]);

  // Best-effort locality name for the top hotspot (falls back to coordinates).
  // Keyed on the grid-cell key alone ("lat,lng") so it only fires when the
  // hotspot actually moves, not on every poll.
  const [areaName, setAreaName] = useState<string | null>(null);
  const topAreaKey = analytics.highestArea?.key ?? null;
  useEffect(() => {
    if (!topAreaKey) {
      setAreaName(null);
      return;
    }
    const [lat, lng] = topAreaKey.split(",").map(Number);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) return;
    let cancelled = false;
    void reverseGeocodeArea(lat, lng).then((name) => {
      if (!cancelled) setAreaName(name);
    });
    return () => {
      cancelled = true;
    };
  }, [topAreaKey]);

  const openComplaint = useCallback((id: number) => setDetailId(id), []);

  return (
    <div className={cn("hotspot-page min-h-[80vh] space-y-5", dark && "dark")}>
      {/* Header */}
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight hp-text sm:text-3xl">Hotspot map</h1>
          <p className="mt-1 text-sm hp-muted">
            Live complaint density across Hyderabad — updated automatically as new reports arrive.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {/* Live badge */}
          <span className="hp-glass flex items-center gap-2 rounded-full px-3 py-1.5 text-xs font-semibold hp-text">
            <span className="relative flex h-2 w-2" aria-hidden>
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-70" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
            </span>
            Live
            {lastUpdated && (
              <span className="font-normal hp-muted">
                · {lastUpdated.toLocaleTimeString("en-IN", { hour: "numeric", minute: "2-digit" })}
              </span>
            )}
          </span>
          <button
            onClick={() => void reload()}
            className="hp-glass rounded-full p-2 hp-muted transition-colors hover:text-brand dark:hover:text-white"
            title="Refresh now"
            aria-label="Refresh complaints"
          >
            <RefreshCw className="h-4 w-4" aria-hidden />
          </button>
          <button
            onClick={() => setDark((v) => !v)}
            className="hp-glass rounded-full p-2 hp-muted transition-colors hover:text-brand dark:hover:text-white"
            title={dark ? "Switch to light map" : "Switch to dark map"}
            aria-label="Toggle map theme"
          >
            {dark ? <Sun className="h-4 w-4" aria-hidden /> : <Moon className="h-4 w-4" aria-hidden />}
          </button>
        </div>
      </div>

      {/* Error banner */}
      {error && (
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-red-300/60 bg-red-50 px-4 py-3 dark:border-red-500/30 dark:bg-red-950/40">
          <p className="flex items-center gap-2 text-sm font-medium text-red-700 dark:text-red-300">
            <AlertTriangle className="h-4 w-4 shrink-0" aria-hidden />
            {error}
          </p>
          <Button size="sm" variant="outline" onClick={() => void reload()}>
            Retry
          </Button>
        </div>
      )}

      {/* Analytics strip */}
      <HotspotStats analytics={analytics} loading={loading} areaName={areaName} />

      {/* Filters + map */}
      <div className="grid gap-5 lg:grid-cols-[300px_1fr]">
        <HotspotFilters
          filters={filters}
          activeCount={activeCount}
          visibleOf={visibleOf}
          onToggleStatus={toggleStatus}
          onTogglePriority={togglePriority}
          onPreset={setPreset}
          onCustomRange={setCustomRange}
          onReset={reset}
        />
        <HotspotMap
          points={heatPoints}
          complaints={filtered ?? EMPTY_LIST}
          dark={dark}
          loading={loading}
          onOpenComplaint={openComplaint}
        />
      </div>

      {/* Full complaint details */}
      <Modal
        open={detailId !== null}
        onClose={() => setDetailId(null)}
        title="Complaint details"
        size="lg"
      >
        {detailId !== null && <ComplaintDetailView complaintId={detailId} onNotFound={() => setDetailId(null)} />}
      </Modal>
    </div>
  );
}
