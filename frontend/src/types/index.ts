// ---------------------------------------------------------------------------
// API types — mirror the backend DTOs exactly (see docs/05 - API Documentation)
// All backend responses are wrapped in ApiResponse<T> = { success, message, data, error }
// ---------------------------------------------------------------------------

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error: unknown;
  /** Application-specific error code (e.g. EMAIL_NOT_VERIFIED) — only set on failures. */
  code?: string;
}

/** Generic pagination envelope returned by the Notification Service. */
export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

// Notification types — mirror the Notification Service NotificationType enum exactly.
export type NotificationType =
  | "COMPLAINT_CREATED"
  | "COMPLAINT_ASSIGNED"
  | "COMPLAINT_STATUS_UPDATED"
  | "COMPLAINT_RESOLVED"
  | "COMPLAINT_REJECTED"
  | "COMPLAINT_REOPENED"
  | "USER_REGISTERED"
  | "EMAIL_VERIFICATION_REQUESTED"
  | "EMAIL_VERIFIED"
  | "COMMUNITY_EVENT"
  | "ARTICLE"
  | "NGO_APPLICATION_SUBMITTED"
  | "NGO_APPLICATION_APPROVED"
  | "NGO_APPLICATION_REJECTED"
  | "NGO_EVENT_SUBMITTED"
  | "NGO_EVENT_APPROVED"
  | "NGO_EVENT_REJECTED"
  | "NGO_EVENT_USER_REGISTERED"
  | "NGO_EVENT_CANCELLED"
  | "SYSTEM"
  | "ADMIN";

/** In-app notification served by GET /api/v1/notifications (NotificationResponse DTO). */
export interface AppNotification {
  id: number;
  userId: number;
  title: string;
  message: string;
  notificationType: NotificationType;
  referenceId: number | null;
  referenceType: string | null;
  read: boolean;
  readAt: string | null;
  metadata: Record<string, unknown> | null;
  createdAt: string;
  updatedAt: string;
}

export type Role = "CITIZEN" | "AUTHORITY" | "ADMIN" | "FIELD_OFFICER" | "NGO_REPRESENTATIVE";

