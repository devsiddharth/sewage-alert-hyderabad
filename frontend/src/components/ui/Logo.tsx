import { cn } from "@/lib/cn";

export function LogoMark({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={cn("h-8 w-8", className)} aria-hidden>
      <rect width="32" height="32" rx="8" fill="#0A2463" />
      <path d="M16 6c4 5.5 7 9.2 7 12.5a7 7 0 1 1-14 0C9 15.2 12 11.5 16 6z" fill="#7692FF" />
      <path
        d="M16 19a2.5 2.5 0 0 0-2.5 2.5 2.5 2.5 0 0 0 2.5 2.5 2.5 2.5 0 0 0 2.5-2.5A2.5 2.5 0 0 0 16 19z"
        fill="#55D6BE"
      />
    </svg>
  );
}

export function Logo({ dark = false, className }: { dark?: boolean; className?: string }) {
  return (
    <span className={cn("inline-flex items-center gap-2.5", className)}>
      <LogoMark />
      <span className="flex flex-col leading-none">
        <span className={cn("text-[15px] font-bold tracking-tight", dark ? "text-white" : "text-brand")}>
          SewageAlert
        </span>
        <span className={cn("text-[11px] font-medium tracking-wide", dark ? "text-white/70" : "text-muted")}>
          HYDERABAD
        </span>
      </span>
    </span>
  );
}
