import { Award, BarChart3, Bot, Calendar, HeartHandshake, Home, IndianRupee, Map, Truck, UserRound } from "lucide-react";
import { AppShell, type NavItem } from "@/components/layout/AppShell";
import { AiAssistant } from "@/components/ai/AiAssistant";

export function NgoLayout() {
  const nav: NavItem[] = [
    { to: "/ngo", label: "Overview", icon: <Home className="h-4 w-4" />, end: true },
    { to: "/ngo/profile", label: "Profile", icon: <HeartHandshake className="h-4 w-4" /> },
    { to: "/ngo/events", label: "Events", icon: <Calendar className="h-4 w-4" /> },
    { to: "/ngo/drives", label: "Drives", icon: <Truck className="h-4 w-4" /> },
    { to: "/ngo/achievements", label: "Achievements", icon: <Award className="h-4 w-4" /> },
    { to: "/ngo/progress", label: "Progress", icon: <BarChart3 className="h-4 w-4" /> },
    { to: "/ngo/funds", label: "Funds & Expenses", icon: <IndianRupee className="h-4 w-4" /> },
    { to: "/ngo/participants", label: "Participants", icon: <Map className="h-4 w-4" /> },
    { to: "/ngo/ai", label: "AI Assistant", icon: <Bot className="h-4 w-4" /> },
  ];

  const footer: NavItem[] = [
    { to: "/ngo/profile", label: "My Profile", icon: <UserRound className="h-4 w-4" /> },
  ];

  return (
    <>
      <AppShell navItems={nav} footerItems={footer} accent="admin" profilePath="/ngo/profile" />
      <AiAssistant mode="ngo" compact />
    </>
  );
}
