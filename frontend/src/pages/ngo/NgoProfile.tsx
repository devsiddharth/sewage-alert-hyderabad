import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Save } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import type { NgoOrganization } from "@/types";

export function NgoProfile() {
  const { toast } = useToast();
  const [org, setOrg] = useState<NgoOrganization | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const dashboard = await api.get<{ organization: NgoOrganization }>("/api/v1/ngo/dashboard");
      setOrg(dashboard.organization);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const update = (field: string, value: string) => setOrg((prev) => prev ? { ...prev, [field]: value } : null);

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    if (!org) return;
    setSaving(true);
    try {
      await api.put(`/api/v1/ngo/organizations/${org.id}`, {
        organizationName: org.organizationName,
        officialEmail: org.officialEmail,
        officialPhone: org.officialPhone,
        website: org.website,
        address: org.address,
        operatingAreas: org.operatingAreas,
        mission: org.mission,
        areasOfFocus: org.areasOfFocus,
        communitiesServed: org.communitiesServed,
        contactPersonName: org.contactPersonName,
        contactPersonEmail: org.contactPersonEmail,
        contactPersonPhone: org.contactPersonPhone,
      });
      toast("success", "Profile updated", "Your organization profile has been saved.");
    } catch (err) {
      toast("error", "Update failed", err instanceof ApiError ? err.message : "Could not save profile.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Skeleton className="h-96 w-full rounded-2xl" />;
  if (!org) return null;

  return (
    <div className="space-y-6 animate-fade-in">
      <h1 className="text-2xl font-bold tracking-tight text-ink">Organization Profile</h1>

      <form onSubmit={handleSave} className="space-y-6">
        <Card className="p-6">
          <h2 className="text-lg font-semibold text-ink mb-4">Basic Information</h2>
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Organization Name" required className="sm:col-span-2">
              <Input value={org.organizationName} onChange={(e) => update("organizationName", e.target.value)} />
            </Field>
            <Field label="Official Email" required>
              <Input type="email" value={org.officialEmail} onChange={(e) => update("officialEmail", e.target.value)} />
            </Field>
            <Field label="Phone">
              <Input value={org.officialPhone} onChange={(e) => update("officialPhone", e.target.value)} />
            </Field>
            <Field label="Website">
              <Input value={org.website || ""} onChange={(e) => update("website", e.target.value)} placeholder="https://..." />
            </Field>
            <Field label="Registration Number">
              <Input value={org.registrationNumber || ""} disabled />
            </Field>
            <Field label="Address" className="sm:col-span-2">
              <Input value={org.address || ""} onChange={(e) => update("address", e.target.value)} />
            </Field>
          </div>
        </Card>

        <Card className="p-6">
          <h2 className="text-lg font-semibold text-ink mb-4">Mission & Focus</h2>
          <div className="grid gap-5">
            <Field label="Mission">
              <Textarea rows={3} value={org.mission || ""} onChange={(e) => update("mission", e.target.value)} />
            </Field>
            <div className="grid gap-5 sm:grid-cols-2">
              <Field label="Areas of Focus">
                <Input value={org.areasOfFocus || ""} onChange={(e) => update("areasOfFocus", e.target.value)} />
              </Field>
              <Field label="Operating Areas">
                <Input value={org.operatingAreas || ""} onChange={(e) => update("operatingAreas", e.target.value)} />
              </Field>
            </div>
            <Field label="Communities Served">
              <Input value={org.communitiesServed || ""} onChange={(e) => update("communitiesServed", e.target.value)} />
            </Field>
          </div>
        </Card>

        <Card className="p-6">
          <h2 className="text-lg font-semibold text-ink mb-4">Contact Person</h2>
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Name">
              <Input value={org.contactPersonName || ""} onChange={(e) => update("contactPersonName", e.target.value)} />
            </Field>
            <Field label="Email">
              <Input type="email" value={org.contactPersonEmail || ""} onChange={(e) => update("contactPersonEmail", e.target.value)} />
            </Field>
            <Field label="Phone">
              <Input value={org.contactPersonPhone || ""} onChange={(e) => update("contactPersonPhone", e.target.value)} />
            </Field>
          </div>
        </Card>

        <Button type="submit" size="lg" loading={saving} icon={<Save className="h-4 w-4" />}>
          Save Changes
        </Button>
      </form>
    </div>
  );
}
