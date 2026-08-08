import type { ComplaintPriority } from "@/types";

// ---------------------------------------------------------------------------
// Heatmap colour model
//
// Intensity rules (low → high) follow a natural blue → cyan → yellow →
// orange → red thermal ramp:
//   Sparse complaints   → blue
//   Low density         → cyan
//   Medium density      → yellow
//   High density        → orange
//   Very high density   → red
//   Extreme density     → dark red
//
// The heat layer accumulates each complaint as a soft radial gradient on a
// low-resolution offscreen canvas ("lighter" compositing), then maps the
// accumulated luminance per-pixel through a 256-entry lookup table built from
// the ramp below. Overlapping complaints raise the luminance, which pushes the
// colour up the ramp — so clusters naturally grow bigger, brighter and hotter
// without any fixed circles.
// ---------------------------------------------------------------------------

export interface HeatPoint {
  lat: number;
  lng: number;
  /** Optional per-point weight (defaults to 1). */
  weight?: number;
}

/**
 * How much a complaint of each priority boosts its heat contribution.
 *
 * The weight scales the per-point gradient alpha before accumulation, so a
 * higher weight saturates the density ramp faster — CRITICAL complaints glow
 * hotter (yellow/orange/red) than LOW ones (blue/cyan) at the same density.
 */
export const PRIORITY_HEAT_WEIGHT: Record<ComplaintPriority, number> = {
  LOW: 0.8,
  MEDIUM: 1.0,
  HIGH: 1.35,
  CRITICAL: 1.8,
};

/** Map a complaint priority to its heat weight (null/unknown → neutral 1). */
export function heatWeightForPriority(priority: ComplaintPriority | null | undefined): number {
  return priority ? PRIORITY_HEAT_WEIGHT[priority] : 1;
}

export interface RampStop {
  /** Position on the 0..1 intensity scale. */
  t: number;
  /** CSS colour at this stop. */
  color: string;
}

export const HEAT_RAMP: RampStop[] = [
  { t: 0.0, color: "rgba(0, 0, 0, 0)" }, // nothing → transparent
  { t: 0.12, color: "rgba(37, 99, 235, 0.45)" }, // sparse → blue
  { t: 0.3, color: "rgba(34, 211, 238, 0.7)" }, // low → cyan
  { t: 0.5, color: "rgba(250, 204, 21, 0.85)" }, // medium → yellow
  { t: 0.7, color: "rgba(251, 146, 60, 0.92)" }, // high → orange
  { t: 0.88, color: "rgba(239, 68, 68, 1)" }, // very high → red
  { t: 1.0, color: "rgba(153, 27, 27, 1)" }, // extreme → dark red
];

/** Parse an "rgba(r, g, b, a)" colour string into components. */
function parseRgba(color: string): [number, number, number, number] {
  const m = color.match(/rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*([\d.]+))?\)/);
  if (!m) return [0, 0, 0, 1];
  return [Number(m[1]), Number(m[2]), Number(m[3]), m[4] !== undefined ? Number(m[4]) : 1];
}

/** Linear interpolation between two RGB values. */
function lerp(a: number, b: number, t: number): number {
  return a + (b - a) * t;
}

/**
 * Build a 256-entry lookup table (as RGBA byte arrays) from the ramp.
 * `boost` scales intensity — used to precompute a second "pulse" frame.
 *
 * A soft knee (I = 1 − (1 − t)^knee) keeps low densities yellow/orange and
 * only pushes genuinely saturated cores toward red/dark red, instead of
 * letting additive blending blow everything out to the top of the ramp.
 */
export function buildHeatLut(ramp: RampStop[] = HEAT_RAMP, boost = 1): {
  r: Uint8ClampedArray;
  g: Uint8ClampedArray;
  b: Uint8ClampedArray;
  a: Uint8ClampedArray;
} {
  const r = new Uint8ClampedArray(256);
  const g = new Uint8ClampedArray(256);
  const b = new Uint8ClampedArray(256);
  const a = new Uint8ClampedArray(256);

  for (let i = 0; i < 256; i++) {
    const raw = Math.min(1, (i / 255) * boost);
    const t = 1 - Math.pow(1 - raw, HEAT_SOFT_KNEE);
    let lo = ramp[0];
    let hi = ramp[ramp.length - 1];
    for (let j = 0; j < ramp.length - 1; j++) {
      if (t >= ramp[j].t && t <= ramp[j + 1].t) {
        lo = ramp[j];
        hi = ramp[j + 1];
        break;
      }
    }
    const span = Math.max(1e-6, hi.t - lo.t);
    const f = (t - lo.t) / span;
    const [r1, g1, b1, a1] = parseRgba(lo.color);
    const [r2, g2, b2, a2] = parseRgba(hi.color);
    r[i] = Math.round(lerp(r1, r2, f));
    g[i] = Math.round(lerp(g1, g2, f));
    b[i] = Math.round(lerp(b1, b2, f));
    a[i] = Math.round(lerp(a1, a2, f) * 255);
  }
  return { r, g, b, a };
}

/** Soft knee exponent — 1 − (1 − t)^knee, keeps low densities distinct. */
export const HEAT_SOFT_KNEE = 1.4;

/**
 * Exponent applied to the accumulated alpha (0..1) before the LUT lookup.
 * Values > 1 keep isolated complaints cool (blue/cyan) while only genuinely
 * saturated cores climb the ramp to red/dark red.
 */
export const HEAT_NORM_GAMMA = 1.35;

/**
 * Per-complaint gradient center alpha in the accumulation canvas.
 *
 * Kept moderate: at city zoom many complaints overlap, so a large alpha would
 * clamp the additive sum to its ceiling everywhere and flatten the density
 * gradient. This value keeps typical areas mid-ramp (cyan/yellow/orange) and
 * reserves red for genuinely saturated cores.
 */
export const HEAT_POINT_ALPHA = 0.3;

/** Final alpha the heat canvas is drawn with over the tiles. */
export const HEAT_GLOBAL_ALPHA = 0.95;

/** Radius (screen px) of a single complaint's glow at the reference zoom. */
export const HEAT_BASE_RADIUS_PX = 40;
export const HEAT_REFERENCE_ZOOM = 12;

/**
 * Screen-space clamp for the glow radius — caps how large a single complaint's
 * glow can appear on screen, so the heatmap never swallows individual markers
 * at street level regardless of the map container size.
 */
export const HEAT_RADIUS_MIN_PX = 12;
export const HEAT_RADIUS_MAX_PX = 64;

/** Pause length of the pulse animation, in ms. */
export const HEAT_PULSE_PERIOD_MS = 2600;
/** How much the heat "breathes" (1 ± boost). */
export const HEAT_PULSE_BOOST = 0.12;

/** Simple bounding-box test — cheap culling before projecting thousands of points. */
export function isPointVisible(
  lat: number,
  lng: number,
  south: number,
  north: number,
  west: number,
  east: number
): boolean {
  return lat >= south && lat <= north && lng >= west && lng <= east;
}
