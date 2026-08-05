import { useCallback, useEffect, useRef, useState } from "react";
import { fetchAllComplaints } from "@/services/complaints";
import type { Complaint } from "@/types";

interface UseComplaintsLiveOptions {
  /** How often to re-fetch, in ms. */
  intervalMs?: number;
}

/**
 * Cheap signature of a complaint list — lets the hook skip re-rendering the
 * whole page when a poll returns identical data (new complaints, status
 * changes and edits all change the hash; unchanged data does not).
 */
function signatureOf(list: Complaint[]): string {
  let h = 0;
  for (let i = 0; i < list.length; i++) {
    const c = list[i];
    h = (h * 31 + c.id) | 0;
    const u = c.updatedAt;
    for (let j = 0; j < u.length; j++) h = (h + u.charCodeAt(j) * 3) | 0;
  }
  return `${list.length}:${h}`;
}

/**
 * Complaints data source for the hotspot map. Polls the existing
 * GET /api/v1/complaints endpoint on an interval and on window focus so newly
 * submitted complaints flow into the heat layer without any page refresh.
 *
 * `loading` only flips on the first load — subsequent background refreshes keep
 * the map stable and never flash a spinner.
 */
export function useComplaintsLive({ intervalMs = 30_000 }: UseComplaintsLiveOptions = {}) {
  const [complaints, setComplaints] = useState<Complaint[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);
  const [bump, setBump] = useState(0);
  // Request sequencing: if interval/focus/manual fetches overlap, only the most
  // recent response is applied.
  const seqRef = useRef(0);
  const lastSigRef = useRef<string | null>(null);

  const load = useCallback(async () => {
    const seq = ++seqRef.current;
    try {
      const data = await fetchAllComplaints();
      if (seq !== seqRef.current) return; // a newer request is already in flight
      const sig = signatureOf(data);
      if (sig === lastSigRef.current) {
        // Data unchanged — still tick the clock, but don't touch state.
        setLastUpdated(new Date());
        setError(null);
        return;
      }
      lastSigRef.current = sig;
      setComplaints(data);
      setLastUpdated(new Date());
      setError(null);
    } catch (e) {
      if (seq !== seqRef.current) return;
      setError(e instanceof Error ? e.message : "Failed to load complaints");
    } finally {
      if (seq === seqRef.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load, bump]);

  useEffect(() => {
    const id = window.setInterval(() => void load(), intervalMs);
    return () => window.clearInterval(id);
  }, [load, intervalMs]);

  // Refresh when the tab regains focus — keeps the map fresh without heavier polling.
  useEffect(() => {
    const onFocus = () => void load();
    window.addEventListener("focus", onFocus);
    return () => window.removeEventListener("focus", onFocus);
  }, [load]);

  return {
    complaints,
    loading,
    error,
    lastUpdated,
    reload: () => setBump((v) => v + 1),
  };
}
