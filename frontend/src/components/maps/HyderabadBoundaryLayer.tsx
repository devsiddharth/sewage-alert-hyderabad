import { useEffect, useRef } from "react";
import { useMap } from "react-leaflet";
import { HYDERABAD_BOUNDARY } from "@/lib/hyderabad-boundary";
import { PixelCanvasLayer } from "@/components/maps/canvasLayer";

// ---------------------------------------------------------------------------
// HyderabadBoundaryLayer
//
// NOTE: not currently rendered by the hotspot map (the map shows the full metro
// without a boundary overlay). Retained here, intentionally, so future
// geographic/GHMC-boundary validation can reuse it without a refactor.
//
// Renders the Hyderabad administrative boundary on the map and mutes everything
// outside it:
//   - a translucent veil covers the whole viewport with the boundary "cut out"
//     (evenodd fill), so the rest of the region fades into the background
//   - the boundary itself gets a crisp stroke with a soft glow
// ---------------------------------------------------------------------------

interface BoundaryStyle {
  veil: string;
  stroke: string;
  glow: string;
}

const LIGHT_STYLE: BoundaryStyle = {
  veil: "rgba(10, 36, 99, 0.30)",
  stroke: "#0A2463",
  glow: "rgba(118, 146, 255, 0.85)",
};

const DARK_STYLE: BoundaryStyle = {
  veil: "rgba(2, 6, 17, 0.55)",
  stroke: "#93B4FF",
  glow: "rgba(147, 180, 255, 0.9)",
};

/** Boundary ring as [lng, lat] pairs (GeoJSON winding). */
// The generated boundary file is `as const`, so its tuples are literal
// readonly types — widen them once here into plain mutable pairs.
const RING: Array<[number, number]> = (
  HYDERABAD_BOUNDARY.geometry.coordinates[0] as unknown as Array<[number, number]>
).map(([lng, lat]) => [lng, lat]);

class BoundaryMaskLayer extends PixelCanvasLayer {
  private _style: BoundaryStyle = LIGHT_STYLE;

  setStyle(style: BoundaryStyle): void {
    this._style = style;
    this._scheduleRedraw(0);
  }

  override redraw(): void {
    if (!this._hostMap || !this._ctx || !this._canvas) return;
    const map = this._hostMap;
    const ctx = this._ctx;
    const size = map.getSize();
    this._syncSize();
    ctx.clearRect(0, 0, size.x, size.y);

    const pts: Array<[number, number]> = RING.map(([lng, lat]) => {
      const p = map.latLngToContainerPoint([lat, lng]);
      return [p.x, p.y];
    });

    // Veil: full-viewport rectangle with the municipal polygon cut out.
    ctx.beginPath();
    ctx.rect(0, 0, size.x, size.y);
    for (let i = 0; i < pts.length; i++) {
      const [x, y] = pts[i];
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.closePath();
    ctx.fillStyle = this._style.veil;
    ctx.fill("evenodd");

    // Boundary stroke with a soft glow.
    ctx.beginPath();
    for (let i = 0; i < pts.length; i++) {
      const [x, y] = pts[i];
      if (i === 0) ctx.moveTo(x, y);
      else ctx.lineTo(x, y);
    }
    ctx.closePath();
    ctx.lineWidth = 2.5;
    ctx.strokeStyle = this._style.stroke;
    ctx.shadowColor = this._style.glow;
    ctx.shadowBlur = 12;
    ctx.stroke();
    ctx.shadowBlur = 0;
  }
}

export function HyderabadBoundaryLayer({ dark }: { dark: boolean }) {
  const map = useMap();
  const layerRef = useRef<BoundaryMaskLayer | null>(null);
  const style = dark ? DARK_STYLE : LIGHT_STYLE;

  useEffect(() => {
    layerRef.current?.setStyle(style);
  }, [style]);

  useEffect(() => {
    const layer = new BoundaryMaskLayer();
    layer.setStyle(style);
    layer.addTo(map);
    layerRef.current = layer;
    return () => {
      layer.remove();
      layerRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map]);

  return null;
}
