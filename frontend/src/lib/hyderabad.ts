// ---------------------------------------------------------------------------
// Hyderabad map configuration
//
// The map is locked to the Hyderabad municipal region:
//   - default center = centroid of the district boundary
//   - maxBounds = padded metro rectangle (complaints may fall slightly outside
//     the district polygon, e.g. Quthbullapur), panning is clamped here
//   - zoom limits keep the view city-scale — no zooming out to India/world
// ---------------------------------------------------------------------------

/** Approximate center of Hyderabad (from the boundary centroid). */
export const HYDERABAD_CENTER: [number, number] = [17.41443, 78.4688];

/** Initial zoom when the map first loads. */
export const DEFAULT_ZOOM = 12.5;

/** Hard zoom limits — the user can never zoom out to a subcontinent view. */
export const MIN_ZOOM = 10;
export const MAX_ZOOM = 17;

/**
 * Navigation bounds for the whole Hyderabad metro region (padded well beyond
 * the municipal polygon so nearby complaints remain reachable, but far tighter
 * than India).
 */
export const HYDERABAD_BOUNDS: [[number, number], [number, number]] = [
  [17.15, 78.05],
  [17.65, 78.8],
];

/** Tile layer URLs — light (Voyager) and dark (Dark Matter), both CARTO/OSM. */
export const TILE_URLS = {
  light: "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png",
  dark: "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png",
} as const;

export const TILE_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>';
