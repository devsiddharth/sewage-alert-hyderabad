import { ArrowDown, Building2, Droplets, Factory, Filter, GitBranch, Gauge, Leaf, Pipette, Recycle, ShieldCheck, Waves } from "lucide-react";
import { Card } from "@/components/ui/Card";

const TREATMENT_STEPS = [
  {
    icon: <Filter className="h-5 w-5" />,
    step: "Step 1",
    title: "Screening & Grit Removal",
    description:
      "Raw sewage first passes through coarse and fine screens that trap rags, plastic and large debris, followed by grit chambers that settle sand and gravel before the water moves on.",
  },
  {
    icon: <Gauge className="h-5 w-5" />,
    step: "Step 2",
    title: "Primary Sedimentation",
    description:
      "In primary clarifiers, heavier suspended solids settle as sludge while oils and grease float to the surface and are skimmed off — typically removing 50–60% of suspended solids.",
  },
  {
    icon: <Pipette className="h-5 w-5" />,
    step: "Step 3",
    title: "Secondary Biological Treatment",
    description:
      "Micro-organisms consume the dissolved organic pollutants. Hyderabad's STPs use technologies such as Activated Sludge, SBR, MBBR and UASB — the heart of the treatment process.",
  },
  {
    icon: <Recycle className="h-5 w-5" />,
    step: "Step 4",
    title: "Sludge Treatment & Tertiary Polishing",
    description:
      "Settled sludge is digested and dried for safe reuse, while the clarified water may pass through tertiary filters and disinfection — producing treated water safe enough for reuse.",
  },
  {
    icon: <Leaf className="h-5 w-5" />,
    step: "Step 5",
    title: "Reuse & Discharge",
    description:
      "Treated water is reused for lake rejuvenation, horticulture, construction and groundwater recharge — reducing freshwater demand across the city.",
  },
];

const PIPELINE_FEATURES = [
  {
    icon: <GitBranch className="h-5 w-5" />,
    title: "Gravity-based trunk & branch sewers",
    description:
      "Hyderabad's sewerage network follows the natural terrain, carrying wastewater by gravity through branch sewers into large trunk sewers that feed the treatment plants.",
  },
  {
    icon: <Building2 className="h-5 w-5" />,
    title: "Lifting & pumping stations",
    description:
      "Where terrain levels out, sewage pumping stations lift the flow back into the network so it can continue its journey downstream by gravity.",
  },
  {
    icon: <ShieldCheck className="h-5 w-5" />,
    title: "Manholes & maintenance access",
    description:
      "Networks are designed with access manholes at every change of direction, gradient or junction, so blockages can be located and cleared quickly.",
  },
  {
    icon: <Waves className="h-5 w-5" />,
    title: "Sewerage zones feeding STPs",
    description:
      "The city is divided into sewerage zones, each conveying its flow to a designated Sewage Treatment Plant — keeping treatment load balanced and efficient.",
  },
];

export function InfrastructurePage() {
  return (
    <>
      <InfraHero />
      <TreatmentSection />
      <PipelineSection />
      <StpBanner />
    </>
  );
}

function InfraHero() {
  return (
    <section className="border-b border-line bg-white">
      <div className="container-page py-10 text-center sm:py-14 lg:py-20">
        <span className="inline-flex items-center gap-2 rounded-full border border-accent/30 bg-accent-soft px-3 py-1 text-xs font-semibold text-brand">
          <Droplets className="h-3.5 w-3.5" aria-hidden /> Water & sanitation infrastructure
        </span>
        <h1 className="mx-auto mt-5 max-w-3xl text-3xl font-extrabold tracking-tight text-ink sm:text-4xl lg:text-5xl">
          How Hyderabad treats its sewage & designs its <span className="text-brand">pipeline network</span>
        </h1>
        <p className="mx-auto mt-4 max-w-2xl text-base text-muted sm:text-lg">
          From the treatment plan followed by HMWS&SB to the sewer network beneath your street — understand the journey of wastewater in Hyderabad.
        </p>
      </div>
    </section>
  );
}

