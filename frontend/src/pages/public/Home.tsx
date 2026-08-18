import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  ArrowRight,
  Camera,
  CheckCircle2,
  ChevronDown,
  Clock,
  FileText,
  LocateFixed,
  Mail,
  MapPin,
  Phone,
  Search,
  ShieldCheck,
  Users,
  Zap,
} from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { StatusBadge } from "@/components/ui/Badge";
import { ComplaintCard } from "@/components/complaints/ComplaintCard";
import { Skeleton } from "@/components/ui/States";
import { usePublicStats } from "@/hooks/usePublicStats";
import { complaintCode, timeAgo } from "@/lib/utils";

export function Home() {
  const stats = usePublicStats();
  return (
    <>
      <Hero stats={stats} />
      <TrustBar />
      <Features />
      <HowItWorks />
      <LiveStats stats={stats} />
      <RecentResolved stats={stats} />
      <Faq />
      <Contact />
    </>
  );
}

function Hero({ stats }: { stats: ReturnType<typeof usePublicStats> }) {
  const navigate = useNavigate();

  return (
    <section className="border-b border-line bg-white">
      <div className="container-page grid items-center gap-8 py-12 sm:gap-12 sm:py-16 lg:grid-cols-2 lg:py-24">
        <div className="animate-fade-in-slow">
          <span className="inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent-soft px-3 py-1 text-xs font-semibold text-brand">
            <span className="h-1.5 w-1.5 rounded-full bg-success" aria-hidden />
            Citizen-first civic platform
          </span>
          <h1 className="mt-5 text-3xl font-extrabold leading-[1.1] tracking-tight text-balance text-ink sm:text-4xl lg:text-5xl xl:text-[3.4rem]">
            Report a sewage issue.
            <span className="block text-brand">Track it to resolution.</span>
          </h1>
          <p className="mt-5 max-w-lg text-lg leading-relaxed text-muted">
            Spotted a blocked drain, overflowing sewer, or open manhole? Report it in under a
            minute with a photo and automatic GPS location — and follow every step until it&apos;s
            fixed.
          </p>
          <div className="mt-8 flex flex-col gap-3 sm:flex-row">
            <Button
              size="lg"
              onClick={() => navigate("/login")}
              icon={<ArrowRight className="h-5 w-5" />}
            >
              Report an Issue
            </Button>
            <Button size="lg" variant="outline" onClick={() => navigate("/track")} icon={<Search className="h-5 w-5" />}>
              Track Complaint
            </Button>
          </div>
          <ul className="mt-8 grid max-w-lg grid-cols-2 gap-x-4 gap-y-3 sm:mt-10 sm:grid-cols-4 sm:gap-x-6 sm:gap-y-4">
            {[
              { icon: <Camera className="h-4 w-4" />, label: "Photo Upload" },
              { icon: <LocateFixed className="h-4 w-4" />, label: "GPS Enabled" },
              { icon: <Clock className="h-4 w-4" />, label: "Real-time Tracking" },
              { icon: <Zap className="h-4 w-4" />, label: "Fast Reporting" },
            ].map((t) => (
              <li key={t.label} className="flex items-center gap-2 text-sm font-medium text-muted">
                <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-accent-soft text-brand">
                  {t.icon}
                </span>
                {t.label}
              </li>
            ))}
          </ul>
        </div>

        {/* Live preview card */}
        <div className="relative mx-auto w-full max-w-md animate-fade-in-slow lg:max-w-none">
          <Card className="p-6">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-ink">Latest report from your city</p>
              <span className="inline-flex items-center gap-1.5 rounded-full bg-success-soft px-2.5 py-1 text-[11px] font-semibold text-emerald-700">
                <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-success" aria-hidden />
                LIVE
              </span>
            </div>
            {stats.loading ? (
              <div className="mt-5 space-y-3">
                <Skeleton className="h-4 w-2/3" />
                <Skeleton className="h-20 w-full" />
                <Skeleton className="h-4 w-1/2" />
              </div>
            ) : stats.latest ? (
              <div className="mt-5">
                <div className="flex items-center justify-between gap-3">
                  <span className="font-mono text-xs font-semibold text-brand">
                    {complaintCode(stats.latest.id)}
                  </span>
                  <StatusBadge status={stats.latest.status} />
                </div>
                <h3 className="mt-3 text-lg font-semibold text-ink">{stats.latest.title}</h3>
                <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-muted">
                  {stats.latest.description}
                </p>
                <div className="mt-5 flex items-center justify-between border-t border-line pt-4 text-xs text-muted">
                  <span className="inline-flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5" aria-hidden />
                    {stats.latest.latitude.toFixed(4)}, {stats.latest.longitude.toFixed(4)}
                  </span>
                  <span>{timeAgo(stats.latest.createdAt)}</span>
                </div>
              </div>
            ) : (
              <div className="mt-5 rounded-xl border border-dashed border-line p-6 text-center text-sm text-muted">
                No reports yet — be the first to report an issue in your neighbourhood.
              </div>
            )}
            <div className="mt-6 grid grid-cols-3 gap-3 border-t border-line pt-5 text-center">
              {[
                { v: stats.loading ? "—" : stats.total, l: "Total reports" },
                { v: stats.loading ? "—" : stats.resolved, l: "Resolved" },
                {
                  v:
                    stats.loading || !stats.avgResolutionDays
                      ? "—"
                      : `${stats.avgResolutionDays.toFixed(1)}d`,
                  l: "Avg. resolution",
                },
              ].map((s) => (
                <div key={s.l}>
                  <p className="text-2xl font-bold text-brand">{s.v}</p>
                  <p className="mt-0.5 text-[11px] font-medium uppercase tracking-wide text-muted">
                    {s.l}
                  </p>
                </div>
              ))}
            </div>
          </Card>
          <div className="absolute -bottom-4 -left-4 hidden -rotate-2 rounded-xl border border-line bg-white px-4 py-3 shadow-lift sm:block animate-fade-in">
            <p className="flex items-center gap-2 text-xs font-semibold text-ink">
              <ShieldCheck className="h-4 w-4 text-success" aria-hidden /> Verified by GHMC
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

function TrustBar() {
  return (
    <section className="border-b border-line bg-canvas">
      <div className="container-page flex flex-wrap items-center justify-center gap-x-10 gap-y-3 py-5 text-sm font-medium text-muted">
        <span>A Government of Telangana initiative</span>
        <span className="hidden h-4 w-px bg-line sm:block" aria-hidden />
        <span>Backed by GHMC & HMWS&SB</span>
        <span className="hidden h-4 w-px bg-line sm:block" aria-hidden />
        <span>Free & open to every citizen</span>
      </div>
    </section>
  );
}

function Features() {
  const features = [
    {
      icon: <Zap className="h-5 w-5" />,
      title: "Easy Reporting",
      description: "A 3-field form, photo, and your location. That's all it takes — most reports take under a minute.",
    },
    {
      icon: <Camera className="h-5 w-5" />,
      title: "Photo Upload",
      description: "Attach a photo so authorities can assess the severity before they even leave the office.",
    },
    {
      icon: <LocateFixed className="h-5 w-5" />,
      title: "Automatic GPS Location",
      description: "We pin your exact location automatically. No addresses to type, no confusion for field teams.",
    },
    {
      icon: <Search className="h-5 w-5" />,
      title: "Complaint Tracking",
      description: "Follow your report from submitted to resolved with a transparent, timestamped timeline.",
    },
    {
      icon: <Clock className="h-5 w-5" />,
      title: "Fast Resolution Updates",
      description: "Get notified the moment your complaint is assigned, worked on, or resolved.",
    },
    {
      icon: <ShieldCheck className="h-5 w-5" />,
      title: "Secure Authentication",
      description: "Your data is protected with JWT-secured accounts. Only you can see your personal reports.",
    },
  ];

  return (
    <section id="features" className="bg-white py-20">
      <div className="container-page">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-accent">Why SewageAlert</p>
          <h2 className="mt-3 text-2xl font-bold tracking-tight text-ink sm:text-3xl lg:text-4xl">
            Everything you need to fix your city
          </h2>
          <p className="mt-4 text-base text-muted sm:text-lg">
            Built around one promise: reporting a civic issue should take less time than it takes to
            complain about it.
          </p>
        </div>
        <div className="mt-10 grid gap-4 sm:mt-14 sm:grid-cols-2 sm:gap-5 lg:grid-cols-3">
          {features.map((f, i) => (
            <Card
              key={f.title}
              className="group p-6 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift"
              style={{ animationDelay: `${i * 40}ms` }}
            >
              <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-accent-soft text-brand transition-colors duration-200 group-hover:bg-brand group-hover:text-white">
                {f.icon}
              </div>
              <h3 className="mt-5 text-lg font-semibold text-ink">{f.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-muted">{f.description}</p>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}

function HowItWorks() {
  const steps = [
    { n: "1", title: "Report Issue", description: "Choose a category, describe the problem, and snap a photo.", icon: <FileText className="h-5 w-5" /> },
    { n: "2", title: "Upload Image", description: "We attach your photo and GPS location automatically.", icon: <Camera className="h-5 w-5" /> },
    { n: "3", title: "Authority Reviews", description: "GHMC reviews, assigns a field team, and starts work.", icon: <Users className="h-5 w-5" /> },
    { n: "4", title: "Issue Resolved", description: "You get notified the moment the issue is resolved.", icon: <CheckCircle2 className="h-5 w-5" /> },
  ];

  return (
    <section id="how-it-works" className="border-t border-line bg-canvas py-20">
      <div className="container-page">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-accent">Simple by design</p>
          <h2 className="mt-3 text-3xl font-bold tracking-tight text-ink sm:text-4xl">How it works</h2>
          <p className="mt-4 text-lg text-muted">Four steps between a dirty street and a clean one.</p>
        </div>
        <ol className="relative mt-10 grid gap-8 sm:mt-14 sm:grid-cols-2 sm:gap-10 lg:grid-cols-4">
          {steps.map((s, i) => (
            <li key={s.n} className="relative">
              {i < steps.length - 1 && (
                <span
                  className="absolute left-full top-7 hidden h-px w-10 bg-line lg:block"
                  aria-hidden
                />
              )}
              <div className="flex items-center gap-4 lg:flex-col lg:items-start lg:gap-0">
                <span className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-brand text-white shadow-card">
                  {s.icon}
                </span>
                <div className="lg:mt-5">
                  <p className="text-xs font-bold uppercase tracking-wider text-accent">Step {s.n}</p>
                  <h3 className="mt-1 text-lg font-semibold text-ink">{s.title}</h3>
                  <p className="mt-1.5 text-sm leading-relaxed text-muted">{s.description}</p>
                </div>
              </div>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}

function LiveStats({ stats }: { stats: ReturnType<typeof usePublicStats> }) {
  const items = [
    { label: "Total Reports", value: stats.total, icon: <FileText className="h-5 w-5" />, tone: "brand" as const },
    { label: "Resolved Issues", value: stats.resolved, icon: <CheckCircle2 className="h-5 w-5" />, tone: "green" as const },
    { label: "Pending Issues", value: stats.pending, icon: <Clock className="h-5 w-5" />, tone: "amber" as const },
    {
      label: "Avg. Resolution Time",
      value: stats.avgResolutionDays ? `${stats.avgResolutionDays.toFixed(1)} days` : "—",
      icon: <Zap className="h-5 w-5" />,
      tone: "blue" as const,
    },
  ];

  return (
    <section className="border-t border-line bg-white py-20">
      <div className="container-page">
        <div className="mx-auto max-w-2xl text-center">
          <p className="text-sm font-semibold uppercase tracking-wider text-accent">Live numbers</p>
          <h2 className="mt-3 text-3xl font-bold tracking-tight text-ink sm:text-4xl">
            Transparency you can see
          </h2>
          <p className="mt-4 text-lg text-muted">
            Real figures pulled from the platform — no screenshots, no spin.
          </p>
        </div>
        {!stats.online && !stats.loading && (
          <p className="mt-6 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-center text-sm text-amber-800">
            The API gateway isn&apos;t reachable right now, so live numbers are unavailable. Start
            the backend services to see real statistics.
          </p>
        )}
        <div className="mt-8 grid gap-4 sm:mt-12 sm:grid-cols-2 sm:gap-5 lg:grid-cols-4">
          {items.map((s) => (
            <Card key={s.label} className="p-5 text-center sm:p-6">
              {stats.loading ? (
                <Skeleton className="mx-auto h-10 w-10 rounded-2xl" />
              ) : (
                <span className="mx-auto flex h-11 w-11 items-center justify-center rounded-2xl bg-accent-soft text-brand">
                  {s.icon}
                </span>
              )}
              {stats.loading ? (
                <Skeleton className="mx-auto mt-4 h-8 w-16" />
              ) : (
                <p className="mt-4 text-3xl font-bold tracking-tight text-ink">{s.value}</p>
              )}
              <p className="mt-1 text-sm font-medium text-muted">{s.label}</p>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}

function RecentResolved({ stats }: { stats: ReturnType<typeof usePublicStats> }) {
  return (
    <section className="border-t border-line bg-canvas py-20">
      <div className="container-page">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-accent">Proof of work</p>
            <h2 className="mt-3 text-3xl font-bold tracking-tight text-ink sm:text-4xl">
              Recently resolved complaints
            </h2>
          </div>
          <Link
            to="/track"
            className="inline-flex h-10 items-center gap-2 rounded-xl border border-line bg-white px-4 text-sm font-semibold text-ink transition-all duration-200 hover:border-accent hover:text-brand"
          >
            Track your own <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
        <div className="mt-10 grid gap-5 lg:grid-cols-3">
          {stats.loading ? (
            [0, 1, 2].map((i) => <Skeleton key={i} className="h-44 w-full rounded-2xl" />)
          ) : stats.recent.length > 0 ? (
            stats.recent.map((c) => <ComplaintCard key={c.id} complaint={c} basePath="/track" />)
          ) : (
            <Card className="col-span-full">
              <div className="flex flex-col items-center gap-2 px-6 py-14 text-center">
                <CheckCircle2 className="h-10 w-10 text-muted/50" />
                <p className="text-sm font-semibold text-ink">No complaints yet</p>
                <p className="text-sm text-muted">When citizens start reporting, resolved cases will appear here.</p>
              </div>
            </Card>
          )}
        </div>
      </div>
    </section>
  );
}

function Faq() {
  const faqs = [
    {
      q: "How long does it take to report an issue?",
      a: "Under a minute. Pick a category, add a short description and a photo, and we attach your GPS location automatically. You can report anonymously-by-login — we just need an account to keep you updated.",
    },
    {
      q: "Who fixes the complaints?",
      a: "Reports go to the Greater Hyderabad Municipal Corporation (GHMC) and are routed to the right field team. You'll see exactly which authority is handling your complaint at every stage.",
    },
    {
      q: "Can I report without a photo?",
      a: "Yes. A photo helps authorities prioritise, but a clear description and location are enough to file a report.",
    },
    {
      q: "How do I check the status of my complaint?",
      a: "Use the Track Complaint page with your complaint ID, or sign in and open My Complaints. Every status change is timestamped on a transparent timeline.",
    },
    {
      q: "Is my location shared publicly?",
      a: "No. Coordinates are visible only to the authorities working on your case. The public feed shows resolved examples without personal information.",
    },
  ];
  const [open, setOpen] = useState<number | null>(0);

  return (      <section id="faq" className="border-t border-line bg-white py-16 sm:py-20">
      <div className="container-page grid gap-10 lg:grid-cols-[1fr_1.4fr] lg:gap-12">
        <div>
          <p className="text-sm font-semibold uppercase tracking-wider text-accent">FAQ</p>
          <h2 className="mt-3 text-3xl font-bold tracking-tight text-ink sm:text-4xl">
            Questions, answered
          </h2>
          <p className="mt-4 text-lg text-muted">
            Can&apos;t find what you need? Our helpline is open Monday to Saturday.
          </p>
          <a href="tel:04023456789" className="mt-6 inline-flex items-center gap-2 text-sm font-semibold text-brand hover:underline">
            <Phone className="h-4 w-4" /> 040-2345 6789
          </a>
        </div>
        <div className="space-y-3">
          {faqs.map((f, i) => {
            const isOpen = open === i;
            return (
              <div key={f.q} className="overflow-hidden rounded-2xl border border-line bg-white">
                <button
                  onClick={() => setOpen(isOpen ? null : i)}
                  className="flex w-full items-center justify-between gap-4 px-5 py-4 text-left"
                  aria-expanded={isOpen}
                >
                  <span className="font-semibold text-ink">{f.q}</span>
                  <ChevronDown
                    className={`h-5 w-5 shrink-0 text-muted transition-transform duration-200 ${isOpen ? "rotate-180" : ""}`}
                    aria-hidden
                  />
                </button>
                {isOpen && (
                  <p className="border-t border-line px-5 py-4 text-sm leading-relaxed text-muted animate-fade-in">
                    {f.a}
                  </p>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}

function Contact() {
  const navigate = useNavigate();
  return (
    <section id="contact" className="border-t border-line bg-canvas py-16 sm:py-20">
      <div className="container-page">
        <Card className="overflow-hidden">
          <div className="grid lg:grid-cols-2">
            <div className="bg-brand p-8 text-white sm:p-12">
              <h2 className="text-3xl font-bold tracking-tight">We&apos;re here to help</h2>
              <p className="mt-4 max-w-md leading-relaxed text-white/75">
                Have a question about a complaint, a technical issue, or a suggestion for the
                platform? Reach out — a real person reads every message.
              </p>
              <ul className="mt-10 space-y-5">
                <li className="flex items-center gap-4">
                  <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/10">
                    <Phone className="h-5 w-5 text-accent" />
                  </span>
                  <div>
                    <p className="text-xs text-white/60">Helpline</p>
                    <p className="font-semibold">040-2345 6789</p>
                  </div>
                </li>
                <li className="flex items-center gap-4">
                  <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/10">
                    <Mail className="h-5 w-5 text-accent" />
                  </span>
                  <div>
                    <p className="text-xs text-white/60">Email</p>
                    <p className="font-semibold">support@sewagealert.telangana.gov.in</p>
                  </div>
                </li>
                <li className="flex items-center gap-4">
                  <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/10">
                    <MapPin className="h-5 w-5 text-accent" />
                  </span>
                  <div>
                    <p className="text-xs text-white/60">GHMC Head Office</p>
                    <p className="font-semibold">Tank Bund Road, Hyderabad 500063</p>
                  </div>
                </li>
              </ul>
            </div>
            <div className="flex flex-col items-center justify-center gap-4 p-8 text-center sm:p-12">
              <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-success-soft text-emerald-600">
                <Zap className="h-6 w-6" />
              </div>
              <h3 className="text-xl font-bold text-ink">Found an issue right now?</h3>
              <p className="max-w-sm text-sm leading-relaxed text-muted">
                Skip the phone call. Report it directly and get a tracking ID in under a minute.
              </p>
              <Button size="lg" onClick={() => navigate("/login")} className="mt-2">
                Report an Issue <ArrowRight className="h-5 w-5" />
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </section>
  );
}
