import { useEffect, useRef } from "react";
import { useMap } from "react-leaflet";
import type { Map as LeafletMap } from "leaflet";
import {
  buildHeatLut,
  HEAT_BASE_RADIUS_PX,
  HEAT_GLOBAL_ALPHA,
  HEAT_NORM_GAMMA,
  HEAT_POINT_ALPHA,
  HEAT_PULSE_BOOST,
  HEAT_PULSE_PERIOD_MS,
  HEAT_RADIUS_MAX_PX,
  HEAT_RADIUS_MIN_PX,
  HEAT_REFERENCE_ZOOM,
  isPointVisible,
  type HeatPoint,
} from "@/lib/heatmap";
import { clamp, PixelCanvasLayer } from "@/components/maps/canvasLayer";

// ---------------------------------------------------------------------------
// HeatLayer — custom canvas density layer.
//
// Every complaint contributes a soft radial gradient to a low-resolution
// offscreen canvas ("lighter" compositing), so nearby complaints naturally
// blend into smooth, continuous hotspots — no fixed circles anywhere.
//
// The accumulated luminance is then mapped per-pixel through the blue → cyan →
// yellow → orange → red ramp (see lib/heatmap.ts). A requestAnimationFrame
// loop slowly cross-fades the base frame against a boosted frame so hotspots
// gently "breathe", exactly like a live complaint region.
//
// Performance: accumulation runs at ≤ ~360px max dimension, points outside the
// padded viewport are culled before projection, and the pulse loop is capped
// at ~30fps — thousands of complaints stay smooth.
// ---------------------------------------------------------------------------

interface Lut {
  r: Uint8ClampedArray;
  g: Uint8ClampedArray;
  b: Uint8ClampedArray;
  a: Uint8ClampedArray;
}

export class HeatLayer extends PixelCanvasLayer {
  private _points: HeatPoint[] = [];
  private _accCanvas: HTMLCanvasElement | null = null;
  private _outCanvas: HTMLCanvasElement | null = null;
  private _accData: ImageData | null = null;
  private _accWidth = 0;
  private _accHeight = 0;
  private _frameImg: ImageData | null = null;
  private _lutBase: Lut = buildHeatLut();
  private _lutBoost: Lut = buildHeatLut(undefined, 1 + HEAT_PULSE_BOOST);
  private _raf: number | null = null;
  private _lastFrame = 0;

  setPoints(points: HeatPoint[]): void {
    this._points = points;
    this._ensurePulse();
    this._scheduleRedraw(150);
  }

  /** Run the pulse loop only while there is something to animate. */
  private _ensurePulse(): void {
    const shouldRun = this._points.length > 0;
    if (shouldRun && this._raf === null) {
      this._raf = requestAnimationFrame(this._tick);
    } else if (!shouldRun && this._raf !== null) {
      cancelAnimationFrame(this._raf);
      this._raf = null;
    }
  }

  override onAdd(map: LeafletMap): this {
    super.onAdd(map);
    this._raf = requestAnimationFrame(this._tick);
    return this;
  }

  override onRemove(map: LeafletMap): this {
    if (this._raf !== null) cancelAnimationFrame(this._raf);
    this._raf = null;
    super.onRemove(map);
    return this;
  }

  private _tick = (time: number): void => {
    this._raf = requestAnimationFrame(this._tick);
    if (time - this._lastFrame < 33) return; // ~30fps is plenty for a slow pulse
    this._lastFrame = time;
    this._applyPulse(time);
  };

