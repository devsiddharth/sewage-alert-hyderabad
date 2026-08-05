import type { ReactNode } from "react";
import { motion } from "framer-motion";
import { CheckCircle2, Clock, FileText, Inbox, MapPin, Timer, XCircle } from "lucide-react";
import type { ComplaintAnalytics } from "@/types";
import { cn } from "@/lib/cn";

// ---------------------------------------------------------------------------
// HotspotStats — the analytics strip above the map.
//
// All values are derived client-side from the complaints returned by the
// existing GET /api/v1/complaints endpoint (see utils/complaintAnalytics.ts).
// ---------------------------------------------------------------------------

function GlassStat({
  label,
  value,
  icon,
  tone,
  hint,
  index,
}: {
  label: string;
  value: string | number;
  icon: ReactNode;
  tone: string;
  hint?: string;
  index: number;
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.045, duration: 0.3, ease: "easeOut" }}
      className="hp-glass flex flex-col rounded-2xl p-4"
    >
      <div className="flex items-center justify-between gap-2">
        <span className="truncate text-[11px] font-semibold uppercase tracking-wide hp-muted">{label}</span>
        <span className={cn("flex h-8 w-8 shrink-0 items-center justify-center rounded-xl", tone)}>
          {icon}
        </span>
      </div>
      <p className="mt-2 text-2xl font-bold tabular-nums hp-text">{value}</p>
      {hint && <p className="mt-0.5 truncate text-[11px] hp-muted">{hint}</p>}
    </motion.div>
  );
}

const tones = {
  brand: "hp-icon-brand",
  amber: "hp-icon-amber",
  blue: "hp-icon-blue",
  green: "hp-icon-green",
  red: "hp-icon-red",
};

export function HotspotStats({
  analytics,
  loading,
  areaName,
}: {
  analytics: ComplaintAnalytics;
  loading: boolean;
  areaName: string | null;
}) {
  const area = analytics.highestArea;
  const areaHint = area
    ? `${areaName ?? `${area.lat.toFixed(3)}, ${area.lng.toFixed(3)}`} · ${area.count} report${area.count === 1 ? "" : "s"}`
    : "No data yet";

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-7">
      <GlassStat
        index={0}
        label="Total complaints"
        value={loading ? "…" : analytics.total}
        icon={<FileText className="h-4 w-4" aria-hidden />}
        tone={tones.brand}
      />
      <GlassStat
        index={1}
        label="Pending"
        value={loading ? "…" : analytics.pending}
        icon={<Inbox className="h-4 w-4" aria-hidden />}
        tone={tones.amber}
      />
      <GlassStat
        index={2}
        label="In progress"
        value={loading ? "…" : analytics.inProgress}
        icon={<Clock className="h-4 w-4" aria-hidden />}
        tone={tones.blue}
      />
      <GlassStat
        index={3}
        label="Resolved"
        value={loading ? "…" : analytics.resolved}
        icon={<CheckCircle2 className="h-4 w-4" aria-hidden />}
        tone={tones.green}
      />
      <GlassStat
        index={4}
        label="Rejected"
        value={loading ? "…" : analytics.rejected}
        icon={<XCircle className="h-4 w-4" aria-hidden />}
        tone={tones.red}
      />
      <GlassStat
        index={5}
        label="Highest area"
        value={loading || !area ? "—" : `#${area.count}`}
        icon={<MapPin className="h-4 w-4" aria-hidden />}
        tone={tones.brand}
        hint={loading ? "Loading…" : areaHint}
      />
      <GlassStat
        index={6}
        label="Avg resolution"
        value={loading || analytics.avgResolutionDays === null ? "—" : `${analytics.avgResolutionDays.toFixed(1)}d`}
        icon={<Timer className="h-4 w-4" aria-hidden />}
        tone={tones.green}
        hint={analytics.avgResolutionDays === null && !loading ? "Not enough data" : "Resolved → closed"}
      />
    </div>
  );
}