function TreatmentSection() {
  return (
    <section className="border-t border-line bg-white py-16 sm:py-20">
      <div className="container-page">
        <div className="grid gap-10 lg:grid-cols-[1fr_1.5fr] lg:items-start">
          <div className="lg:sticky lg:top-24">
            <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-brand/10 text-brand">
              <Factory className="h-6 w-6" aria-hidden />
            </span>
            <h2 className="mt-5 text-3xl font-bold tracking-tight text-ink">
              The HMWS&SB treatment plan
            </h2>
            <p className="mt-4 leading-relaxed text-muted">
              The <strong className="font-semibold text-ink">Hyderabad Metropolitan Water Supply and Sewerage Board (HMWS&SB)</strong>{" "}
              operates a network of Sewage Treatment Plants (STPs) across the city. Every litre of
              sewage is collected, treated to prescribed standards and then reused or safely
              discharged.
            </p>
            <p className="mt-3 leading-relaxed text-muted">
              The treatment plan follows a proven multi-stage process — from physical screening to
              biological purification — before water is returned to lakes or reused for non-potable
              purposes.
            </p>
          </div>

          <ol className="relative space-y-5">
            {TREATMENT_STEPS.map((s, i) => (
              <li key={s.title} className="relative pl-14">
                {i < TREATMENT_STEPS.length - 1 && (
                  <span className="absolute left-[22px] top-12 h-[calc(100%-1.5rem)] w-px bg-line" aria-hidden />
                )}
                <span className="absolute left-0 top-0 flex h-11 w-11 items-center justify-center rounded-2xl bg-brand text-white shadow-card">
                  {s.icon}
                </span>
                <Card className="p-5 transition-shadow duration-200 hover:shadow-lift">
                  <p className="text-xs font-bold uppercase tracking-wider text-accent">{s.step}</p>
                  <h3 className="mt-1 text-lg font-semibold text-ink">{s.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-muted">{s.description}</p>
                </Card>
              </li>
            ))}
            <li className="pl-14">
              <span className="absolute left-0 top-0 flex h-11 w-11 items-center justify-center rounded-2xl bg-emerald-500 text-white shadow-card">
                <ArrowDown className="h-5 w-5" aria-hidden />
              </span>
              <Card className="border-emerald-100 bg-emerald-50/60 p-5">
                <h3 className="text-lg font-semibold text-emerald-900">End result: cleaner lakes & less freshwater use</h3>
                <p className="mt-2 text-sm leading-relaxed text-emerald-800/80">
                  Reused treated water sustains the city's lakes and green spaces while reducing pressure on drinking-water supplies.
                </p>
              </Card>
            </li>
          </ol>
        </div>
      </div>
    </section>
  );
}

function PipelineSection() {
  return (
    <section className="border-t border-line bg-canvas py-16 sm:py-20">
      <div className="container-page">
        <div className="mx-auto max-w-2xl text-center">
          <span className="flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500/10 text-amber-600 mx-auto">
            <GitBranch className="h-6 w-6" aria-hidden />
          </span>
          <h2 className="mt-5 text-3xl font-bold tracking-tight text-ink">Pipeline design of Hyderabad</h2>
          <p className="mt-4 text-lg text-muted">
            A sewer network is designed like the veins of a city — engineered to move wastewater reliably for decades.
          </p>
        </div>

        <div className="mt-10 grid gap-4 sm:mt-12 sm:grid-cols-2 sm:gap-5 lg:grid-cols-4">
          {PIPELINE_FEATURES.map((f) => (
            <Card
              key={f.title}
              className="group p-6 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lift"
            >
              <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-amber-50 text-amber-600 transition-colors duration-200 group-hover:bg-amber-500 group-hover:text-white">
                {f.icon}
              </span>
              <h3 className="mt-5 text-base font-semibold text-ink">{f.title}</h3>
              <p className="mt-2 text-sm leading-relaxed text-muted">{f.description}</p>
            </Card>
          ))}
        </div>

        <div className="mt-12 rounded-2xl border border-line bg-white p-6 sm:p-8">
          <h3 className="flex items-center gap-2 text-lg font-semibold text-ink">
            <Gauge className="h-5 w-5 text-brand" aria-hidden /> Did you know?
          </h3>
          <div className="mt-4 grid gap-4 text-sm leading-relaxed text-muted sm:grid-cols-2">
            <p>
              Sewer pipes are laid at a <strong className="font-semibold text-ink">minimum self-cleaning slope</strong> so
              flowing wastewater keeps solids moving — a steeper slope than needed just wastes excavation, while a flatter one lets solids settle.
            </p>
            <p>
              Manholes are placed at regular intervals — typically every <strong className="font-semibold text-ink">30–50 metres</strong> on small sewers —
              giving crews access to clear blockages before they surface as a complaint.
            </p>
            <p>
              Wastewater flows from your home into a <strong className="font-semibold text-ink">branch sewer</strong>, then a{" "}
              <strong className="font-semibold text-ink">trunk sewer</strong>, and finally to an{" "}
              <strong className="font-semibold text-ink">STP</strong> — the same journey, street by street, zone by zone.
            </p>
            <p>
              When you report a complaint, authorities can trace which <strong className="font-semibold text-ink">sewerage zone</strong> and
              pipeline segment serves your location — that's how field teams know exactly where to dig.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

function StpBanner() {
  return (
    <section className="border-t border-line bg-brand">
      <div className="container-page flex flex-col items-center gap-6 py-16 text-center sm:flex-row sm:text-left">
        <span className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl bg-white/10 text-white">
          <Recycle className="h-7 w-7" aria-hidden />
        </span>
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Treatment plants across Hyderabad</h2>
          <p className="mt-1 max-w-2xl text-white/75">
            Administrators maintain a live registry of STPs, their capacity, treatment method and water-reuse details — explore it in the admin console.
          </p>
        </div>
      </div>
    </section>
  );
}
