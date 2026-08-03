import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Complaint } from "@/types";

export function useComplaint(id: number | null) {
  const [complaint, setComplaint] = useState<Complaint | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState(false);

  const load = useCallback(async () => {
    if (id == null) return;
    setLoading(true);
    setError(null);
    setNotFound(false);
    try {
      const data = await api.get<Complaint>(`/api/v1/complaints/${id}`);
      setComplaint(data);
    } catch (e) {
      const message = e instanceof Error ? e.message : "Failed to load complaint";
      setError(message);
      if (message.toLowerCase().includes("not found")) setNotFound(true);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    setComplaint(null);
    void load();
  }, [load]);

  return { complaint, loading, error, notFound, reload: load };
}
