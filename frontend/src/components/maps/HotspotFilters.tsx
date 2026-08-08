import { useState } from "react";
import { CalendarDays, Filter, RotateCcw } from "lucide-react";
import { PRIORITY_OPTIONS, STATUS_OPTIONS } from "@/hooks/useHeatmapFilters";
import { PRIORITY_META, STATUS_META } from "@/types";
import type {
  ComplaintPriority,
  ComplaintStatus,
  DateRangePreset,
  HeatmapFilters,
} from "@/types";
import { cn } from "@/lib/cn";

const PRESETS: Array<{ value: DateRangePreset; label: string }> = [
  { value: "ALL", label: "All time" },
  { value: "TODAY", label: "Today" },
  { value: "LAST_7_DAYS", label: "7 days" },
  { value: "LAST_MONTH", label: "Last month" },
  { value: "CUSTOM", label: "Custom" },
];

const STATUS_DOT: Record<ComplaintStatus, string> = {
  PENDING: "bg-amber-500",
  IN_PROGRESS: "bg-blue-500",
  RESOLVED: "bg-emerald-500",
  REJECTED: "bg-red-500",
};

interface HotspotFiltersProps {
  filters: HeatmapFilters;
  activeCount: number;
  visibleOf: { shown: number; total: number };
  onToggleStatus: (s: ComplaintStatus) => void;
  onTogglePriority: (p: ComplaintPriority) => void;
  onPreset: (p: DateRangePreset) => void;
  onCustomRange: (from: string, to: string) => void;
  onReset: () => void;
}

export function HotspotFilters({
  filters,
  activeCount,
  visibleOf,
  onToggleStatus,
  onTogglePriority,
  onPreset,
  onCustomRange,
  onReset,
}: HotspotFiltersProps) {
  const [open, setOpen] = useState(true);
  const { statuses, priorities, preset, customFrom, customTo } = filters;

  return (
    // On desktop the panel fills its grid column (lg:h-full) so it visually
    // matches the map height; the options block below scrolls internally when
    // the viewport is too short instead of overflowing the page.
    <aside className="hp-glass flex h-fit flex-col gap-5 rounded-2xl p-4 lg:h-full">
      {/* Header (always visible) */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="flex h-8 w-8 items-center justify-center rounded-xl hp-accent-soft hp-accent">
            <Filter className="h-4 w-4" aria-hidden />
          </span>
          <div>
            <h3 className="text-sm font-semibold hp-text">Filters</h3>
            <p className="text-[11px] hp-muted">
              {activeCount > 0 ? `${activeCount} active` : "Showing all"} · {visibleOf.shown}/{visibleOf.total} on map
            </p>
          </div>
        </div>
        <button
          onClick={onReset}
          disabled={activeCount === 0}
          className="rounded-lg p-1.5 hp-muted transition-colors hover:bg-black/5 disabled:opacity-40 dark:hover:bg-white/10"
          title="Reset filters"
          aria-label="Reset filters"
        >
          <RotateCcw className="h-4 w-4" aria-hidden />
        </button>
      </div>

      {/* Mobile toggle */}
      <button
        onClick={() => setOpen((v) => !v)}
        className="flex items-center justify-between rounded-xl border hp-border px-3 py-2 text-sm font-medium hp-text lg:hidden"
      >
        {open ? "Hide filter options" : "Show filter options"}
        <span className={cn("transition-transform", open && "rotate-180")}>▾</span>
      </button>

      <div className={cn("space-y-5 lg:min-h-0 lg:flex-1 lg:overflow-y-auto", !open && "hidden lg:block")}>
        {/* Status */}
        <section>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide hp-muted">Status</p>
          <div className="flex flex-wrap gap-1.5">
            {STATUS_OPTIONS.map((s) => {
              const active = statuses.includes(s);
              return (
                <button
                  key={s}
                  onClick={() => onToggleStatus(s)}
                  aria-pressed={active}
                  className={cn(
                    "hp-chip",
                    active ? "hp-chip-active" : "hp-chip-idle"
                  )}
                >
                  <span className={cn("h-1.5 w-1.5 rounded-full", STATUS_DOT[s])} aria-hidden />
                  {STATUS_META[s].label}
                </button>
              );
            })}
          </div>
        </section>

        {/* Priority */}
        <section>
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide hp-muted">Priority</p>
          <div className="flex flex-wrap gap-1.5">
            {PRIORITY_OPTIONS.map((p) => {
              const active = priorities.includes(p);
              return (
                <button
                  key={p}
                  onClick={() => onTogglePriority(p)}
                  aria-pressed={active}
                  className={cn("hp-chip", active ? "hp-chip-active" : "hp-chip-idle")}
                >
                  {PRIORITY_META[p].label}
                </button>
              );
            })}
          </div>
        </section>

        {/* Date range */}
        <section>
          <p className="mb-2 flex items-center gap-1.5 text-xs font-semibold uppercase tracking-wide hp-muted">
            <CalendarDays className="h-3.5 w-3.5" aria-hidden /> Reported
          </p>
          <div className="flex flex-wrap gap-1.5">
            {PRESETS.map((p) => (
              <button
                key={p.value}
                onClick={() => onPreset(p.value)}
                aria-pressed={preset === p.value}
                className={cn(
                  "hp-chip",
                  preset === p.value ? "hp-chip-active" : "hp-chip-idle"
                )}
              >
                {p.label}
              </button>
            ))}
          </div>

          {preset === "CUSTOM" && (
            <div className="mt-3 grid grid-cols-2 gap-2">
              <label className="hp-field">
                <span className="hp-field-label">From</span>
                <input
                  type="date"
                  value={customFrom}
                  max={customTo || undefined}
                  onChange={(e) => onCustomRange(e.target.value, customTo)}
                  className="hp-date-input"
                />
              </label>
              <label className="hp-field">
                <span className="hp-field-label">To</span>
                <input
                  type="date"
                  value={customTo}
                  min={customFrom || undefined}
                  onChange={(e) => onCustomRange(customFrom, e.target.value)}
                  className="hp-date-input"
                />
              </label>
            </div>
          )}
        </section>

        <p className="border-t hp-border pt-3 text-[11px] leading-relaxed hp-muted">
          Hotspots are generated live from complaint GPS coordinates. Individual markers appear
          when zoomed in.
        </p>
      </div>
    </aside>
  );
}
