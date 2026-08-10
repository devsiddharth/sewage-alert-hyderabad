import { api } from "@/lib/api";
import type { Complaint, ComplaintStatusRequest, FieldOfficer } from "@/types";

// ---------------------------------------------------------------------------
// Complaint assignment workflow API client.
//
//   • Field officers  → GET  /api/v1/auth/admin/field-officers   (auth-service)
//   • Assign          → PUT  /api/v1/complaints/admin/{id}/assign
//   • Officer list    → GET  /api/v1/complaints/field-officer
//   • Officer update  → PATCH /api/v1/complaints/field-officer/{id}/status
//
// The acting admin/officer id is always sent by the API client as
// X-Auth-User-Id and re-verified server-side — never passed in the body.
// ---------------------------------------------------------------------------

export function fetchFieldOfficers(): Promise<FieldOfficer[]> {
  return api.get<FieldOfficer[]>("/api/v1/auth/admin/field-officers");
}

export function assignComplaint(complaintId: number, fieldOfficerId: number): Promise<Complaint> {
  return api.put<Complaint>(`/api/v1/complaints/admin/${complaintId}/assign`, {
    fieldOfficerId,
  });
}

export function fetchAssignedComplaints(): Promise<Complaint[]> {
  return api.get<Complaint[]>("/api/v1/complaints/field-officer");
}

export function updateAssignedComplaintStatus(
  complaintId: number,
  request: ComplaintStatusRequest
): Promise<Complaint> {
  return api.patch<Complaint>(`/api/v1/complaints/field-officer/${complaintId}/status`, request);
}
