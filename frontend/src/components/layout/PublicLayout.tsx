import { useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { LogIn, Menu, Plus, X } from "lucide-react";
import { Logo } from "@/components/ui/Logo";
import { Button } from "@/components/ui/Button";
import { useAuth } from "@/lib/auth";
import { cn } from "@/lib/cn";

const links = [
  { to: "/", label: "Home", end: true },
  { to: "/track", label: "Track Complaint" },
  { to: "/events", label: "Events" },
  { to: "/articles", label: "Articles" },
  { to: "/ngos", label: "NGOs" },
  { to: "/lakes", label: "Lakes" },
  { to: "/infrastructure", label: "Infrastructure" },
];

export function PublicLayout() {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  return (
    <div className="flex min-h-screen flex-col bg-canvas">
      <header className="sticky top-0 z-50 border-b border-line/80 bg-white/85 backdrop-blur">
        <div className="container-page flex h-16 items-center justify-between gap-4">
          <Link to="/" aria-label="SewageAlert Hyderabad home">
            <Logo />
          </Link>

          <nav className="hidden items-center gap-1 md:flex" aria-label="Main">
            {links.map((l) => (
              <NavLink
                key={l.to}
                to={l.to}
                end={l.end}
                className="rounded-lg px-3 py-2 text-sm font-medium text-muted transition-colors duration-200 hover:bg-canvas hover:text-ink"
              >
                {l.label}
              </NavLink>
            ))}
          </nav>

          <div className="hidden items-center gap-2 md:flex">
            {isAuthenticated ? (
              <>
                <Link
                  to={user?.role === "ADMIN" || user?.role === "AUTHORITY" ? "/admin" : "/dashboard"}
                  className="rounded-xl px-4 py-2 text-sm font-semibold text-ink transition-colors hover:bg-canvas"
                >
                  Dashboard
                </Link>
                <Button size="sm" icon={<Plus className="h-4 w-4" />} onClick={() => navigate(isAuthenticated ? "/dashboard/report" : "/login")}>
                  Report Issue
                </Button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="inline-flex items-center gap-1.5 rounded-xl px-4 py-2 text-sm font-semibold text-ink transition-colors hover:bg-canvas"
                >
                  <LogIn className="h-4 w-4" aria-hidden /> Login
                </Link>
                <Button size="sm" onClick={() => navigate("/register")}>
                  Get Started
                </Button>
              </>
            )}
          </div>

          <button
            className="touch-target flex items-center justify-center rounded-lg p-2 text-ink transition-colors hover:bg-canvas md:hidden"
            onClick={() => setOpen((v) => !v)}
            aria-label={open ? "Close menu" : "Open menu"}
            aria-expanded={open}
          >
            {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>

        {open && (
          <div className="border-t border-line bg-white px-4 pb-4 pt-2 md:hidden animate-fade-in">
            <nav className="flex flex-col gap-1" aria-label="Mobile">
              {links.map((l) => (
                <NavLink
                  key={l.to}
                  to={l.to}
                  end={l.end}
                  onClick={() => setOpen(false)}
                  className="rounded-lg px-3 py-2.5 text-sm font-medium text-ink transition-colors hover:bg-canvas"
                >
                  {l.label}
                </NavLink>
              ))}
              <div className="mt-2 flex flex-col gap-2">
                {isAuthenticated ? (
                  <>
                    <Button
                      fullWidth
                      variant="outline"
                      onClick={() => navigate(user?.role === "ADMIN" || user?.role === "AUTHORITY" ? "/admin" : "/dashboard")}
                    >
                      Dashboard
                    </Button>
                    <Button
                      fullWidth
                      icon={<Plus className="h-4 w-4" />}
                      onClick={() => navigate("/dashboard/report")}
                    >
                      Report Issue
                    </Button>
                  </>
                ) : (
                  <>
                    <Button fullWidth variant="outline" onClick={() => navigate("/login")}>
                      Login
                    </Button>
                    <Button fullWidth onClick={() => navigate("/register")}>
                      Get Started
                    </Button>
                  </>
                )}
              </div>
            </nav>
          </div>
        )}
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <Footer />
    </div>
  );
}

function Footer() {
  return (
    <footer className="border-t border-line bg-white">
      <div className="container-page grid gap-8 py-10 sm:grid-cols-2 sm:gap-10 sm:py-12 lg:grid-cols-4">
        <div>
          <Logo />
          <p className="mt-4 max-w-xs text-sm leading-relaxed text-muted">
            A citizen-first platform by the Government of Telangana to report and resolve sewage
            issues across Hyderabad — faster, together.
          </p>
        </div>
        <div>
          <h3 className="text-sm font-semibold text-ink">Platform</h3>
          <ul className="mt-4 space-y-2.5 text-sm text-muted">
            <li><Link className="transition-colors hover:text-brand" to="/track">Track a complaint</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/register">Create an account</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/#how-it-works">How it works</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/#faq">FAQ</Link></li>
          </ul>
        </div>
        <div>
          <h3 className="text-sm font-semibold text-ink">Community</h3>
          <ul className="mt-4 space-y-2.5 text-sm text-muted">
            <li><Link className="transition-colors hover:text-brand" to="/events">Events</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/articles">Articles</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/ngos">NGOs</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/lakes">Lakes</Link></li>
            <li><Link className="transition-colors hover:text-brand" to="/infrastructure">Infrastructure</Link></li>
          </ul>
        </div>
        <div>
          <h3 className="text-sm font-semibold text-ink">Contact</h3>
          <ul className="mt-4 space-y-2.5 text-sm text-muted">
            <li>Helpline: 040-2345 6789</li>
            <li>Email: support@sewagealert.telangana.gov.in</li>
            <li>Mon–Sat, 9:00 AM – 6:00 PM</li>
          </ul>
        </div>
        <div>
          <h3 className="text-sm font-semibold text-ink">Emergency</h3>
          <p className="mt-4 text-sm leading-relaxed text-muted">
            For urgent sewage overflow, manhole, or health emergencies, call GHMC at{" "}
            <a href="tel:04021111111" className="font-semibold text-brand hover:underline">
              040-2111 1111
            </a>
            .
          </p>
        </div>
      </div>
      <div className="border-t border-line">
        <div className="container-page flex flex-col items-center justify-between gap-2 py-4 text-xs text-muted sm:flex-row sm:py-5">
          <p>© {new Date().getFullYear()} SewageAlert Hyderabad · Government of Telangana initiative</p>
          <p className={cn("inline-flex items-center gap-1.5")}>
            <span className="h-1.5 w-1.5 rounded-full bg-success" aria-hidden />
            All systems operational
          </p>
        </div>
      </div>
    </footer>
  );
}
