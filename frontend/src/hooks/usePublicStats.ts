import { useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api";
import type { Complaint } from "@/types";

export interface PublicStats {
  total: number;
  resolved: number;
  pending: number;
  inProgress: number;
  rejected: number;
  avgResolutionDays: number | null;
  recent: Complaint[];
  latest: Complaint | null;
  loading: boolean;
  online: boolean;
}

export function usePublicStats(): PublicStats {
  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [loading, setLoading] = useState(true);
  const [online, setOnline] = useState(false);

  useEffect(() => {
    let cancelled = false;
    api
      .get<Complaint[]>("/api/v1/complaints")
      .then((data) => {
        if (cancelled) return;
        setComplaints(data);
        setOnline(true);
      })
      .catch(() => {
        if (cancelled) return;
        setComplaints([]);
        setOnline(false);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return useMemo(() => {
    const resolvedList = complaints.filter((c) => c.status === "RESOLVED");
    const durations = resolvedList
      .map((c) => (new Date(c.updatedAt).getTime() - new Date(c.createdAt).getTime()) / 86_400_000)
      .filter((d) => Number.isFinite(d) && d >= 0);
    const avgResolutionDays =
      durations.length > 0 ? durations.reduce((a, b) => a + b, 0) / durations.length : null;

    const recent = [...complaints]
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
      .slice(0, 3);

    return {
      total: complaints.length,
      resolved: resolvedList.length,
      pending: complaints.filter((c) => c.status === "PENDING").length,
      inProgress: complaints.filter((c) => c.status === "IN_PROGRESS").length,
      rejected: complaints.filter((c) => c.status === "REJECTED").length,
      avgResolutionDays,
      recent,
      latest: recent[0] ?? null,
      loading,
      online,
    };
  }, [complaints, loading]);
}
