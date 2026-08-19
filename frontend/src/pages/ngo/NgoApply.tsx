import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowLeft, HeartHandshake, Send, CheckCircle2 } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";

export function NgoApply() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [form, setForm] = useState({
    organizationName: "",
    officialEmail: "",
    officialPhone: "",
    registrationNumber: "",
    registrationDetails: "",
    website: "",
    address: "",
    operatingAreas: "",
    mission: "",
    areasOfFocus: "",
    communitiesServed: "",
    contactPersonName: "",
    contactPersonEmail: "",
    contactPersonPhone: "",
  });

  const update = (field: string, value: string) => setForm((f) => ({ ...f, [field]: value }));

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!isAuthenticated) {
      setError("Please sign in first to submit an NGO application.");
      return;
    }

    if (!form.organizationName || !form.officialEmail) {
      setError("Organization name and official email are required.");
      return;
    }

    setLoading(true);
    try {
      await api.post("/api/v1/ngo/apply", form);
      setSubmitted(true);
      toast("success", "Application submitted!", "Your NGO application has been submitted for admin review.");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to submit application. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (submitted) {
    return (
      <div className="min-h-screen bg-canvas">
        <div className="container-page py-12">
          <Card className="mx-auto max-w-lg text-center">
            <div className="px-6 py-12">
              <CheckCircle2 className="mx-auto h-16 w-16 text-emerald-500" />
              <h1 className="mt-4 text-2xl font-bold text-ink">Application Submitted!</h1>
              <p className="mt-2 text-muted">
                Your NGO application has been submitted for admin review. You&apos;ll be notified once your application is approved.
              </p>
              <Button className="mt-6" onClick={() => navigate("/")} icon={<ArrowLeft className="h-4 w-4" />}>
                Back to Home
              </Button>
            </div>
          </Card>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-canvas">
      <div className="container-page py-8">
        <Link to="/" className="inline-flex items-center gap-1.5 text-sm font-medium text-muted hover:text-ink mb-6">
          <ArrowLeft className="h-4 w-4" /> Back to Home
        </Link>

        <div className="mx-auto max-w-2xl">
          <div className="flex items-center gap-3 mb-2">
            <HeartHandshake className="h-6 w-6 text-emerald-600" />
            <span className="text-xs font-semibold uppercase tracking-wider text-emerald-600">NGO Application</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-ink sm:text-3xl">Apply as an NGO</h1>
          <p className="mt-2 text-muted">
            Submit your organization details for verification. Once approved by an administrator, you&apos;ll gain access to the NGO dashboard.
          </p>

          {!isAuthenticated && (
            <div className="mt-4 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
              You need to <Link to="/login" className="font-semibold underline">sign in</Link> before submitting an application.
            </div>
          )}

          <form onSubmit={handleSubmit} className="mt-8 space-y-6">
            {error && (
              <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                {error}
              </div>
            )}

            <Card className="p-6">
              <h2 className="text-lg font-semibold text-ink mb-4">Organization Details</h2>
              <div className="grid gap-5 sm:grid-cols-2">
                <Field label="Organization Name" required className="sm:col-span-2">
                  <Input value={form.organizationName} onChange={(e) => update("organizationName", e.target.value)} placeholder="e.g. Green Hyderabad Trust" />
                </Field>
                <Field label="Official Email" required>
                  <Input type="email" value={form.officialEmail} onChange={(e) => update("officialEmail", e.target.value)} placeholder="contact@ngo.org" />
                </Field>
                <Field label="Official Phone">
                  <Input value={form.officialPhone} onChange={(e) => update("officialPhone", e.target.value)} placeholder="9876543210" />
                </Field>
                <Field label="Registration Number">
                  <Input value={form.registrationNumber} onChange={(e) => update("registrationNumber", e.target.value)} placeholder="NGO/2024/12345" />
                </Field>
                <Field label="Website">
                  <Input value={form.website} onChange={(e) => update("website", e.target.value)} placeholder="https://ngo.org" />
                </Field>
                <Field label="Address" className="sm:col-span-2">
                  <Input value={form.address} onChange={(e) => update("address", e.target.value)} placeholder="Banjara Hills, Hyderabad" />
                </Field>
                <Field label="Registration Details" className="sm:col-span-2">
                  <Textarea rows={2} value={form.registrationDetails} onChange={(e) => update("registrationDetails", e.target.value)} placeholder="Additional registration information..." />
                </Field>
              </div>
            </Card>

            <Card className="p-6">
              <h2 className="text-lg font-semibold text-ink mb-4">Mission & Focus</h2>
              <div className="grid gap-5">
                <Field label="Mission Statement">
                  <Textarea rows={3} value={form.mission} onChange={(e) => update("mission", e.target.value)} placeholder="Describe your organization's mission..." />
                </Field>
                <div className="grid gap-5 sm:grid-cols-2">
                  <Field label="Areas of Focus">
                    <Input value={form.areasOfFocus} onChange={(e) => update("areasOfFocus", e.target.value)} placeholder="Water conservation, Sewage awareness" />
                  </Field>
                  <Field label="Operating Areas">
                    <Input value={form.operatingAreas} onChange={(e) => update("operatingAreas", e.target.value)} placeholder="Miyapur, Kondapur" />
                  </Field>
                </div>
                <Field label="Communities Served">
                  <Input value={form.communitiesServed} onChange={(e) => update("communitiesServed", e.target.value)} placeholder="Local resident welfare associations" />
                </Field>
              </div>
            </Card>

            <Card className="p-6">
              <h2 className="text-lg font-semibold text-ink mb-4">Contact Person</h2>
              <div className="grid gap-5 sm:grid-cols-2">
                <Field label="Name" required>
                  <Input value={form.contactPersonName} onChange={(e) => update("contactPersonName", e.target.value)} placeholder="Full name" />
                </Field>
                <Field label="Email" required>
                  <Input type="email" value={form.contactPersonEmail} onChange={(e) => update("contactPersonEmail", e.target.value)} placeholder="person@ngo.org" />
                </Field>
                <Field label="Phone">
                  <Input value={form.contactPersonPhone} onChange={(e) => update("contactPersonPhone", e.target.value)} placeholder="9876543210" />
                </Field>
              </div>
            </Card>

            <Button type="submit" size="lg" fullWidth loading={loading} disabled={!isAuthenticated} icon={<Send className="h-4 w-4" />}>
              Submit Application
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}
