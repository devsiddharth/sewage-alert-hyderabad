// Legend overlay pinned to the bottom-left of the map. The gradient mirrors the
// heat ramp in lib/heatmap.ts (yellow → orange → deep orange → red → dark red).
export function HotspotLegend() {
  return (
    <div className="hp-legend" aria-hidden>
      <p className="hp-legend-title">Complaint density</p>
      <div className="hp-legend-gradient" />
      <div className="mt-1 flex items-center justify-between text-[10px] font-medium hp-muted">
        <span>Low</span>
        <span>High</span>
      </div>
    </div>
  );
}
