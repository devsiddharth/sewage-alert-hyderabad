import { useState, type FormEvent } from "react";
import { Building2, Plus, Tags, Trash2, TriangleAlert, X } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Field";
import { useToast } from "@/lib/toast";
import { ISSUE_CATEGORIES } from "@/types";

// Settings are stored locally in localStorage — the backend does not expose
// configuration endpoints yet. Seed values mirror the platform defaults.
const CATS_KEY = "sa_categories";
const DEPTS_KEY = "sa_departments";

const defaultDepartments = ["GHMC — Sanitation", "HMWS&SB — Sewage", "GHMC — Roads & Drains", "Forest & Environment"];

function loadList(key: string, fallback: string[]): string[] {
  try {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    const parsed = JSON.parse(raw) as string[];
    return Array.isArray(parsed) && parsed.length > 0 ? parsed : fallback;
  } catch {
    return fallback;
  }
}

export function SettingsPage() {
  const { toast } = useToast();
  const [categories, setCategories] = useState<string[]>(() => loadList(CATS_KEY, [...ISSUE_CATEGORIES]));
  const [departments, setDepartments] = useState<string[]>(() => loadList(DEPTS_KEY, defaultDepartments));
  const [newCategory, setNewCategory] = useState("");
  const [newDept, setNewDept] = useState("");

  const [prefs, setPrefs] = useState({
    notifyNew: true,
    notifyStatus: true,
    notifyWeekly: false,
    publicFeed: true,
  });

  const addItem = (e: FormEvent, key: "cat" | "dept") => {
    e.preventDefault();
    const value = (key === "cat" ? newCategory : newDept).trim();
    if (!value) return;
    const list = key === "cat" ? categories : departments;
    if (list.some((c) => c.toLowerCase() === value.toLowerCase())) {
      toast("error", "Already exists");
      return;
    }
    const next = [...list, value];
    if (key === "cat") {
      setCategories(next);
      localStorage.setItem(CATS_KEY, JSON.stringify(next));
      setNewCategory("");
    } else {
      setDepartments(next);
      localStorage.setItem(DEPTS_KEY, JSON.stringify(next));
      setNewDept("");
    }
    toast("success", key === "cat" ? "Category added" : "Department added");
  };

  const removeItem = (key: "cat" | "dept", value: string) => {
    const list = key === "cat" ? categories : departments;
    const next = list.filter((c) => c !== value);
    if (key === "cat") {
      setCategories(next);
      localStorage.setItem(CATS_KEY, JSON.stringify(next));
    } else {
      setDepartments(next);
      localStorage.setItem(DEPTS_KEY, JSON.stringify(next));
    }
    toast("success", "Removed");
  };

  const togglePref = (key: keyof typeof prefs, label: string) => {
    setPrefs((p) => ({ ...p, [key]: !p[key] }));
    toast("success", `${label} ${prefs[key] ? "disabled" : "enabled"}`);
  };

  const ListEditor = ({
    title,
    items,
    onRemove,
    onSubmit,
    value,
    onChange,
    placeholder,
    icon,
  }: {
    title: string;
    items: string[];
    onRemove: (v: string) => void;
    onSubmit: (e: FormEvent) => void;
    value: string;
    onChange: (v: string) => void;
    placeholder: string;
    icon: React.ReactNode;
  }) => (
    <Card>
      <CardHeader title={title} />
      <div className="p-6">
        <form onSubmit={onSubmit} className="flex gap-2">
          <Input value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} />
          <Button type="submit" icon={<Plus className="h-4 w-4" />}>Add</Button>
        </form>
        <ul className="mt-4 flex flex-wrap gap-2">
          {items.map((c) => (
            <li key={c} className="group inline-flex items-center gap-2 rounded-full border border-line bg-canvas py-1.5 pl-3.5 pr-2 text-sm font-medium text-ink">
              {icon}
              {c}
              <button
                onClick={() => onRemove(c)}
                className="rounded-full p-1 text-muted transition-colors hover:bg-white hover:text-red-600"
                aria-label={`Remove ${c}`}
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </li>
          ))}
        </ul>
      </div>
    </Card>
  );

  return (
    <div className="mx-auto max-w-3xl space-y-5 sm:space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">Settings</h1>
        <p className="mt-1 text-sm text-muted sm:text-base">Platform configuration used across the reporting workflow.</p>
      </div>

      <div className="flex items-start gap-3 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
        <TriangleAlert className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
        <p>
          These settings are saved on this device. Changes here affect how the report form
          presents categories and departments.
        </p>
      </div>

      <ListEditor
        title="Issue categories"
        items={categories}
        onRemove={(v) => removeItem("cat", v)}
        onSubmit={(e) => addItem(e, "cat")}
        value={newCategory}
        onChange={setNewCategory}
        placeholder="e.g. Waterlogging"
        icon={<Tags className="h-3.5 w-3.5 text-muted" aria-hidden />}
      />

      <ListEditor
        title="Departments"
        items={departments}
        onRemove={(v) => removeItem("dept", v)}
        onSubmit={(e) => addItem(e, "dept")}
        value={newDept}
        onChange={setNewDept}
        placeholder="e.g. HMWS&SB — Water Supply"
        icon={<Building2 className="h-3.5 w-3.5 text-muted" aria-hidden />}
      />

      <Card>
        <CardHeader title="Priorities" description="Used by authorities to triage complaints." />
        <div className="p-6">
          <div className="grid gap-3 sm:grid-cols-2">
            {[
              { p: "LOW", c: "bg-slate-100 text-slate-700" },
              { p: "MEDIUM", c: "bg-amber-50 text-amber-700" },
              { p: "HIGH", c: "bg-red-50 text-red-700" },
              { p: "CRITICAL", c: "bg-red-100 text-red-800" },
            ].map((x) => (
              <div key={x.p} className={`flex items-center justify-between rounded-xl px-4 py-3 text-sm font-semibold ${x.c}`}>
                {x.p}
                <span className="flex items-center gap-1 text-xs font-medium opacity-70">
                  <Trash2 className="h-3 w-3" /> Locked
                </span>
              </div>
            ))}
          </div>
        </div>
      </Card>

      <Card>
        <CardHeader title="Notification settings" description="Platform-wide notification behaviour." />
        <div className="divide-y divide-line">
          {(
            [
              { key: "notifyNew" as const, label: "New complaint notifications", desc: "Alert authorities when a citizen files a report" },
              { key: "notifyStatus" as const, label: "Status change notifications", desc: "Notify citizens on every status update" },
              { key: "notifyWeekly" as const, label: "Weekly digest", desc: "Weekly summary emailed to administrators" },
              { key: "publicFeed" as const, label: "Public resolved feed", desc: "Show resolved complaints on the landing page" },
            ]
          ).map((p) => (
            <button key={p.key} onClick={() => togglePref(p.key, p.label)} className="flex w-full items-center justify-between gap-4 px-6 py-4 text-left transition-colors hover:bg-canvas/60">
              <div>
                <p className="text-sm font-medium text-ink">{p.label}</p>
                <p className="mt-0.5 text-xs text-muted">{p.desc}</p>
              </div>
              <span className={`relative h-6 w-11 shrink-0 rounded-full transition-colors duration-200 ${prefs[p.key] ? "bg-brand" : "bg-line"}`} aria-hidden>
                <span className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all duration-200 ${prefs[p.key] ? "left-[22px]" : "left-0.5"}`} />
              </span>
            </button>
          ))}
        </div>
      </Card>
    </div>
  );
}
