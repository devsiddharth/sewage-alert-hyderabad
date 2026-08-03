import { PRIORITY_META, STATUS_META } from "@/types";
import { toTitleCase } from "@/lib/utils";
import { cn } from "@/lib/cn";

const toneClasses: Record<string, string> = {
  slate: "bg-slate-100 text-slate-700 ring-slate-200",
  blue: "bg-blue-50 text-blue-700 ring-blue-200",
  green: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  red: "bg-red-50 text-red-700 ring-red-200",
  amber: "bg-amber-50 text-amber-700 ring-amber-200",
};

export function Badge({
  tone = "slate",
  children,
  className,
}: {
  tone?: keyof typeof toneClasses;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ring-1 ring-inset",
        toneClasses[tone],
        className
      )}
    >
      {children}
    </span>
  );
}

export function StatusBadge({ status, className }: { status: string; className?: string }) {
  const meta = STATUS_META[status as keyof typeof STATUS_META];
  if (!meta) return <Badge className={className}>{toTitleCase(status)}</Badge>;
  return (
    <Badge tone={meta.tone} className={className}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden />
      {meta.label}
    </Badge>
  );
}

export function PriorityBadge({ priority, className }: { priority: string | null; className?: string }) {
  if (!priority) return <Badge className={className}>Unassigned</Badge>;
  const meta = PRIORITY_META[priority as keyof typeof PRIORITY_META];
  if (!meta) return <Badge className={className}>{toTitleCase(priority)}</Badge>;
  return (
    <Badge tone={meta.tone} className={className}>
      {meta.label}
    </Badge>
  );
}
