import { Bell, Bot, CalendarDays, FileText, Globe, Home, Plus, Search, UserRound } from "lucide-react";
import { AppShell, type NavItem } from "@/components/layout/AppShell";
import { AiAssistant } from "@/components/ai/AiAssistant";
import { useNotifications } from "@/hooks/useNotifications";

export function CitizenLayout() {
  const { unread } = useNotifications();

  const nav: NavItem[] = [
    { to: "/", label: "Home", icon: <Globe className="h-4 w-4" />, end: true },
    { to: "/dashboard", label: "Dashboard", icon: <Home className="h-4 w-4" />, end: true },
    { to: "/dashboard/report", label: "Report Issue", icon: <Plus className="h-4 w-4" /> },
    { to: "/dashboard/complaints", label: "My Complaints", icon: <FileText className="h-4 w-4" /> },
    { to: "/dashboard/my-events", label: "My Events", icon: <CalendarDays className="h-4 w-4" /> },
    { to: "/dashboard/notifications", label: "Notifications", icon: <Bell className="h-4 w-4" />, badge: unread },
    { to: "/dashboard/ai", label: "AI Assistant", icon: <Bot className="h-4 w-4" /> },
    { to: "/track", label: "Track Complaint", icon: <Search className="h-4 w-4" /> },
  ];

  const footer: NavItem[] = [
    { to: "/dashboard/profile", label: "Profile", icon: <UserRound className="h-4 w-4" /> },
  ];

  return (
    <>
      <AppShell navItems={nav} footerItems={footer} accent="citizen" />
      <AiAssistant mode="user" compact />
    </>
  );
}
