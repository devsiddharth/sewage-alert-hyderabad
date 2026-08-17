import { useCallback, useEffect, useMemo, useRef, useState, type DragEvent, type FormEvent } from "react";
import { Link } from "react-router-dom";
import {
  AlertTriangle,
  CheckCircle2,
  ChevronLeft,
  ImagePlus,
  LocateFixed,
  MapPin,
  Navigation,
  Send,
  X,
} from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { fileToCompressedFile, complaintCode } from "@/lib/utils";
import { ISSUE_CATEGORIES, SEVERITY_LEVELS, type Complaint } from "@/types";
import { cn } from "@/lib/cn";

interface GeoState {
  lat: string;
  lng: string;
  source: "gps" | "manual" | "none";
  locating: boolean;
}

// Mirrors the backend whitelist (JPG/PNG/WEBP) so a file the UI accepts can never be
// rejected by the server with a confusing error after submission.
const ALLOWED_IMAGE_TYPES = ["image/jpeg", "image/png", "image/webp"];

export function ReportIssue() {
  const { user } = useAuth();
  const { toast } = useToast();

  const [category, setCategory] = useState<string>(ISSUE_CATEGORIES[0]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [landmark, setLandmark] = useState("");
  const [severity, setSeverity] = useState<string>("MEDIUM");
  const [images, setImages] = useState<File[]>([]);
  const [geo, setGeo] = useState<GeoState>({ lat: "", lng: "", source: "none", locating: false });
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState<Complaint | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  // Object-URL previews for the selected files (no base64 in state). Revoked whenever the
  // selection changes or the form unmounts to avoid leaking blob URLs.
  const previewUrls = useMemo(() => images.map((f) => URL.createObjectURL(f)), [images]);
  useEffect(() => {
    const urls = previewUrls;
    return () => urls.forEach((u) => URL.revokeObjectURL(u));
  }, [previewUrls]);

  const detectGps = useCallback(() => {
    if (!navigator.geolocation) {
      setGeo((g) => ({ ...g, source: "manual" }));
      toast("info", "GPS not available", "Enter your location manually below.");
      return;
    }
    setGeo((g) => ({ ...g, locating: true }));
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setGeo({
          lat: pos.coords.latitude.toFixed(6),
          lng: pos.coords.longitude.toFixed(6),
          source: "gps",
          locating: false,
        });
        toast("success", "Location detected", "Your GPS coordinates were captured.");
      },
      () => {
        setGeo((g) => ({ ...g, locating: false, source: "manual" }));
        toast("error", "Couldn't get your location", "Enter it manually — it only takes a second.");
      },
      { enableHighAccuracy: true, timeout: 12000 }
    );
  }, [toast]);

  const addFiles = useCallback(
    async (files: FileList | null) => {
      if (!files) return;
      const incoming = Array.from(files).filter((f) => ALLOWED_IMAGE_TYPES.includes(f.type));
      if (incoming.length === 0) {
        toast("error", "Unsupported file", "Please choose an image (JPG, PNG, WEBP).");
        return;
      }
      const total = images.length + incoming.length;
      if (total > 4) {
        toast("error", "Too many photos", "You can attach up to 4 photos per complaint.");
        return;
      }
      try {
        const processed = await Promise.all(incoming.map((f) => fileToCompressedFile(f)));
        setImages((prev) => [...prev, ...processed]);
      } catch {
        toast("error", "Couldn't read image", "Please try a different photo.");
      }
    },
    [images.length, toast]
  );

  const onDrop = (e: DragEvent) => {
    e.preventDefault();
    void addFiles(e.dataTransfer.files);
  };

  const submit = async (e: FormEvent) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);

    const lat = Number(geo.lat);
    const lng = Number(geo.lng);
    const finalTitle = title.trim() || `${category} reported`;
    const parts = [
      description.trim() || "No additional details provided.",
      `Category: ${category}`,
      severity && `Severity: ${SEVERITY_LEVELS.find((s) => s.value === severity)?.label}`,
      landmark && `Landmark: ${landmark.trim()}`,
    ].filter(Boolean);

    try {
      // Multipart upload — image files go as binary parts; the backend uploads them to
      // object storage and persists only the returned URLs.
      const formData = new FormData();
      formData.append("title", finalTitle);
      formData.append("description", parts.join("\n"));
      formData.append("latitude", String(lat));
      formData.append("longitude", String(lng));
      images.forEach((file) => formData.append("images", file));
      const complaint = await api.postForm<Complaint>("/api/v1/complaints", formData);
      setSubmitted(complaint);
      toast("success", "Complaint submitted", `Your tracking ID is ${complaintCode(complaint.id)}.`);
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (err) {
      toast(
        "error",
        "Couldn't submit your report",
        err instanceof ApiError ? err.message : "Please try again in a moment."
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="mx-auto max-w-xl py-8 text-center animate-fade-in">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-success-soft">
          <CheckCircle2 className="h-10 w-10 text-emerald-600" />
        </div>
        <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Report submitted 🎉</h1>
        <p className="mt-3 text-muted">
          Thank you{user ? `, ${user.name.split(" ")[0]}` : ""}. Your complaint has been logged and
          the authorities have been notified.
        </p>
        <div className="mx-auto mt-8 max-w-sm rounded-2xl border border-line bg-white p-6 shadow-card">
          <p className="text-xs font-semibold uppercase tracking-wider text-muted">Your tracking ID</p>
          <p className="mt-1 font-mono text-3xl font-bold text-brand">{complaintCode(submitted.id)}</p>
          <p className="mt-2 text-sm text-muted">Status: Submitted — we&apos;ll update you at every step.</p>
        </div>
        <div className="mt-8 flex flex-col justify-center gap-3 sm:flex-row">
          <Link
            to={`/dashboard/complaints/${submitted.id}`}
            className="inline-flex h-12 items-center justify-center gap-2 rounded-xl bg-brand px-6 text-base font-semibold text-white transition-colors hover:bg-brand-light"
          >
            View complaint <ChevronLeft className="h-4 w-4 rotate-180" />
          </Link>
          <Button variant="outline" size="lg" onClick={() => {
            setSubmitted(null);
            setTitle("");
            setDescription("");
            setLandmark("");
            setImages([]);
            setGeo({ lat: "", lng: "", source: "none", locating: false });
            setCategory(ISSUE_CATEGORIES[0]);
          }}>
            Report another issue
          </Button>
        </div>
      </div>
    );
  }

  const locationReady = Boolean(geo.lat && geo.lng && Number.isFinite(Number(geo.lat)) && Number.isFinite(Number(geo.lng)));

  return (
    <div className="mx-auto max-w-3xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink">Report an issue</h1>
          <p className="mt-1 text-sm text-muted">Takes less than a minute. Everything is saved the moment you submit.</p>
        </div>
        <span className="hidden rounded-full bg-accent-soft px-3 py-1 text-xs font-semibold text-brand sm:block">
          Report details
        </span>
      </div>

      <form onSubmit={submit} className="mt-8 space-y-6">
        {/* Category */}
        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">What&apos;s the issue?</h2>
          <div className="mt-4 grid gap-2 sm:grid-cols-2">
            {ISSUE_CATEGORIES.map((c) => (
              <button
                key={c}
                type="button"
                onClick={() => setCategory(c)}
                className={cn(
                  "flex items-center gap-3 rounded-xl border px-4 py-3 text-left text-sm font-medium transition-all duration-200",
                  category === c
                    ? "border-brand bg-brand text-white shadow-sm"
                    : "border-line bg-white text-ink hover:border-accent"
                )}
                aria-pressed={category === c}
              >
                <MapPin className={cn("h-4 w-4", category === c ? "text-white" : "text-muted")} aria-hidden />
                {c}
              </button>
            ))}
          </div>
          <div className="mt-4">
            <Field label="Short title" hint="Pre-filled from the category — you can customise it.">
              <Input value={title} onChange={(e) => setTitle(e.target.value)} placeholder={`${category} — tell us where`} maxLength={120} />
            </Field>
          </div>
        </Card>

        {/* Photos */}
        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">Add photos</h2>
          <p className="mt-1 text-sm text-muted">Up to 4 photos help authorities prioritise. Optional but recommended.</p>
          <div
            onDrop={onDrop}
            onDragOver={(e) => e.preventDefault()}
            className="mt-4 grid gap-3 sm:grid-cols-2"
          >
            {images.map((img, i) => (
              <div key={`${img.name}-${img.size}-${i}`} className="group relative overflow-hidden rounded-xl border border-line">
                <img src={previewUrls[i]} alt={`Photo ${i + 1}`} className="h-40 w-full object-cover" />
                <button
                  type="button"
                  onClick={() => setImages((prev) => prev.filter((_, j) => j !== i))}
                  className="absolute right-2 top-2 rounded-lg bg-brand-dark/70 p-1.5 text-white opacity-0 transition-opacity duration-200 hover:bg-brand-dark group-hover:opacity-100"
                  aria-label={`Remove photo ${i + 1}`}
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            ))}
            <button
              type="button"
              onClick={() => fileRef.current?.click()}
              disabled={images.length >= 4}
              className="flex h-40 flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed border-line bg-canvas text-muted transition-all duration-200 hover:border-accent hover:text-brand disabled:opacity-50"
            >
              <ImagePlus className="h-6 w-6" />
              <span className="text-sm font-medium">Upload or drag & drop</span>
              <span className="text-xs">JPG · PNG · WEBP</span>
            </button>
            <input
              ref={fileRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              multiple
              className="hidden"
              onChange={(e) => {
                void addFiles(e.target.files);
                e.target.value = "";
              }}
            />
          </div>
        </Card>

        {/* Location */}
        <Card className="p-6">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold text-ink">Location</h2>
              <p className="mt-1 text-sm text-muted">We use this to send the right field team to the right place.</p>
            </div>
            <Button type="button" variant="outline" size="sm" onClick={detectGps} loading={geo.locating} icon={<LocateFixed className="h-4 w-4" />}>
              {geo.locating ? "Detecting…" : "Detect my location"}
            </Button>
          </div>

          {geo.source === "gps" && (
            <div className="mt-4 flex items-center gap-2 rounded-xl bg-success-soft px-4 py-3 text-sm font-medium text-emerald-800 animate-fade-in">
              <Navigation className="h-4 w-4" aria-hidden />
              GPS coordinates captured — you&apos;re all set.
            </div>
          )}
          {geo.source === "manual" && (
            <div className="mt-4 flex items-center gap-2 rounded-xl bg-amber-50 px-4 py-3 text-sm font-medium text-amber-800 animate-fade-in">
              <AlertTriangle className="h-4 w-4" aria-hidden />
              Enter coordinates manually below.
            </div>
          )}

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <Field label="Latitude" required>
              <Input
                inputMode="decimal"
                placeholder="e.g. 17.3850"
                value={geo.lat}
                onChange={(e) => setGeo((g) => ({ ...g, lat: e.target.value, source: "manual" }))}
              />
            </Field>
            <Field label="Longitude" required>
              <Input
                inputMode="decimal"
                placeholder="e.g. 78.4867"
                value={geo.lng}
                onChange={(e) => setGeo((g) => ({ ...g, lng: e.target.value, source: "manual" }))}
              />
            </Field>
          </div>
          <div className="mt-4">
            <Field label="Nearest landmark" hint="A nearby landmark helps the team find it faster.">
              <Input value={landmark} onChange={(e) => setLandmark(e.target.value)} placeholder="e.g. Near Sarathi Studios, Ameerpet" maxLength={100} />
            </Field>
          </div>
        </Card>

        {/* Severity + description */}
        <Card className="p-6">
          <h2 className="text-base font-semibold text-ink">Severity & details</h2>
          <div className="mt-4 grid gap-2 sm:grid-cols-3">
            {SEVERITY_LEVELS.map((s) => (
              <button
                key={s.value}
                type="button"
                onClick={() => setSeverity(s.value)}
                className={cn(
                  "rounded-xl border px-4 py-3 text-left transition-all duration-200",
                  severity === s.value
                    ? "border-brand bg-brand text-white shadow-sm"
                    : "border-line bg-white hover:border-accent"
                )}
                aria-pressed={severity === s.value}
              >
                <p className="text-sm font-semibold">{s.label}</p>
                <p className={cn("mt-0.5 text-xs", severity === s.value ? "text-white/75" : "text-muted")}>
                  {s.description}
                </p>
              </button>
            ))}
          </div>
          <div className="mt-4">
            <Field label="Describe the problem" required>
              <Textarea
                rows={4}
                maxLength={1000}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="How long has this been happening? Is it on the road, near a school, blocking a drain…?"
              />
            </Field>
            <p className="mt-1.5 text-right text-xs text-muted">{description.length}/1000</p>
          </div>
        </Card>

        <div className="flex flex-col-reverse items-stretch gap-3 sm:flex-row sm:items-center sm:justify-end">
          <Link
            to="/dashboard"
            className="inline-flex h-12 items-center justify-center rounded-xl border border-line bg-white px-6 text-sm font-semibold text-ink transition-colors hover:border-accent"
          >
            Cancel
          </Link>
          <Button
            type="submit"
            size="lg"
            loading={submitting}
            disabled={!locationReady}
            icon={<Send className="h-4 w-4" />}
          >
            {locationReady ? "Submit complaint" : "Add location to submit"}
          </Button>
        </div>
      </form>
    </div>
  );
}
