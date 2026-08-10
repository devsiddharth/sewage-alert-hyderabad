import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "@/lib/auth";
import { homePathFor } from "@/lib/utils";

export function RequireAuth({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  return <>{children}</>;
}

export function RequireAdmin({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isAdmin, user } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  if (!isAdmin) {
    return <Navigate to={homePathFor(user?.role)} replace />;
  }
  return <>{children}</>;
}

export function RequireFieldOfficer({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isFieldOfficer, user } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }
  if (!isFieldOfficer) {
    return <Navigate to={homePathFor(user?.role)} replace />;
  }
  return <>{children}</>;
}

export function RedirectIfAuthed({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, user } = useAuth();
  if (isAuthenticated) {
    return <Navigate to={homePathFor(user?.role)} replace />;
  }
  return <>{children}</>;
}
