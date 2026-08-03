import { STATUS_META, type ComplaintHistoryEntry, type ComplaintStatus } from "@/types";
import { formatDateTime } from "@/lib/utils";
import { cn } from "@/lib/cn";

export function ComplaintTimeline({
  status,
  history,
  createdAt,
}: {
  status: ComplaintStatus;
  history: ComplaintHistoryEntry[];
  createdAt: string;
}) {
  // Build a clean chronological list of steps: submission + every status change.
  // The backend records an initial PENDING history entry on creation, so PENDING is
  // already represented by the submission step below — skip it from history.
  const steps: { key: string; status: ComplaintStatus; label: string; at: string; remarks?: string | null; isCurrent: boolean }[] = [
    { key: "created", status: "PENDING", label: STATUS_META.PENDING.label, at: createdAt, isCurrent: status === "PENDING" },
  ];

  const seen = new Set<string>(["PENDING"]);
  for (const h of [...history].sort((a, b) => new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime())) {
    if (seen.has(h.status)) continue;
    seen.add(h.status);
    steps.push({
      key: `${h.status}-${h.updatedAt}`,
      status: h.status,
      label: STATUS_META[h.status]?.label ?? h.status,
      at: h.updatedAt,
      remarks: h.remarks,
      isCurrent: h.status === status,
    });
  }

  // Normalize into milestone order for the visual timeline
  const ordered = steps.sort(
    (a, b) => new Date(a.at).getTime() - new Date(b.at).getTime()
  );

  const currentIndex = ordered.findIndex((s) => s.isCurrent);

  return (
    <ol className="relative space-y-0">
      {ordered.map((step, i) => {
        const done = i <= currentIndex || (status === "REJECTED" && i < ordered.length - 1);
        const rejected = step.status === "REJECTED";
        const tone = done ? (rejected ? "bg-red-500" : "bg-success") : "bg-line";
        return (
          <li key={step.key} className="relative flex gap-4 pb-6 last:pb-0">
            {i < ordered.length - 1 && (
              <span
                className={cn(
                  "absolute left-[11px] top-6 h-[calc(100%-24px)] w-0.5 rounded",
                  i < currentIndex ? "bg-success" : "bg-line"
                )}
                aria-hidden
              />
            )}
            <span
              className={cn(
                "relative z-10 mt-1 flex h-6 w-6 shrink-0 items-center justify-center rounded-full ring-4",
                tone,
                "ring-white"
              )}
              aria-hidden
            >
              {done && i < ordered.length - 1 && (
                <svg viewBox="0 0 12 12" className="h-3 w-3 text-white" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="m2.5 6.5 2.5 2.5 4.5-5" />
                </svg>
              )}
            </span>
            <div className="min-w-0 flex-1 pt-0.5">
              <div className="flex flex-wrap items-center justify-between gap-x-3 gap-y-1">
                <p className={cn("text-sm font-semibold", done ? "text-ink" : "text-muted")}>
                  {step.label}
                </p>
                <time className="text-xs text-muted" dateTime={step.at}>
                  {formatDateTime(step.at)}
                </time>
              </div>
              {step.remarks && <p className="mt-1 text-sm text-muted">{step.remarks}</p>}
            </div>
          </li>
        );
      })}
    </ol>
  );
}
