import type { ApiResponse } from "@/types";

// ---------------------------------------------------------------------------
// API client
//
// All traffic is routed through the Spring Cloud Gateway:
//   - dev  : same-origin, proxied by Vite -> http://localhost:8080
//   - prod : set VITE_API_URL to the deployed gateway (e.g. https://api.example.com)
//
// Authenticated calls send both:
//   Authorization: Bearer <jwt>          (validated by auth-service)
//   X-Auth-User-Id: <id>                 (consumed by complaint/community/user services)
// ---------------------------------------------------------------------------

export const API_BASE: string = import.meta.env.VITE_API_URL ?? "";

const TOKEN_KEY = "sa_token";
const USER_KEY = "sa_user";

export interface StoredUser {
  id: number;
  name: string;
  email: string;
  role: string;
}

export const session = {
  getToken: (): string | null => localStorage.getItem(TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  getUser: (): StoredUser | null => {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as StoredUser) : null;
    } catch {
      return null;
    }
  },
  setUser: (user: StoredUser) => localStorage.setItem(USER_KEY, JSON.stringify(user)),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

export class ApiError extends Error {
  status: number;
  details: unknown;
  constructor(message: string, status: number, details?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(init.headers as Record<string, string> | undefined),
  };

  const token = session.getToken();
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const user = session.getUser();
  if (user?.id != null) headers["X-Auth-User-Id"] = String(user.id);

  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, { ...init, headers });
  } catch {
    throw new ApiError(
      "Unable to reach the server. Make sure the API Gateway is running on port 8080.",
      0
    );
  }

  let body: ApiResponse<T> | null = null;
  try {
    body = (await res.json()) as ApiResponse<T>;
  } catch {
    // no JSON body
  }

  if (!res.ok || (body && body.success === false)) {
    const message =
      body?.message ??
      (res.status === 401
        ? "Your session has expired. Please sign in again."
        : `Request failed (${res.status})`);
    if (res.status === 401) {
      // Token invalid/expired — clear session and notify the AuthProvider so the
      // UI immediately drops the stale authenticated state and redirects to login.
      session.clear();
      window.dispatchEvent(new Event("sa:auth-expired"));
    }
    throw new ApiError(message, res.status, body?.error);
  }

  return body?.data as T;
}

export const api = {
  get: <T>(path: string) => request<T>(path, { method: "GET" }),
  post: <T>(path: string, data?: unknown) =>
    request<T>(path, { method: "POST", body: JSON.stringify(data ?? {}) }),
  put: <T>(path: string, data?: unknown) =>
    request<T>(path, { method: "PUT", body: JSON.stringify(data ?? {}) }),
  patch: <T>(path: string, data?: unknown) =>
    request<T>(path, { method: "PATCH", body: JSON.stringify(data ?? {}) }),
  del: <T>(path: string) => request<T>(path, { method: "DELETE" }),
};
