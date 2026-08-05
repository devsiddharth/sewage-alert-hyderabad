// ---------------------------------------------------------------------------
// Best-effort reverse geocoding for the "highest complaint area" card.
//
// Uses the public Nominatim service (OpenStreetMap) with a module-level cache
// and a hard timeout. Failures return null and the UI falls back to showing
// the raw grid coordinates — so this never blocks or breaks the page.
// ---------------------------------------------------------------------------

const cache = new Map<string, string>();
const REQUEST_TIMEOUT_MS = 3500;

export async function reverseGeocodeArea(lat: number, lng: number): Promise<string | null> {
  const key = `${lat.toFixed(3)},${lng.toFixed(3)}`;
  const cached = cache.get(key);
  if (cached !== undefined) return cached;

  const ctrl = new AbortController();
  const timer = window.setTimeout(() => ctrl.abort(), REQUEST_TIMEOUT_MS);
  try {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?format=jsonv2&zoom=13&accept-language=en&lat=${lat}&lon=${lng}`,
      { signal: ctrl.signal, headers: { Accept: "application/json" } }
    );
    if (!res.ok) return null;

    const data = (await res.json()) as {
      address?: {
        suburb?: string;
        neighbourhood?: string;
        town?: string;
        city?: string;
        city_district?: string;
      };
    };
    const name =
      data.address?.suburb ??
      data.address?.neighbourhood ??
      data.address?.town ??
      data.address?.city_district ??
      data.address?.city ??
      null;
    if (name) cache.set(key, name);
    return name;
  } catch {
    return null;
  } finally {
    window.clearTimeout(timer);
  }
}
