import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { api, session } from "@/lib/api";
import type { AuthResponse, RegisterRequest } from "@/types";

interface AuthContextValue {
  user: AuthResponse | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isFieldOfficer: boolean;
  login: (email: string, password: string) => Promise<AuthResponse>;
  register: (data: RegisterRequest) => Promise<AuthResponse>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function toStored(user: AuthResponse) {
  return { id: user.id, name: user.name, email: user.email, role: user.role };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(() => {
    const stored = session.getUser();
    return stored
      ? ({ token: session.getToken() ?? "", type: "Bearer", ...stored } as AuthResponse)
      : null;
  });

  const applySession = useCallback((auth: AuthResponse) => {
    session.setToken(auth.token);
    session.setUser(toStored(auth));
    setUser(auth);
  }, []);

  // If any API call receives a 401, drop the in-memory session so route guards
  // redirect to the login page immediately.
  useEffect(() => {
    const onAuthExpired = () => setUser(null);
    window.addEventListener("sa:auth-expired", onAuthExpired);
    return () => window.removeEventListener("sa:auth-expired", onAuthExpired);
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const auth = await api.post<AuthResponse>("/api/v1/auth/login", { email, password });
      applySession(auth);
      return auth;
    },
    [applySession]
  );

  const register = useCallback(
    async (data: RegisterRequest) => {
      const auth = await api.post<AuthResponse>("/api/v1/auth/register", data);
      applySession(auth);
      return auth;
    },
    [applySession]
  );

  const logout = useCallback(() => {
    session.clear();
    setUser(null);
  }, []);

  const refreshProfile = useCallback(async () => {
    try {
      const auth = await api.get<AuthResponse>("/api/v1/auth/profile");
      applySession(auth);
    } catch {
      // keep the cached session if the profile refresh fails
    }
  }, [applySession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: Boolean(user?.token),
      isAdmin: user?.role === "ADMIN" || user?.role === "AUTHORITY",
      isFieldOfficer: user?.role === "FIELD_OFFICER",
      login,
      register,
      logout,
      refreshProfile,
    }),
    [user, login, register, logout, refreshProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
