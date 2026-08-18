import type { ReactNode } from "react";
import { Card } from "@/components/ui/Card";
import { cn } from "@/lib/cn";

export function StatCard({
  label,
  value,
  icon,
  tone = "brand",
  hint,
  delay = 0,
}: {
  label: string;
  value: string | number;
  icon: ReactNode;
  tone?: "brand" | "green" | "amber" | "red" | "blue";
  hint?: string;
  delay?: number;
}) {
  const tones: Record<string, string> = {
    brand: "bg-brand/8 text-brand",
    green: "bg-emerald-50 text-emerald-600",
    amber: "bg-amber-50 text-amber-600",
    red: "bg-red-50 text-red-600",
    blue: "bg-blue-50 text-blue-600",
  };

  return (
    <Card
      className="animate-fade-in p-5 transition-shadow duration-200 hover:shadow-lift"
      style={{ animationDelay: `${delay}ms` }}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-muted">{label}</p>
          <p className="mt-2 text-2xl font-bold tracking-tight text-ink sm:text-3xl">{value}</p>
          {hint && <p className="mt-1 text-xs text-muted">{hint}</p>}
        </div>
        <div className={cn("flex h-11 w-11 items-center justify-center rounded-2xl", tones[tone])}>
          {icon}
        </div>
      </div>
    </Card>
  );
}
