import { useState, type FormEvent } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowRight, Search } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Field";
import { ComplaintDetailView } from "@/components/complaints/ComplaintDetail";

export function TrackComplaint() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [query, setQuery] = useState(id ?? "");
  const [error, setError] = useState<string | null>(null);

  const resolvedId = id ? Number(id) : null;

  const submit = (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    const digits = query.replace(/\D/g, "");
    if (!digits) {
      setError("Enter your complaint ID — for example 1042.");
      return;
    }
    navigate(`/track/${digits}`);
  };

  return (
    <div className="container-page py-10 sm:py-12 lg:py-16">
      <div className="mx-auto max-w-2xl text-center">
        <p className="text-sm font-semibold uppercase tracking-wider text-accent">Track a complaint</p>
        <h1 className="mt-3 text-2xl font-bold tracking-tight text-ink sm:text-3xl lg:text-4xl">
          Where is my complaint?
        </h1>
        <p className="mt-3 text-base text-muted sm:text-lg">
          Enter the complaint ID you received when you filed your report.
        </p>
      </div>

      <form onSubmit={submit} className="mx-auto mt-6 max-w-xl sm:mt-8" noValidate>
        <div className="flex flex-col gap-3 sm:flex-row">
          <div className="relative flex-1">
            <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
            <Input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Complaint ID, e.g. 1042"
              className="h-12 pl-10 text-base"
              name="complaintId"
              aria-label="Complaint ID"
            />
          </div>
          <Button type="submit" size="lg" icon={<ArrowRight className="h-5 w-5" />}>
            Track
          </Button>
        </div>
        {error && <p className="mt-2 text-center text-sm font-medium text-red-600">{error}</p>}
        {!id && (
          <p className="mt-4 text-center text-sm text-muted">
            You&apos;ll find your ID in the confirmation message after reporting. It looks like{" "}
            <span className="font-mono font-semibold text-brand">#SA-1042</span>.
          </p>
        )}
      </form>

      <div className="mx-auto mt-10 max-w-5xl">
        {resolvedId ? (
          <ComplaintDetailView
            complaintId={resolvedId}
            onNotFound={() => {
              /* handled inside the view */
            }}
          />
        ) : (
          <div className="rounded-2xl border border-dashed border-line bg-white p-14 text-center text-muted">
            <p className="text-sm">Enter a complaint ID above to see its live status timeline.</p>
          </div>
        )}
      </div>
    </div>
  );
}