  override redraw(): void {
    if (!this._hostMap || !this._ctx || !this._canvas) return;
    const map = this._hostMap;
    const ctx = this._ctx;
    const size = map.getSize();
    this._syncSize();
    ctx.clearRect(0, 0, size.x, size.y);
    if (this._points.length === 0) {
      this._accData = null;
      return;
    }

    // Low-resolution accumulation canvas (smooth output + fast LUT pass).
    const maxDim = Math.max(size.x, size.y);
    const scale = clamp(Math.ceil(maxDim / 360), 1.5, 8);
    const aw = Math.max(2, Math.ceil(size.x / scale));
    const ah = Math.max(2, Math.ceil(size.y / scale));
    if (!this._accCanvas) this._accCanvas = document.createElement("canvas");
    const acc = this._accCanvas;
    if (acc.width !== aw || acc.height !== ah) {
      acc.width = aw;
      acc.height = ah;
    }
    const actx = acc.getContext("2d");
    if (!actx) return;
    actx.globalCompositeOperation = "source-over";
    actx.clearRect(0, 0, aw, ah);

    const zoom = map.getZoom();
    // Clamp the glow in screen space first, then divide by the accumulation
    // resolution — the visible radius never exceeds HEAT_RADIUS_MAX_PX on
    // screen, so street-level markers stay legible on any layout size.
    const radius = Math.max(
      1,
      clamp(
        HEAT_BASE_RADIUS_PX * map.getZoomScale(HEAT_REFERENCE_ZOOM, zoom),
        HEAT_RADIUS_MIN_PX,
        HEAT_RADIUS_MAX_PX
      ) / scale
    );
    const b = map.getBounds().pad(0.12);

    actx.globalCompositeOperation = "lighter";
    actx.fillStyle = "#ffffff";
    const points = this._points;
    for (let i = 0; i < points.length; i++) {
      const p = points[i];
      if (
        !isPointVisible(p.lat, p.lng, b.getSouth(), b.getNorth(), b.getWest(), b.getEast())
      ) {
        continue;
      }
      const pt = map.latLngToContainerPoint([p.lat, p.lng]);
      const x = pt.x / scale;
      const y = pt.y / scale;
      if (x < -radius || y < -radius || x > aw + radius || y > ah + radius) continue;
      const alpha = HEAT_POINT_ALPHA * (p.weight ?? 1);
      const grad = actx.createRadialGradient(x, y, 0, x, y, radius);
      grad.addColorStop(0, `rgba(255,255,255,${alpha})`);
      grad.addColorStop(0.4, `rgba(255,255,255,${alpha * 0.55})`);
      grad.addColorStop(1, "rgba(255,255,255,0)");
      actx.fillStyle = grad;
      actx.beginPath();
      actx.arc(x, y, radius, 0, Math.PI * 2);
      actx.fill();
    }

    this._accData = actx.getImageData(0, 0, aw, ah);
    this._accWidth = aw;
    this._accHeight = ah;
    // The accumulation canvas stores premultiplied pixels, so the accumulated
    // intensity lives in the ALPHA channel (read-back RGB is always white for
    // white gradients). Map that alpha directly through the ramp's soft knee +
    // gamma: a lone complaint's core (alpha ≈ HEAT_POINT_ALPHA) lands on blue
    // and only saturated cores (alpha clamped to 1) reach red — an absolute
    // scale, so sparse datasets stay cool as specified.
    this._applyPulse(performance.now());
  }

  /** Colour-map the accumulation and draw it upscaled, with a subtle pulse. */
  private _applyPulse(time: number): void {
    if (!this._hostMap || !this._ctx || !this._canvas || !this._accData) return;
    const map = this._hostMap;
    const ctx = this._ctx;
    const size = map.getSize();
    const aw = this._accWidth;
    const ah = this._accHeight;
    if (!this._outCanvas) this._outCanvas = document.createElement("canvas");
    const out = this._outCanvas;
    if (out.width !== aw || out.height !== ah) {
      out.width = aw;
      out.height = ah;
    }
    const octx = out.getContext("2d");
    if (!octx) return;
    // Reuse the frame buffer across frames to avoid ~30fps allocations.
    if (!this._frameImg || this._frameImg.width !== aw || this._frameImg.height !== ah) {
      this._frameImg = octx.createImageData(aw, ah);
    }
    const img = this._frameImg;

    const mix = 0.5 + 0.5 * Math.sin((time / HEAT_PULSE_PERIOD_MS) * Math.PI * 2);
    const acc = this._accData.data;
    const dest = img.data;
    const base = this._lutBase;
    const boost = this._lutBoost;
    const n = aw * ah;
    for (let i = 0; i < n; i++) {
      const alpha = acc[i * 4 + 3] / 255;
      const idx = Math.min(255, (Math.pow(alpha, HEAT_NORM_GAMMA) * 255) | 0);
      const j = i * 4;
      dest[j] = base.r[idx] + (boost.r[idx] - base.r[idx]) * mix;
      dest[j + 1] = base.g[idx] + (boost.g[idx] - base.g[idx]) * mix;
      dest[j + 2] = base.b[idx] + (boost.b[idx] - base.b[idx]) * mix;
      dest[j + 3] = base.a[idx] + (boost.a[idx] - base.a[idx]) * mix;
    }
    octx.putImageData(img, 0, 0);

    ctx.clearRect(0, 0, size.x, size.y);
    ctx.globalAlpha = HEAT_GLOBAL_ALPHA;
    ctx.imageSmoothingEnabled = true;
    ctx.drawImage(out, 0, 0, size.x, size.y);
    ctx.globalAlpha = 1;
  }
}

/**
 * React wrapper — mounts a HeatLayer onto the map and streams new points into
 * it without remounting the layer.
 */
export function HeatmapOverlay({ points }: { points: HeatPoint[] }) {
  const map = useMap();
  const pointsRef = useRef(points);
  const layerRef = useRef<HeatLayer | null>(null);

  useEffect(() => {
    pointsRef.current = points;
    layerRef.current?.setPoints(points);
  }, [points]);

  useEffect(() => {
    const layer = new HeatLayer();
    layer.setPoints(pointsRef.current);
    layer.addTo(map);
    layerRef.current = layer;
    return () => {
      layer.remove();
      layerRef.current = null;
    };
  }, [map]);

  return null;
}
