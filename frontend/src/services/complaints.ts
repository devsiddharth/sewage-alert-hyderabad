import { api } from "@/lib/api";
import type { Complaint } from "@/types";

// ---------------------------------------------------------------------------
// Complaint service client.
//
// Reuses the existing GET /api/v1/complaints endpoint (via the gateway). All
// hotspot analytics and heatmap density are derived client-side from this one
// list — no new backend endpoints were introduced.
// ---------------------------------------------------------------------------

export function fetchAllComplaints(): Promise<Complaint[]> {
  return api.get<Complaint[]>("/api/v1/complaints");
}

export function fetchComplaint(id: number): Promise<Complaint> {
  return api.get<Complaint>(`/api/v1/complaints/${id}`);
}
