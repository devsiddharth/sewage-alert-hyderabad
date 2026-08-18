import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import { Logo } from "@/components/ui/Logo";

export function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="flex flex-col justify-center px-5 py-10 sm:px-12 sm:py-12">
        <div className="mx-auto w-full max-w-md">
          <Link to="/" className="inline-flex" aria-label="Back to home">
            <Logo />
          </Link>
          <div className="mt-10">{children}</div>
        </div>
      </div>
      <div className="relative hidden overflow-hidden bg-brand lg:block">
        <div className="absolute inset-0 bg-brand-dark" aria-hidden />
        <div
          className="absolute -right-32 -top-32 h-96 w-96 rounded-full bg-brand-light/40 blur-3xl"
          aria-hidden
        />
        <div
          className="absolute -bottom-40 -left-24 h-96 w-96 rounded-full bg-accent/20 blur-3xl"
          aria-hidden
        />
        <div className="relative flex h-full flex-col justify-between p-12">
          <div className="flex items-center gap-2 text-white/80">
            <span className="h-2 w-2 rounded-full bg-success" aria-hidden />
            <p className="text-sm font-medium">Serving every neighbourhood of Hyderabad</p>
          </div>
          <blockquote className="max-w-lg">
            <p className="text-3xl font-bold leading-snug text-white">
              “A cleaner city isn&apos;t built by authorities alone — it&apos;s built one report at a
              time.”
            </p>
            <footer className="mt-6 flex items-center gap-3">
              <span className="flex h-10 w-10 items-center justify-center rounded-full bg-accent text-sm font-bold text-white">
                GH
              </span>
              <div>
                <p className="text-sm font-semibold text-white">Greater Hyderabad Municipal Corporation</p>
                <p className="text-xs text-white/60">Sanitation & Public Health Division</p>
              </div>
            </footer>
          </blockquote>
          <ul className="flex flex-wrap gap-x-6 gap-y-2 text-sm text-white/60">
            <li>Photo reporting</li>
            <li>GPS pinned location</li>
            <li>Live status tracking</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
