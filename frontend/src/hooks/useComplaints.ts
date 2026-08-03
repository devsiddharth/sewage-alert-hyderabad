import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Complaint } from "@/types";

export function useComplaints(params?: { userId?: number }) {
  const [complaints, setComplaints] = useState<Complaint[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const qs = params?.userId ? `?userId=${params.userId}` : "";
      const data = await api.get<Complaint[]>(`/api/v1/complaints${qs}`);
      setComplaints(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load complaints");
    } finally {
      setLoading(false);
    }
  }, [params?.userId]);

  useEffect(() => {
    void load();
  }, [load]);

  return { complaints, loading, error, reload: load };
}
