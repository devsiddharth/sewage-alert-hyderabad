import { useCallback, useEffect, useRef, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, MailCheck, RefreshCw, UserPlus } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";

// Resend cooldown — mirrors the backend throttle (one verification email per 60s).
const RESEND_COOLDOWN_SECONDS = 60;

export function Register() {
  const { register } = useAuth();
  const { toast } = useToast();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  // Verification-pending state shown after a successful registration
  const [registeredEmail, setRegisteredEmail] = useState<string | null>(null);
  const [resending, setResending] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    return () => {
      if (timerRef.current) window.clearInterval(timerRef.current);
    };
  }, []);

  const startCooldown = useCallback(() => {
    setCooldown(RESEND_COOLDOWN_SECONDS);
    timerRef.current = window.setInterval(() => {
      setCooldown((prev) => {
        if (prev <= 1) {
          if (timerRef.current) window.clearInterval(timerRef.current);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (name.trim().length < 2) return setError("Please enter your full name.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return setError("Please enter a valid email address.");
    if (password.length < 6) return setError("Password must be at least 6 characters.");
    if (password !== confirm) return setError("Passwords do not match.");

    setLoading(true);
    try {
      await register({
        name: name.trim(),
        email: email.trim(),
        password,
        phone: phone ? Number(phone) : null,
      });
      setRegisteredEmail(email.trim());
      startCooldown();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Unable to create your account. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!registeredEmail || resending || cooldown > 0) return;
    setResending(true);
    try {
      await api.post<null>("/api/v1/auth/resend-verification", { email: registeredEmail });
      toast("success", "Verification email sent", "Check your inbox — the link expires in 30 minutes.");
      startCooldown();
    } catch (err) {
      toast("error", "Could not resend", err instanceof ApiError ? err.message : "Please try again later.");
    } finally {
      setResending(false);
    }
  };

  // ── Verification-pending state ────────────────────────────────────────────
  if (registeredEmail) {
    return (
      <AuthLayout>
        <div className="animate-fade-in">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
            <MailCheck className="h-7 w-7" />
          </div>
          <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Registration successful!</h1>
          <p className="mt-3 max-w-md leading-relaxed text-muted">
            We&apos;ve sent a verification email to{" "}
            <span className="font-semibold text-ink">{registeredEmail}</span>. Please verify your
            email address before logging in — your account becomes active after verification.
          </p>

          <div className="mt-8 space-y-3">
            <Button
              size="lg"
              fullWidth
              onClick={handleResend}
              disabled={resending || cooldown > 0}
              icon={<RefreshCw className="h-4 w-4" />}
              variant={cooldown > 0 ? "outline" : "primary"}
            >
              {resending
                ? "Sending…"
                : cooldown > 0
                  ? `Resend available in ${cooldown}s`
                  : "Resend verification email"}
            </Button>
            <Link
              to="/login"
              className="inline-flex w-full items-center justify-center gap-1.5 rounded-xl border border-line bg-white px-4 py-2.5 text-sm font-semibold text-ink transition-colors hover:bg-canvas"
            >
              Go to login
              <ArrowRight className="h-4 w-4" aria-hidden />
            </Link>
          </div>

          <p className="mt-8 text-center text-sm text-muted">
            Didn&apos;t get the email? Check your spam folder, or try resending in a minute.
          </p>
        </div>
      </AuthLayout>
    );
  }

  // ── Registration form ─────────────────────────────────────────────────────
  return (
    <AuthLayout>
      <h1 className="text-3xl font-bold tracking-tight text-ink">Create your account</h1>
      <p className="mt-2 text-muted">Join thousands of citizens keeping Hyderabad clean.</p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-5" noValidate>
        {error && (
          <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700 animate-fade-in">
            {error}
          </div>
        )}
        <Field label="Full name" required>
          <Input
            autoComplete="name"
            placeholder="Your full name"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </Field>
        <Field label="Email address" required>
          <Input
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </Field>
        <Field label="Phone number (optional)" hint="Used by authorities to reach you if needed.">
          <Input
            type="tel"
            autoComplete="tel"
            inputMode="numeric"
            placeholder="98765 43210"
            value={phone}
            onChange={(e) => setPhone(e.target.value.replace(/\D/g, "").slice(0, 10))}
          />
        </Field>
        <div className="grid gap-5 sm:grid-cols-2">
          <Field label="Password" required>
            <Input
              type="password"
              autoComplete="new-password"
              placeholder="Min. 6 characters"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </Field>
          <Field label="Confirm password" required>
            <Input
              type="password"
              autoComplete="new-password"
              placeholder="Repeat password"
              value={confirm}
              onChange={(e) => setConfirm(e.target.value)}
            />
          </Field>
        </div>

        <Button type="submit" size="lg" fullWidth loading={loading} icon={<UserPlus className="h-4 w-4" />}>
          Create account
        </Button>
      </form>

      <p className="mt-8 text-center text-sm text-muted">
        Already have an account?{" "}
        <Link to="/login" className="font-semibold text-brand hover:underline">
          Sign in
          <ArrowRight className="ml-0.5 inline h-3.5 w-3.5" aria-hidden />
        </Link>
      </p>
    </AuthLayout>
  );
}
