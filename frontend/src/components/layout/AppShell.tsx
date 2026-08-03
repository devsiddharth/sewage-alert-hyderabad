import { useState, type ReactNode } from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { ChevronDown, LogOut, Menu, X } from "lucide-react";
import { Logo } from "@/components/ui/Logo";
import { useAuth } from "@/lib/auth";
import { initials } from "@/lib/utils";
import { cn } from "@/lib/cn";

export interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  end?: boolean;
  badge?: number;
}

export function AppShell({
  navItems,
  footerItems,
  accent = "admin",
  profilePath = "/dashboard/profile",
}: {
  navItems: NavItem[];
  footerItems?: NavItem[];
  accent?: "citizen" | "admin";
  profilePath?: string;
}) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const sidebar = (
    <div className="flex h-full flex-col">
      <div className={cn("flex h-16 items-center px-5", accent === "admin" ? "bg-brand-dark" : "bg-brand")}>
        <Logo dark />
      </div>
      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-4" aria-label="Primary">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.end}
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors duration-200",
                isActive
                  ? "bg-white/10 text-white"
                  : "text-white/65 hover:bg-white/5 hover:text-white"
              )
            }
          >
            {item.icon}
            <span className="flex-1">{item.label}</span>
            {item.badge ? (
              <span className="rounded-full bg-accent px-2 py-0.5 text-[11px] font-semibold text-white">
                {item.badge}
              </span>
            ) : null}
          </NavLink>
        ))}
        {footerItems && footerItems.length > 0 && (
          <>
            <div className="my-3 border-t border-white/10" />
            {footerItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={() => setOpen(false)}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors duration-200",
                    isActive
                      ? "bg-white/10 text-white"
                      : "text-white/65 hover:bg-white/5 hover:text-white"
                  )
                }
              >
                {item.icon}
                <span className="flex-1">{item.label}</span>
              </NavLink>
            ))}
          </>
        )}
      </nav>
      <div className="border-t border-white/10 p-3">
        <div className="relative">
          <button
            onClick={() => setMenuOpen((v) => !v)}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition-colors hover:bg-white/5"
          >
            <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-accent text-sm font-semibold text-white">
              {initials(user?.name)}
            </span>
            <span className="min-w-0 flex-1">
              <span className="block truncate text-sm font-semibold text-white">{user?.name}</span>
              <span className="block text-xs text-white/55">{user?.role}</span>
            </span>
            <ChevronDown className="h-4 w-4 text-white/50" />
          </button>
          {menuOpen && (
            <div className="absolute bottom-full left-0 right-0 mb-2 overflow-hidden rounded-xl bg-brand-light shadow-lift animate-fade-in">
              <button
                onClick={() => {
                  setMenuOpen(false);
                  setOpen(false);
                  navigate(profilePath);
                }}
                className="block w-full px-4 py-2.5 text-left text-sm text-white/85 transition-colors hover:bg-white/10"
              >
                Profile settings
              </button>
              <button
                onClick={handleLogout}
                className="flex w-full items-center gap-2 px-4 py-2.5 text-left text-sm text-red-300 transition-colors hover:bg-white/10"
              >
                <LogOut className="h-4 w-4" /> Sign out
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-canvas">
      {/* Desktop sidebar */}
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-64 lg:block">
        <div className={cn("h-full", accent === "admin" ? "bg-brand-dark" : "bg-brand")}>{sidebar}</div>
      </aside>

      {/* Mobile drawer */}
      {open && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <div className="absolute inset-0 bg-brand-dark/50" onClick={() => setOpen(false)} aria-hidden />
          <div className="absolute inset-y-0 left-0 w-72 max-w-[85%] animate-fade-in">
            <div className={cn("h-full", accent === "admin" ? "bg-brand-dark" : "bg-brand")}>
              <button
                onClick={() => setOpen(false)}
                className="absolute right-3 top-4 rounded-lg p-1.5 text-white/70 hover:bg-white/10 hover:text-white"
                aria-label="Close menu"
              >
                <X className="h-5 w-5" />
              </button>
              {sidebar}
            </div>
          </div>
        </div>
      )}

      <div className="lg:pl-64">
        {/* Mobile top bar */}
        <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-line bg-white/90 px-4 backdrop-blur lg:hidden">
          <button
            onClick={() => setOpen(true)}
            className="rounded-lg p-2 text-ink transition-colors hover:bg-canvas"
            aria-label="Open menu"
          >
            <Menu className="h-5 w-5" />
          </button>
          <Logo />
          <div className="w-9" />
        </header>

        <main className="mx-auto w-full max-w-7xl px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
