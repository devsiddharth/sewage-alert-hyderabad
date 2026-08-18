import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/cn";

export function Pagination({
  page,
  pageCount,
  onChange,
  total,
}: {
  page: number;
  pageCount: number;
  onChange: (page: number) => void;
  total?: number;
}) {
  if (pageCount <= 1 && !total) return null;

  const pages = Array.from({ length: Math.max(pageCount, 1) }, (_, i) => i + 1);
  // On mobile, show fewer page numbers to prevent overflow
  const visible = pageCount > 7 ? [1, page, page + 1, page + 2, pageCount].filter((p, i, a) => p >= 1 && p <= pageCount && a.indexOf(p) === i) : pages;

  return (
    <nav className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 sm:gap-3 sm:px-5 sm:py-4" aria-label="Pagination">
      {total !== undefined && <p className="text-xs text-muted sm:text-sm">{total} item{total === 1 ? "" : "s"}</p>}
      <div className="flex items-center gap-1">
        <button
          onClick={() => onChange(page - 1)}
          disabled={page <= 1}
          className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-line bg-white text-muted transition-colors hover:text-ink disabled:opacity-40"
          aria-label="Previous page"
        >
          <ChevronLeft className="h-4 w-4" />
        </button>
        {visible.map((p) => (
          <button
            key={p}
            onClick={() => onChange(p)}
            className={cn(
              "h-9 min-w-9 rounded-lg px-2 text-sm font-medium transition-colors",
              p === page ? "bg-brand text-white" : "text-muted hover:bg-canvas hover:text-ink",
              // Hide middle pages with ellipsis gap on mobile
              Math.abs(p - page) > 1 && p !== 1 && p !== pageCount && "hidden sm:inline-flex"
            )}
            aria-current={p === page ? "page" : undefined}
          >
            {p}
          </button>
        ))}
        <button
          onClick={() => onChange(page + 1)}
          disabled={page >= pageCount}
          className="inline-flex h-9 w-9 items-center justify-center rounded-lg border border-line bg-white text-muted transition-colors hover:text-ink disabled:opacity-40"
          aria-label="Next page"
        >
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>
    </nav>
  );
}
