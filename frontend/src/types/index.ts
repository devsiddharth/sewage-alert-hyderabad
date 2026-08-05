// ---------------------------------------------------------------------------
// API types — mirror the backend DTOs exactly (see docs/05 - API Documentation)
// All backend responses are wrapped in ApiResponse<T> = { success, message, data, error }
// ---------------------------------------------------------------------------

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error: unknown;
}

export type Role = "CITIZEN" | "AUTHORITY" | "ADMIN";

export interface AuthResponse {
  token: string;
  type: string; // "Bearer"
  id: number;
  name: string;
  email: string;
  role: Role;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  phone?: number | null;
  role?: Role;
}

export interface UserProfile {
  id: number;
  authUserId: number;
  name: string;
  phone: number | null;
  profilePictureUrl: string | null;
  address: string | null;
  preferences: string | null;
  createdAt: string;
  updatedAt: string;
}

export type ComplaintStatus = "PENDING" | "IN_PROGRESS" | "RESOLVED" | "REJECTED";
export type ComplaintPriority = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export interface ComplaintHistoryEntry {
  status: ComplaintStatus;
  remarks: string | null;
  updatedBy: number | null;
  updatedAt: string;
}

export interface Complaint {
  id: number;
  title: string;
  description: string;
  latitude: number;
  longitude: number;
  status: ComplaintStatus;
  priority: ComplaintPriority | null;
  createdBy: number;
  assignedTo: number | null;
  resolutionRemarks: string | null;
  imageUrls: string[];
  history: ComplaintHistoryEntry[];
  createdAt: string;
  updatedAt: string;
}

export interface ComplaintRequest {
  title: string;
  description: string;
  latitude: number;
  longitude: number;
  imageUrls?: string[];
}

export interface ComplaintStatusRequest {
  status: ComplaintStatus;
  priority?: ComplaintPriority | null;
  remarks?: string | null;
}

export interface Event {
  id: number;
  title: string;
  description: string;
  location: string;
  eventDate: string; // yyyy-MM-dd
  organizerName: string;
  organizerId: number;
  capacity: number | null;
  registeredCount: number;
  createdAt: string;
}

export interface EventRequest {
  title: string;
  description: string;
  location: string;
  eventDate: string;
  organizerName: string;
  capacity?: number | null;
}

export interface Article {
  id: number;
  title: string;
  content: string;
  category: string;
  authorName: string;
  publishedAt: string;
}

export interface ArticleRequest {
  title: string;
  content: string;
  category: string;
  authorName: string;
}

export interface Ngo {
  id: number;
  name: string;
  contactPerson: string;
  email: string;
  phone: string;
  website: string;
  description: string;
  createdAt: string;
}

export interface NgoRequest {
  name: string;
  contactPerson: string;
  email: string;
  phone: string;
  website: string;
  description: string;
}

export interface Pipeline {
  id: number;
  locality: string;
  installationYear: number | null;
  designedCapacity: number | null;
  maintenanceDate: string | null;
  operationalStatus: string;
  notes: string | null;
  createdAt: string;
}

export interface PipelineRequest {
  locality: string;
  installationYear?: number | null;
  designedCapacity?: number | null;
  maintenanceDate?: string | null;
  operationalStatus: string;
  notes?: string | null;
}

export interface TreatmentPlant {
  id: number;
  name: string;
  capacityMld: number | null;
  location: string;
  treatmentMethod: string;
  waterReuseInfo: string | null;
  description: string;
  createdAt: string;
}

export interface TreatmentPlantRequest {
  name: string;
  capacityMld?: number | null;
  location: string;
  treatmentMethod: string;
  waterReuseInfo?: string | null;
  description: string;
}

export interface Lake {
  id: number;
  name: string;
  location: string;
  restorationStatus: string | null;
  waterSource: string | null;
  connectedStpId: number | null;
  environmentalUpdates: string | null;
  description: string;
  createdAt: string;
}

export interface LakeRequest {
  name: string;
  location: string;
  restorationStatus?: string | null;
  waterSource?: string | null;
  connectedStpId?: number | null;
  environmentalUpdates?: string | null;
  description: string;
}

// ---------------------------------------------------------------------------
// Issue categories shown in the report form. The backend Complaint entity does
// not carry a category column, so the selected category is embedded into the
// complaint title ("Category — custom title") and the severity/landmark are
// appended to the description. This keeps the UI rich while staying 1:1 with
// the actual ComplaintRequest DTO.
// ---------------------------------------------------------------------------
export const ISSUE_CATEGORIES = [
  "Sewage Overflow",
  "Blocked Drain",
  "Open Manhole",
  "Sewage Leakage",
  "Bad Odour",
  "Other",
] as const;

export const SEVERITY_LEVELS = [
  { value: "LOW", label: "Low", description: "Minor issue, no immediate risk" },
  { value: "MEDIUM", label: "Medium", description: "Needs attention soon" },
  { value: "HIGH", label: "High", description: "Affects public health or safety" },
] as const;

export const STATUS_META: Record<
  ComplaintStatus,
  { label: string; step: number; tone: "slate" | "blue" | "green" | "red" }
> = {
  PENDING: { label: "Submitted", step: 1, tone: "slate" },
  IN_PROGRESS: { label: "In Progress", step: 2, tone: "blue" },
  RESOLVED: { label: "Resolved", step: 3, tone: "green" },
  REJECTED: { label: "Rejected", step: 3, tone: "red" },
};

export const PRIORITY_META: Record<ComplaintPriority, { label: string; tone: "slate" | "amber" | "red" }> = {
  LOW: { label: "Low", tone: "slate" },
  MEDIUM: { label: "Medium", tone: "amber" },
  HIGH: { label: "High", tone: "red" },
  CRITICAL: { label: "Critical", tone: "red" },
};

// ---------------------------------------------------------------------------
// Complaint hotspot heatmap (admin)
// ---------------------------------------------------------------------------

export type DateRangePreset = "ALL" | "TODAY" | "LAST_7_DAYS" | "LAST_MONTH" | "CUSTOM";

export interface HeatmapFilters {
  /** Empty array = all statuses. */
  statuses: ComplaintStatus[];
  /** Empty array = all priorities. */
  priorities: ComplaintPriority[];
  preset: DateRangePreset;
  /** yyyy-MM-dd, only used when preset === "CUSTOM". */
  customFrom: string;
  /** yyyy-MM-dd, only used when preset === "CUSTOM". */
  customTo: string;
}

export const DEFAULT_HEATMAP_FILTERS: HeatmapFilters = {
  statuses: [],
  priorities: [],
  preset: "ALL",
  customFrom: "",
  customTo: "",
};

export interface HighestArea {
  key: string;
  count: number;
  lat: number;
  lng: number;
}

export interface ComplaintAnalytics {
  total: number;
  pending: number;
  inProgress: number;
  resolved: number;
  rejected: number;
  avgResolutionDays: number | null;
  highestArea: HighestArea | null;
}
