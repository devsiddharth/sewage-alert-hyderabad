import * as L from "leaflet";

// ---------------------------------------------------------------------------
// Shared base for pixel-space canvas overlays (heat layer, boundary mask).
//
// The canvas is appended to Leaflet's overlayPane and redrawn whenever the map
// view settles (moveend / zoomend / resize, debounced). During pans the canvas
// element is repositioned cheaply per frame so the heat "sticks" to the
// geography beneath it.
// ---------------------------------------------------------------------------

export abstract class PixelCanvasLayer extends L.Layer {
  /** Host map (null while the layer is not added to any map). */
  protected _hostMap: L.Map | null = null;
  protected _canvas: HTMLCanvasElement | null = null;
  protected _ctx: CanvasRenderingContext2D | null = null;
  private _redrawTimer: number | null = null;
  private _moveFrame: number | null = null;

  private _onViewSettle = (): void => {
    this._scheduleRedraw();
  };

  onAdd(map: L.Map): this {
    this._hostMap = map;
    const canvas = L.DomUtil.create("canvas", "hp-canvas leaflet-zoom-animated") as HTMLCanvasElement;
    canvas.style.position = "absolute";
    canvas.style.top = "0";
    canvas.style.left = "0";
    map.getPane("overlayPane")?.appendChild(canvas);
    this._canvas = canvas;
    this._ctx = canvas.getContext("2d");
    this._syncSize();
    this._syncPosition();
    map.on("move", this._onMove, this);
    map.on("moveend zoomend resize", this._onViewSettle, this);
    this.redraw();
    return this;
  }

  onRemove(map: L.Map): this {
    if (this._redrawTimer !== null) window.clearTimeout(this._redrawTimer);
    if (this._moveFrame !== null) cancelAnimationFrame(this._moveFrame);
    map.off("move", this._onMove, this);
    map.off("moveend zoomend resize", this._onViewSettle, this);
    this._canvas?.remove();
    this._hostMap = null;
    this._canvas = null;
    this._ctx = null;
    return this;
  }

  /** Schedule a full redraw after the view settles. */
  protected _scheduleRedraw(delay = 80): void {
    if (this._redrawTimer !== null) window.clearTimeout(this._redrawTimer);
    this._redrawTimer = window.setTimeout(() => {
      this._redrawTimer = null;
      this.redraw();
    }, delay);
  }

  private _onMove(): void {
    if (this._moveFrame !== null) return;
    this._moveFrame = requestAnimationFrame(() => {
      this._moveFrame = null;
      this._syncPosition();
    });
  }

  private _syncPosition(): void {
    if (!this._hostMap || !this._canvas) return;
    L.DomUtil.setPosition(this._canvas, this._hostMap.containerPointToLayerPoint([0, 0]));
  }

  protected _syncSize(): void {
    if (!this._hostMap || !this._canvas) return;
    const size = this._hostMap.getSize();
    if (this._canvas.width !== size.x || this._canvas.height !== size.y) {
      this._canvas.width = size.x;
      this._canvas.height = size.y;
    }
  }

  abstract redraw(): void;
}

export function clamp(v: number, min: number, max: number): number {
  return v < min ? min : v > max ? max : v;
}
