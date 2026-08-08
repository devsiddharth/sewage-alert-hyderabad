// ---------------------------------------------------------------------------
// Hyderabad map configuration
//
// The map is centered on Hyderabad but framed wide enough to show the whole
// metro area plus the surrounding districts (Medchal, Rangareddy, Sangareddy):
//   - default center = centroid of the district boundary
//   - maxBounds = generously padded metro rectangle so surrounding regions stay
//     reachable while panning stays clamped well short of a subcontinent view
//   - zoom limits start at a city-wide overview (11) and allow zooming out to
//     the wider region (9) for context
//
// Note: with Leaflet's default zoomSnap=1, zoom values are rounded to whole
// numbers, so the initial zoom must be an integer to actually take effect.
// ---------------------------------------------------------------------------

/** Approximate center of Hyderabad (from the boundary centroid). */
export const HYDERABAD_CENTER: [number, number] = [17.41443, 78.4688];

/** Initial zoom when the map first loads — a metro-wide overview. */
export const DEFAULT_ZOOM = 11;

/** Hard zoom limits — region context at the outer edge, street level at the inner. */
export const MIN_ZOOM = 9;
export const MAX_ZOOM = 18;

/**
 * Navigation bounds for the wider Hyderabad metro (padded well beyond the
 * municipal polygon so surrounding regions and nearby complaints remain
 * reachable, but far tighter than India).
 */
export const HYDERABAD_BOUNDS: [[number, number], [number, number]] = [
  [17.0, 77.85],
  [17.8, 79.0],
];

/** Tile layer URLs — light (Voyager) and dark (Dark Matter), both CARTO/OSM. */
export const TILE_URLS = {
  light: "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png",
  dark: "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png",
} as const;

export const TILE_ATTRIBUTION =
  '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>';
