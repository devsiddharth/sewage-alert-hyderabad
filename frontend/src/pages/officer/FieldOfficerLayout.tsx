import { Bell, FileText, Globe, Home, UserRound } from "lucide-react";
import { AppShell, type NavItem } from "@/components/layout/AppShell";
import { useNotifications } from "@/hooks/useNotifications";

export function FieldOfficerLayout() {
  const { unread } = useNotifications();

  const nav: NavItem[] = [
    { to: "/", label: "Home", icon: <Globe className="h-4 w-4" />, end: true },
    { to: "/officer", label: "Dashboard", icon: <Home className="h-4 w-4" />, end: true },
    { to: "/officer/complaints", label: "Assigned complaints", icon: <FileText className="h-4 w-4" /> },
    { to: "/officer/notifications", label: "Notifications", icon: <Bell className="h-4 w-4" />, badge: unread },
  ];

  const footer: NavItem[] = [
    { to: "/officer/profile", label: "My profile", icon: <UserRound className="h-4 w-4" /> },
  ];

  return <AppShell navItems={nav} footerItems={footer} accent="citizen" profilePath="/officer/profile" />;
}
