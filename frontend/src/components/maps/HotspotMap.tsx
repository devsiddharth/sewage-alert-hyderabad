import { useEffect } from "react";
import { MapContainer, TileLayer, useMap, ZoomControl } from "react-leaflet";
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
import { ComplaintMarkers } from "@/components/maps/ComplaintMarkers";
import { HotspotLegend } from "@/components/maps/HotspotLegend";
import { Spinner } from "@/components/ui/States";
import type { Complaint } from "@/types";

// ---------------------------------------------------------------------------
// HotspotMap — the interactive Hyderabad complaint map.
//
// Layers (bottom → top): tiles → animated heat → clustered complaint markers.
// Both the light and dark tile sets stay mounted and cross-fade via opacity on
// theme switch, so toggling the theme never flashes a blank map. Panning is
// clamped to the wider Hyderabad metro via maxBounds + viscosity.
// ---------------------------------------------------------------------------

// Keeps Leaflet's internal size in sync with the wrapper. The map's height is
// flex-driven now (it stretches to fill the viewport), and Leaflet only
// auto-invalidates on window resize — so observe the container itself and
// refresh on any layout shift (fonts settling, stats placeholders resolving,
// theme toggle reflow, etc.).
function MapAutoResize() {
  const map = useMap();
  useEffect(() => {
    const el = map.getContainer();
    const observer = new ResizeObserver(() => {
      map.invalidateSize();
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, [map]);
  return null;
}

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
  // The wrapper fills its grid cell: on desktop the cell is sized by the
  // page's flex layout (flex-1), so the map consumes every remaining pixel of
  // the viewport. Mobile keeps a responsive viewport-based height; min-h
  // guards against the map collapsing on very short screens.
  return (
    <div className="hp-map-wrap relative h-[50vh] min-h-[320px] overflow-hidden rounded-2xl lg:h-full">
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
        <MapAutoResize />
        {/* Both tile sets stay mounted; opacity cross-fades on theme switch so
            there is no white flash while the new tiles download. */}
        <TileLayer url={TILE_URLS.light} attribution={TILE_ATTRIBUTION} opacity={dark ? 0 : 1} />
        <TileLayer url={TILE_URLS.dark} attribution={TILE_ATTRIBUTION} opacity={dark ? 1 : 0} />
        <ZoomControl position="bottomright" />
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
