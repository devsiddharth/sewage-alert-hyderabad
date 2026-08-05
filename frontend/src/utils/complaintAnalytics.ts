import type { Complaint, ComplaintAnalytics } from "@/types";

/**
 * Grid-cell key for a coordinate pair. Rounds to 2 decimals (~1.1 km cells),
 * the same granularity used by the existing Analytics page.
 */
export function areaKey(lat: number, lng: number): string {
  return `${lat.toFixed(2)},${lng.toFixed(2)}`;
}

/**
 * Computes the analytics cards shown above the hotspot map. Pure function so it
 * stays trivially memoizable and unit-testable. Everything is derived from the
 * complaint list returned by the existing GET /api/v1/complaints endpoint — no
 * backend changes required.
 */
export function computeComplaintAnalytics(complaints: Complaint[]): ComplaintAnalytics {
  let pending = 0;
  let inProgress = 0;
  let resolved = 0;
  let rejected = 0;
  const cellCounts = new Map<string, { count: number; lat: number; lng: number }>();
  const durations: number[] = [];

  for (const c of complaints) {
    switch (c.status) {
      case "PENDING":
        pending++;
        break;
      case "IN_PROGRESS":
        inProgress++;
        break;
      case "RESOLVED":
        resolved++;
        const d = (new Date(c.updatedAt).getTime() - new Date(c.createdAt).getTime()) / 86_400_000;
        if (d >= 0) durations.push(d);
        break;
      case "REJECTED":
        rejected++;
        break;
    }

    if (
      Number.isFinite(c.latitude) &&
      Number.isFinite(c.longitude) &&
      (c.latitude !== 0 || c.longitude !== 0)
    ) {
      const key = areaKey(c.latitude, c.longitude);
      const cell = cellCounts.get(key) ?? { count: 0, lat: c.latitude, lng: c.longitude };
      cell.count++;
      cellCounts.set(key, cell);
    }
  }

  let highestArea: ComplaintAnalytics["highestArea"] = null;
  for (const [key, cell] of cellCounts) {
    if (!highestArea || cell.count > highestArea.count) {
      highestArea = { key, count: cell.count, lat: cell.lat, lng: cell.lng };
    }
  }

  return {
    total: complaints.length,
    pending,
    inProgress,
    resolved,
    rejected,
    avgResolutionDays: durations.length ? durations.reduce((a, b) => a + b, 0) / durations.length : null,
    highestArea,
  };
}
