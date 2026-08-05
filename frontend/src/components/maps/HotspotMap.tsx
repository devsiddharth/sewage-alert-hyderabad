import { MapContainer, TileLayer, ZoomControl } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { MapPin } from "lucide-react";
import {
  DEFAULT_ZOOM,
  HYDERABAD_BOUNDS,
  HYDERABAD_CENTER,
  MAX_ZOOM,
  MIN_ZOOM,
  TILE_ATTRIBUTION,
  TILE_URLS,
} from "@/lib/hyderabad";
import type { HeatPoint } from "@/lib/heatmap";
import { HeatmapOverlay } from "@/components/maps/HeatmapOverlay";
import { HyderabadBoundaryLayer } from "@/components/maps/HyderabadBoundaryLayer";
import { ComplaintMarkers } from "@/components/maps/ComplaintMarkers";
import { HotspotLegend } from "@/components/maps/HotspotLegend";
import { Spinner } from "@/components/ui/States";
import type { Complaint } from "@/types";

// ---------------------------------------------------------------------------
// HotspotMap — the interactive Hyderabad complaint map.
//
// Layers (bottom → top): tiles → boundary mask + stroke → animated heat →
// clustered complaint markers. Panning is clamped to the Hyderabad metro via
// maxBounds + viscosity, so users can never wander across India.
// ---------------------------------------------------------------------------

export function HotspotMap({
  points,
  complaints,
  dark,
  loading,
  onOpenComplaint,
}: {
  points: HeatPoint[];
  complaints: Complaint[];
  dark: boolean;
  loading: boolean;
  onOpenComplaint: (id: number) => void;
}) {
  return (
    <div className="hp-map-wrap relative h-[520px] overflow-hidden rounded-2xl lg:h-[640px]">
      <MapContainer
        center={HYDERABAD_CENTER}
        zoom={DEFAULT_ZOOM}
        minZoom={MIN_ZOOM}
        maxZoom={MAX_ZOOM}
        maxBounds={HYDERABAD_BOUNDS}
        maxBoundsViscosity={0.85}
        zoomControl={false}
        preferCanvas
        className="hp-map h-full w-full"
      >
        <TileLayer url={dark ? TILE_URLS.dark : TILE_URLS.light} attribution={TILE_ATTRIBUTION} />
        <ZoomControl position="bottomright" />
        <HyderabadBoundaryLayer dark={dark} />
        <HeatmapOverlay points={points} />
        <ComplaintMarkers complaints={complaints} onOpenComplaint={onOpenComplaint} />
      </MapContainer>

      <HotspotLegend />

      {/* Initial load */}
      {loading && (
        <div className="hp-overlay pointer-events-none absolute inset-0 flex items-center justify-center rounded-2xl">
          <div className="hp-glass flex items-center gap-2.5 rounded-full px-4 py-2 text-sm font-medium hp-text">
            <Spinner className="h-4 w-4" /> Loading complaints…
          </div>
        </div>
      )}

      {/* Nothing matches the current filters */}
      {!loading && points.length === 0 && (
        <div className="hp-overlay pointer-events-none absolute inset-0 flex items-center justify-center rounded-2xl">
          <div className="hp-glass flex items-center gap-2 rounded-2xl px-4 py-3 text-sm font-medium hp-text">
            <MapPin className="h-4 w-4 hp-muted" aria-hidden />
            No complaints match the current filters
          </div>
        </div>
      )}
    </div>
  );
}
