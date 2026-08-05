import { useCallback, useMemo, useState } from "react";
import {
  DEFAULT_HEATMAP_FILTERS,
  type Complaint,
  type ComplaintPriority,
  type ComplaintStatus,
  type DateRangePreset,
  type HeatmapFilters,
} from "@/types";

export const STATUS_OPTIONS: ComplaintStatus[] = ["PENDING", "IN_PROGRESS", "RESOLVED", "REJECTED"];
export const PRIORITY_OPTIONS: ComplaintPriority[] = ["LOW", "MEDIUM", "HIGH", "CRITICAL"];

function matchesDateRange(
  createdAt: string,
  preset: DateRangePreset,
  customFrom: string,
  customTo: string
): boolean {
  const t = new Date(createdAt).getTime();
  if (!Number.isFinite(t)) return false;
  const now = new Date();
  switch (preset) {
    case "TODAY": {
      const start = new Date(now);
      start.setHours(0, 0, 0, 0);
      return t >= start.getTime();
    }
    case "LAST_7_DAYS": {
      const start = new Date(now);
      start.setDate(start.getDate() - 7);
      return t >= start.getTime();
    }
    case "LAST_MONTH": {
      const start = new Date(now);
      start.setDate(start.getDate() - 30);
      return t >= start.getTime();
    }
    case "CUSTOM": {
      if (customFrom) {
        const from = new Date(`${customFrom}T00:00:00`).getTime();
        if (t < from) return false;
      }
      if (customTo) {
        const to = new Date(`${customTo}T23:59:59`).getTime();
        if (t > to) return false;
      }
      return true;
    }
    default:
      return true;
  }
}

export function useHeatmapFilters(all: Complaint[] | null) {
  const [filters, setFilters] = useState<HeatmapFilters>(DEFAULT_HEATMAP_FILTERS);

  const toggle = useCallback(
    (key: "statuses" | "priorities", value: string) => {
      setFilters((prev) => {
        const list = prev[key];
        const next = list.includes(value as never)
          ? list.filter((v) => v !== value)
          : [...list, value as never];
        return { ...prev, [key]: next };
      });
    },
    []
  );

  const setPreset = useCallback((preset: DateRangePreset) => {
    setFilters((prev) => ({ ...prev, preset }));
  }, []);

  const setCustomRange = useCallback((from: string, to: string) => {
    setFilters((prev) => ({ ...prev, customFrom: from, customTo: to }));
  }, []);

  const reset = useCallback(() => setFilters(DEFAULT_HEATMAP_FILTERS), []);

  const filtered = useMemo(() => {
    if (!all) return null;
    const { statuses, priorities, preset, customFrom, customTo } = filters;
    const statusActive = statuses.length > 0;
    const priorityActive = priorities.length > 0;
    return all.filter((c) => {
      if (statusActive && !statuses.includes(c.status)) return false;
      if (priorityActive && !priorities.includes(c.priority as ComplaintPriority)) return false;
      if (preset !== "ALL" && !matchesDateRange(c.createdAt, preset, customFrom, customTo)) return false;
      return true;
    });
  }, [all, filters]);

  const activeCount =
    filters.statuses.length +
    filters.priorities.length +
    (filters.preset !== "ALL" ? 1 : 0);

  return {
    filters,
    toggleStatus: (s: ComplaintStatus) => toggle("statuses", s),
    togglePriority: (p: ComplaintPriority) => toggle("priorities", p),
    setPreset,
    setCustomRange,
    reset,
    filtered,
    activeCount,
    visibleOf: { shown: filtered?.length ?? 0, total: all?.length ?? 0 },
  };
}