export interface AuthResponse {
  /** JWT — absent on register (accounts must verify their email before logging in). */
  token: string | null;
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

/** Body for POST /api/v1/auth/verify-code — inline 6-digit code verification during registration. */
export interface VerifyCodeRequest {
  email: string;
  code: string;
}

/** Field officer user (auth-service) — safe projection used by the assignment UI. */
export interface FieldOfficer {
  id: number;
  name: string;
  email: string;
}

/** Body for PUT /api/v1/complaints/admin/{id}/assign */
export interface AssignComplaintRequest {
  fieldOfficerId: number;
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
  /** Object-storage URL of the mandatory resolution-proof photo (set when RESOLVED). */
  resolutionProofImageUrl: string | null;
  imageUrls: string[];
  history: ComplaintHistoryEntry[];
  createdAt: string;
  updatedAt: string;
}

// Complaint creation is now a multipart/form-data upload: the form fields below plus
// binary image files under the "images" part (max 4). The backend uploads the files to
// object storage and persists only the returned URLs. This type models the form fields.
export interface ComplaintRequest {
  title: string;
  description: string;
  latitude: number;
  longitude: number;
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
// External API payloads (community-service) — returned by the /latest, /search
// and /external endpoints. These are sanitized DTOs from GNews / Google Places
// / OpenStreetMap Overpass / Telangana ArcGIS.
// ---------------------------------------------------------------------------

/** GET /api/v1/articles/latest (GNews) */
export interface ArticleFeedItem {
  title: string;
  description: string;
  imageUrl: string;
  articleUrl: string;
  source: string;
  author: string;
  publishedAt: string;
}

/** GET /api/v1/ngos/search (Google Places) */
export interface NgoSearchResult {
  name: string;
  address: string;
  phone: string;
  rating: number | null;
  website: string;
  latitude: number | null;
  longitude: number | null;
}

/** GET /api/v1/lakes/external (OpenStreetMap Overpass) */
export interface LakeGeoData {
  name: string;
  latitude: number;
  longitude: number;
  geometry: { lat: number; lon: number }[];
  address: {
    street: string | null;
    city: string | null;
    postcode: string | null;
    state: string | null;
    formatted: string | null;
  } | null;
}

/** GET /api/v1/treatment-plants/external (Telangana ArcGIS open data) */
export interface StpLocationData {
  name: string;
  latitude: number | null;
  longitude: number | null;
  yearOfCommissioning: number | null;
  operationalStatus: string;
  installedCapacityMld: number | null;
  utilizationCapacityMld: number | null;
  technology: string;
  frequencyOfMonitoring: string;
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
// NGO v2.0.0 types
// ---------------------------------------------------------------------------

export type NgoApplicationStatus = "PENDING" | "UNDER_REVIEW" | "APPROVED" | "REJECTED" | "SUSPENDED";

export interface NgoOrganization {
  id: number;
  representativeUserId: number;
  organizationName: string;
  officialEmail: string;
  officialPhone: string;
  registrationNumber: string;
  registrationDetails: string;
  website: string;
  address: string;
  operatingAreas: string;
  mission: string;
  areasOfFocus: string;
  communitiesServed: string;
  contactPersonName: string;
  contactPersonEmail: string;
  contactPersonPhone: string;
  supportingDocumentUrl: string | null;
  logoUrl: string | null;
  status: NgoApplicationStatus;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export type EventApprovalStatus = "PENDING_APPROVAL" | "APPROVED" | "REJECTED" | "PUBLISHED" | "CANCELLED";

export interface NgoEvent {
  id: number;
  title: string;
  description: string;
  location: string;
  eventDate: string;
  endDate: string | null;
  eventTime: string | null;
  capacity: number | null;
  category: string | null;
  images: string | null;
  ngoOrganizationId: number;
  ngoOrganizationName: string;
  approvalStatus: EventApprovalStatus;
  rejectionReason: string | null;
  registeredCount: number;
  isRegisteredByCurrentUser: boolean;
  createdAt: string;
}

export type DriveStatus = "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface NgoDrive {
  id: number;
  title: string;
  description: string;
  driveType: string | null;
  location: string;
  startDate: string;
  endDate: string | null;
  ngoOrganizationId: number;
  ngoOrganizationName: string;
  status: DriveStatus;
  images: string | null;
  totalTarget: number | null;
  currentParticipants: number;
  progressNotes: string | null;
  createdAt: string;
}

export interface NgoAchievement {
  id: number;
  title: string;
  description: string | null;
  date: string | null;
  evidence: string | null;
  images: string | null;
  ngoOrganizationId: number;
  createdAt: string;
}

export interface NgoProgress {
  id: number;
  ngoOrganizationId: number;
  complaintsAddressed: number;
  areasCovered: number;
  drivesConducted: number;
  eventsConducted: number;
  volunteersInvolved: number;
  peopleReached: number;
  updatedAt: string;
}

export interface NgoFund {
  id: number;
  ngoOrganizationId: number;
  source: string;
  amount: number;
  allocatedAmount: number | null;
  remainingAmount: number | null;
  projectName: string | null;
  description: string | null;
  receivedDate: string;
  supportingDocumentUrl: string | null;
  createdAt: string;
}

export interface NgoExpense {
  id: number;
  fundRecordId: number;
  ngoOrganizationId: number;
  category: string;
  amount: number;
  description: string | null;
  expenseDate: string;
  supportingDocumentUrl: string | null;
  createdAt: string;
}

export interface NgoDashboard {
  organization: NgoOrganization;
  progress: NgoProgress | null;
  totalEvents: number;
  pendingEvents: number;
  publishedEvents: number;
  totalDrives: number;
  totalAchievements: number;
  totalParticipants: number;
  totalFundsReceived: number | null;
  totalExpenses: number | null;
  remainingBalance: number | null;
}

export interface NgoParticipant {
  userId: number;
  name: string;
  email: string;
  registrationStatus: string;
  attendanceStatus: string | null;
}

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
