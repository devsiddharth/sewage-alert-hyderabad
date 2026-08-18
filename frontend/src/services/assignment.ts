import { api } from "@/lib/api";
import type { Complaint, ComplaintPriority, ComplaintStatusRequest, FieldOfficer } from "@/types";

// ---------------------------------------------------------------------------
// Complaint assignment / resolution workflow API client.
//
//   • Field officers  → GET  /api/v1/auth/admin/field-officers   (auth-service)
//   • Assign          → PUT  /api/v1/complaints/admin/{id}/assign
//   • Admin resolve   → POST /api/v1/complaints/admin/{id}/resolve  (multipart + proof photo)
//   • Officer list    → GET  /api/v1/complaints/field-officer
//   • Officer update  → PATCH /api/v1/complaints/field-officer/{id}/status
//   • Officer resolve → POST /api/v1/complaints/field-officer/{id}/resolve (multipart + proof photo)
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

/**
 * Resolve a complaint as an admin. The resolution-proof photo is MANDATORY —
 * the backend rejects the request without it, so the UI enforces it too.
 */
export function resolveComplaint(
  complaintId: number,
  payload: {
    remarks?: string | null;
    priority?: ComplaintPriority | null;
    proofImage: File;
  }
): Promise<Complaint> {
  const formData = new FormData();
  if (payload.remarks) formData.append("remarks", payload.remarks);
  if (payload.priority) formData.append("priority", payload.priority);
  formData.append("proofImage", payload.proofImage);
  return api.postForm<Complaint>(`/api/v1/complaints/admin/${complaintId}/resolve`, formData);
}

/**
 * Resolve an assigned complaint as a field officer (proof photo mandatory).
 */
export function resolveAssignedComplaint(
  complaintId: number,
  payload: {
    remarks?: string | null;
    priority?: ComplaintPriority | null;
    proofImage: File;
  }
): Promise<Complaint> {
  const formData = new FormData();
  if (payload.remarks) formData.append("remarks", payload.remarks);
  if (payload.priority) formData.append("priority", payload.priority);
  formData.append("proofImage", payload.proofImage);
  return api.postForm<Complaint>(`/api/v1/complaints/field-officer/${complaintId}/resolve`, formData);
}
