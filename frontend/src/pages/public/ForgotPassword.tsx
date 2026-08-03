import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, MailCheck, Send } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Field";

// NOTE: The backend does not expose a password-reset endpoint yet. The form
// follows best practice (never reveal whether an account exists) and the
// success state explains the next step. Wire this to a real endpoint when the
// notification service ships.
export function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      setError("Please enter a valid email address.");
      return;
    }
    setSent(true);
  };

  return (
    <AuthLayout>
      <Link to="/login" className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted transition-colors hover:text-brand">
        <ArrowLeft className="h-4 w-4" /> Back to sign in
      </Link>

      {sent ? (
        <div className="mt-8 animate-fade-in">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-success-soft text-emerald-600">
            <MailCheck className="h-7 w-7" />
          </div>
          <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Check your inbox</h1>
          <p className="mt-3 max-w-md leading-relaxed text-muted">
            If an account exists for <span className="font-semibold text-ink">{email}</span>, we&apos;ve
            sent instructions to reset your password. The link expires in 30 minutes.
          </p>
          <div className="mt-8 rounded-xl border border-line bg-canvas px-5 py-4 text-sm text-muted">
            Didn&apos;t receive an email? Call the helpline at{" "}
            <a href="tel:04023456789" className="font-semibold text-brand hover:underline">
              040-2345 6789
            </a>{" "}
            and a support agent will reset your password manually.
          </div>
        </div>
      ) : (
        <>
          <h1 className="mt-8 text-3xl font-bold tracking-tight text-ink">Reset your password</h1>
          <p className="mt-2 text-muted">
            Enter the email linked to your account and we&apos;ll send you a reset link.
          </p>
          <form onSubmit={handleSubmit} className="mt-8 space-y-5" noValidate>
            {error && (
              <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700 animate-fade-in">
                {error}
              </div>
            )}
            <Field label="Email address" required>
              <Input
                type="email"
                autoComplete="email"
                placeholder="you@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </Field>
            <Button type="submit" size="lg" fullWidth icon={<Send className="h-4 w-4" />}>
              Send reset link
            </Button>
          </form>
        </>
      )}
    </AuthLayout>
  );
}
