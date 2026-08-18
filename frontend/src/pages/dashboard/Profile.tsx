import { useEffect, useState, type FormEvent } from "react";
import { KeyRound, Save, ShieldCheck, UserRound } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { Skeleton } from "@/components/ui/States";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { initials, formatDate } from "@/lib/utils";
import type { UserProfile } from "@/types";

const PREF_KEY = "sa_notification_prefs";

interface Prefs {
  assigned: boolean;
  updated: boolean;
  resolved: boolean;
  email: boolean;
}

const defaultPrefs: Prefs = { assigned: true, updated: true, resolved: true, email: false };

function loadPrefs(): Prefs {
  try {
    return { ...defaultPrefs, ...(JSON.parse(localStorage.getItem(PREF_KEY) ?? "{}") as Partial<Prefs>) };
  } catch {
    return defaultPrefs;
  }
}

export function Profile() {
  const { user } = useAuth();
  const { toast } = useToast();

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [prefs, setPrefs] = useState<Prefs>(loadPrefs);

  useEffect(() => {
    let cancelled = false;
    api
      .get<UserProfile>(`/api/v1/users/auth/${user?.id}`)
      .then((p) => {
        if (cancelled) return;
        setProfile(p);
        setName(p.name);
        setPhone(p.phone ? String(p.phone) : "");
        setAddress(p.address ?? "");
      })
      .catch(() => {
        // profile may not exist yet — still allow editing name/phone (profile is created via auth-service)
        if (!cancelled) setProfile(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [user?.id]);

  const saveProfile = async (e: FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      toast("error", "Name is required");
      return;
    }
    setSaving(true);
    try {
      const payload = {
        name: name.trim(),
        phone: phone ? Number(phone) : null,
        address: address.trim() || null,
        profilePictureUrl: null,
        preferences: null,
      };
      if (profile) {
        await api.put(`/api/v1/users/${profile.id}`, payload);
      } else if (user?.id != null) {
        await api.post("/api/v1/users", payload);
      }
      toast("success", "Profile saved", "Your details were updated.");
    } catch (err) {
      toast("error", "Couldn't save profile", err instanceof ApiError ? err.message : undefined);
    } finally {
      setSaving(false);
    }
  };

  const togglePref = (key: keyof Prefs) => {
    const next = { ...prefs, [key]: !prefs[key] };
    setPrefs(next);
    localStorage.setItem(PREF_KEY, JSON.stringify(next));
    toast("success", "Preferences saved");
  };

  return (
    <div className="mx-auto max-w-3xl space-y-5 sm:space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">Profile settings</h1>
        <p className="mt-1 text-sm text-muted sm:text-base">Manage your account details and preferences.</p>
      </div>

      {/* Identity card */}
      <Card className="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between sm:gap-5 sm:p-6">
        <div className="flex items-center gap-4">
          <span className="flex h-16 w-16 items-center justify-center rounded-2xl bg-brand text-xl font-bold text-white">
            {initials(user?.name)}
          </span>
          <div>
            <p className="text-lg font-bold text-ink">{user?.name}</p>
            <p className="text-sm text-muted">{user?.email}</p>
            <p className="mt-1 inline-flex items-center gap-1 rounded-full bg-accent-soft px-2.5 py-0.5 text-xs font-semibold text-brand">
              <ShieldCheck className="h-3.5 w-3.5" aria-hidden />
              {user?.role}
            </p>
          </div>
        </div>
        {profile && (
          <p className="text-xs text-muted">
            Member since {formatDate(profile.createdAt)}
          </p>
        )}
      </Card>

      <div className="grid gap-6 lg:grid-cols-[1.5fr_1fr]">
        {/* Personal info */}
        <Card>
          <CardHeader
            title="Personal information"
            description="This is visible to the authorities handling your complaints."
          />
          {loading ? (
            <div className="space-y-4 p-6">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-24 w-full" />
            </div>
          ) : (
            <form onSubmit={saveProfile} className="space-y-5 p-6">
              <Field label="Full name" required>
                <Input value={name} onChange={(e) => setName(e.target.value)} />
              </Field>
              <div className="grid gap-5 sm:grid-cols-2">
                <Field label="Email" hint="Managed by your sign-in — contact helpline to change.">
                  <Input value={user?.email ?? ""} disabled />
                </Field>
                <Field label="Phone">
                  <Input
                    type="tel"
                    inputMode="numeric"
                    value={phone}
                    onChange={(e) => setPhone(e.target.value.replace(/\D/g, "").slice(0, 10))}
                    placeholder="98765 43210"
                  />
                </Field>
              </div>
              <Field label="Address">
                <Textarea rows={2} value={address} onChange={(e) => setAddress(e.target.value)} placeholder="e.g. 12-3-456, Ameerpet, Hyderabad" />
              </Field>
              <div className="flex justify-end">
                <Button type="submit" loading={saving} icon={<Save className="h-4 w-4" />}>
                  Save changes
                </Button>
              </div>
            </form>
          )}
        </Card>

        <div className="space-y-6">
          {/* Password */}
          <Card>
            <CardHeader title="Change password" />
            <div className="p-6">
              <div className="flex items-start gap-3 rounded-xl bg-canvas p-4">
                <KeyRound className="mt-0.5 h-4 w-4 shrink-0 text-muted" aria-hidden />
                <p className="text-sm leading-relaxed text-muted">
                  To change your password, call our helpline at{" "}
                  <a href="tel:04023456789" className="font-semibold text-brand hover:underline">
                    040-2345 6789
                  </a>{" "}
                  and a support agent will help you.
                </p>
              </div>
            </div>
          </Card>

          {/* Notification preferences */}
          <Card>
            <CardHeader title="Notification preferences" description="Where and when we notify you." />
            <div className="divide-y divide-line">
              {(
                [
                  { key: "assigned" as const, label: "Complaint assigned", desc: "When an authority picks up your report" },
                  { key: "updated" as const, label: "Status updates", desc: "Any progress on your complaints" },
                  { key: "resolved" as const, label: "Resolution", desc: "When your issue is marked resolved" },
                  { key: "email" as const, label: "Email alerts", desc: "Mirror notifications to your inbox" },
                ]
              ).map((p) => (
                <button key={p.key} onClick={() => togglePref(p.key)} className="flex w-full items-center justify-between gap-4 px-6 py-4 text-left transition-colors hover:bg-canvas/60">
                  <div>
                    <p className="text-sm font-medium text-ink">{p.label}</p>
                    <p className="mt-0.5 text-xs text-muted">{p.desc}</p>
                  </div>
                  <span
                    className={`relative h-6 w-11 shrink-0 rounded-full transition-colors duration-200 ${prefs[p.key] ? "bg-brand" : "bg-line"}`}
                    aria-hidden
                  >
                    <span
                      className={`absolute top-0.5 h-5 w-5 rounded-full bg-white shadow transition-all duration-200 ${prefs[p.key] ? "left-[22px]" : "left-0.5"}`}
                    />
                  </span>
                </button>
              ))}
            </div>
          </Card>

          {/* Account */}
          <Card>
            <CardHeader title="Account" />
            <div className="flex items-center gap-3 p-6">
              <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-accent-soft text-brand">
                <UserRound className="h-5 w-5" aria-hidden />
              </span>
              <p className="text-sm text-muted">
                Signed in as <span className="font-semibold text-ink">{user?.email}</span>
              </p>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
