import { useLocation } from "react-router-dom";
import {
  BarChart3,
  Bot,
  Building2,
  CalendarCheck,
  CalendarDays,
  Droplets,
  FileText,
  GitBranch,
  Globe,
  HeartHandshake,
  Home,
  Map as MapIcon,
  Settings,
  Waves,
} from "lucide-react";
import { AppShell, type NavItem } from "@/components/layout/AppShell";
import { AiAssistant } from "@/components/ai/AiAssistant";

export function AdminLayout() {
  // The hotspot map page opts into full-bleed mode so it fills the entire
  // viewport next to the sidebar (no max-width container or outer gaps).
  const { pathname } = useLocation();
  const fullBleed = pathname === "/admin/hotspots";
  const nav: NavItem[] = [
    { to: "/", label: "Home", icon: <Globe className="h-4 w-4" />, end: true },
    { to: "/admin", label: "Dashboard", icon: <Home className="h-4 w-4" />, end: true },
    { to: "/admin/hotspots", label: "Hotspot map", icon: <MapIcon className="h-4 w-4" /> },
    { to: "/admin/complaints", label: "Complaints", icon: <FileText className="h-4 w-4" /> },
    { to: "/admin/analytics", label: "Analytics", icon: <BarChart3 className="h-4 w-4" /> },
    { to: "/admin/ai-assistant", label: "AI Assistant", icon: <Bot className="h-4 w-4" /> },
    { to: "/admin/community/events", label: "Events", icon: <CalendarDays className="h-4 w-4" /> },
    { to: "/admin/community/articles", label: "Articles", icon: <FileText className="h-4 w-4" /> },
    { to: "/admin/ngo-applications", label: "NGO Applications", icon: <CalendarCheck className="h-4 w-4" /> },
    { to: "/admin/community/ngos", label: "NGOs", icon: <HeartHandshake className="h-4 w-4" /> },
    { to: "/admin/community/plants", label: "Treatment plants", icon: <Droplets className="h-4 w-4" /> },
    { to: "/admin/community/pipelines", label: "Pipelines", icon: <GitBranch className="h-4 w-4" /> },
    { to: "/admin/community/lakes", label: "Lakes", icon: <Waves className="h-4 w-4" /> },
    { to: "/admin/settings", label: "Settings", icon: <Settings className="h-4 w-4" /> },
  ];

  const footer: NavItem[] = [
    { to: "/admin/profile", label: "My profile", icon: <Building2 className="h-4 w-4" /> },
  ];

  return (
    <>
      <AppShell
        navItems={nav}
        footerItems={footer}
        accent="admin"
        profilePath="/admin/profile"
        fullBleed={fullBleed}
      />
      <AiAssistant mode="admin" compact />
    </>
  );
}
