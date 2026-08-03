import {
  BarChart3,
  Building2,
  CalendarDays,
  Droplets,
  FileText,
  GitBranch,
  HeartHandshake,
  Home,
  Settings,
  Waves,
} from "lucide-react";
import { AppShell, type NavItem } from "@/components/layout/AppShell";

export function AdminLayout() {
  const nav: NavItem[] = [
    { to: "/admin", label: "Dashboard", icon: <Home className="h-4 w-4" />, end: true },
    { to: "/admin/complaints", label: "Complaints", icon: <FileText className="h-4 w-4" /> },
    { to: "/admin/analytics", label: "Analytics", icon: <BarChart3 className="h-4 w-4" /> },
    { to: "/admin/community/events", label: "Events", icon: <CalendarDays className="h-4 w-4" /> },
    { to: "/admin/community/articles", label: "Articles", icon: <FileText className="h-4 w-4" /> },
    { to: "/admin/community/ngos", label: "NGOs", icon: <HeartHandshake className="h-4 w-4" /> },
    { to: "/admin/community/plants", label: "Treatment plants", icon: <Droplets className="h-4 w-4" /> },
    { to: "/admin/community/pipelines", label: "Pipelines", icon: <GitBranch className="h-4 w-4" /> },
    { to: "/admin/community/lakes", label: "Lakes", icon: <Waves className="h-4 w-4" /> },
    { to: "/admin/settings", label: "Settings", icon: <Settings className="h-4 w-4" /> },
  ];

  const footer: NavItem[] = [
    { to: "/dashboard/profile", label: "My profile", icon: <Building2 className="h-4 w-4" /> },
  ];

  return <AppShell navItems={nav} footerItems={footer} accent="admin" profilePath="/dashboard/profile" />;
}
