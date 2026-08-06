import { useEffect, useState } from "react";
import { BadgeCheck, KeyRound, PencilLine, Settings, ShieldCheck, UserRound } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { Field, Input } from "@/components/ui/Field";
import { Skeleton } from "@/components/ui/States";
import { useAuth } from "@/lib/auth";
import { api } from "@/lib/api";
import { formatDate, initials } from "@/lib/utils";
import type { UserProfile } from "@/types";

const QUICK_ACTIONS = [
  {
    title: "Edit profile",
    description: "Update your administrator name and contact details.",
    icon: <PencilLine className="h-5 w-5" aria-hidden />,
  },
  {
    title: "Change password",
    description: "Reset your account password securely.",
    icon: <KeyRound className="h-5 w-5" aria-hidden />,
  },
  {
    title: "Account settings",
    description: "Manage notifications and platform preferences.",
    icon: <Settings className="h-5 w-5" aria-hidden />,
  },
];

export function AdminProfile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    if (user?.id == null) {
      setLoading(false);
      return;
    }
    api
      .get<UserProfile>(`/api/v1/users/auth/${user.id}`)
      .then((p) => {
        if (!cancelled) setProfile(p);
      })
      .catch(() => {
        // user profile may not exist yet — still show auth-level details
        if (!cancelled) setProfile(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [user?.id]);

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">My profile</h1>
        <p className="mt-1 text-muted">Manage your administrator account details.</p>
      </div>

      {/* Identity card */}
      <Card className="flex flex-col gap-5 p-6 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-4">
          <span className="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-brand text-xl font-bold text-white">
            {initials(user?.name)}
          </span>
          <div className="min-w-0">
            <p className="truncate text-lg font-bold text-ink">{user?.name ?? "—"}</p>
            <p className="truncate text-sm text-muted">{user?.email ?? "—"}</p>
            <div className="mt-1.5 flex flex-wrap items-center gap-1.5">
              <span className="inline-flex items-center gap-1 rounded-full bg-accent-soft px-2.5 py-0.5 text-xs font-semibold text-brand">
                <ShieldCheck className="h-3.5 w-3.5" aria-hidden />
                {user?.role ?? "ADMIN"}
              </span>
              <Badge tone="green">
                <BadgeCheck className="h-3 w-3" aria-hidden />
                Active
              </Badge>
            </div>
          </div>
        </div>
        <dl className="space-y-1 text-left text-xs text-muted sm:text-right">
          <div>
            <dt className="sr-only">Admin ID</dt>
            <dd>Admin ID · #{user?.id ?? "—"}</dd>
          </div>
          <div>
            <dt className="sr-only">Joined</dt>
            <dd>Joined {loading ? "…" : formatDate(profile?.createdAt)}</dd>
          </div>
        </dl>
      </Card>

      {/* Account information */}
      <Card>
        <CardHeader
          title="Account information"
          description="Details linked to your administrator account."
        />
        {loading ? (
          <div className="space-y-5 p-6">
            <Skeleton className="h-10 w-full" />
            <div className="grid gap-5 sm:grid-cols-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
            <div className="grid gap-5 sm:grid-cols-2">
              <Skeleton className="h-10 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          </div>
        ) : (
          <div className="grid gap-5 p-6 sm:grid-cols-2">
            <Field label="Name">
              <Input value={user?.name ?? ""} disabled />
            </Field>
            <Field label="Email" hint="Managed by your sign-in — contact helpline to change.">
              <Input value={user?.email ?? ""} disabled />
            </Field>
            <Field label="Phone">
              <Input value={profile?.phone ? String(profile.phone) : "—"} disabled />
            </Field>
            <Field label="Role">
              <Input value={user?.role ?? "ADMIN"} disabled />
            </Field>
            <Field label="Account status">
              <div className="flex h-10 items-center">
                <Badge tone="green">
                  <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden />
                  Active
                </Badge>
              </div>
            </Field>
            <Field label="Joined date">
              <Input value={formatDate(profile?.createdAt)} disabled />
            </Field>
          </div>
        )}
      </Card>

      {/* Quick actions */}
      <div>
        <h2 className="text-base font-semibold text-ink">Quick actions</h2>
        <p className="mt-0.5 text-sm text-muted">More profile tools are on the way.</p>
        <div className="mt-4 grid gap-4 sm:grid-cols-3">
          {QUICK_ACTIONS.map((action) => (
            <Card key={action.title} className="flex flex-col p-5">
              <div className="flex items-start gap-3">
                <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-accent-soft text-brand">
                  {action.icon}
                </span>
                <div className="min-w-0">
                  <p className="text-sm font-semibold text-ink">{action.title}</p>
                  <p className="mt-0.5 text-xs leading-relaxed text-muted">{action.description}</p>
                </div>
              </div>
              <div className="mt-4 flex-1" />
              <Button variant="outline" disabled fullWidth>
                Coming soon
              </Button>
            </Card>
          ))}
        </div>
      </div>

      <p className="flex items-center gap-1.5 text-xs text-muted">
        <UserRound className="h-3.5 w-3.5" aria-hidden />
        Signed in as {user?.email ?? "administrator"}
      </p>
    </div>
  );
}
