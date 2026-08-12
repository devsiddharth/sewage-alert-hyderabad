import {
  useCallback,
  useEffect,
  useRef,
  useState,
  type ClipboardEvent,
  type FormEvent,
  type KeyboardEvent,
} from "react";
import { Link } from "react-router-dom";
import { ArrowRight, CheckCircle2, MailCheck, ShieldCheck, UserPlus } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";

// Resend cooldown — mirrors the backend throttle (one verification email per 60s).
const RESEND_COOLDOWN_SECONDS = 60;
const OTP_LENGTH = 6;

type VerifyStep = "form" | "code" | "success";

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

  // Inline verification state — the 6-digit code is asked right inside registration,
  // instead of relying only on an emailed link the user has to open later.
  const [verifyStep, setVerifyStep] = useState<VerifyStep>("form");
  const [registeredEmail, setRegisteredEmail] = useState<string | null>(null);
  const [code, setCode] = useState("");
  const [codeError, setCodeError] = useState<string | null>(null);
  const [verifying, setVerifying] = useState(false);
  const [resending, setResending] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const otpRefs = useRef<(HTMLInputElement | null)[]>([]);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    return () => {
      if (timerRef.current) window.clearInterval(timerRef.current);
    };
  }, []);

  // Focus the first OTP box as soon as the code step appears.
  useEffect(() => {
    if (verifyStep === "code") {
      const t = window.setTimeout(() => otpRefs.current[0]?.focus(), 50);
      return () => window.clearTimeout(t);
    }
  }, [verifyStep]);

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
      setVerifyStep("code");
      startCooldown();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Unable to create your account. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleOtpChange = (index: number, value: string) => {
    const digit = value.replace(/\D/g, "").slice(-1);
    const next = code.split("");
    next[index] = digit;
    setCode(next.join("").slice(0, OTP_LENGTH));
    setCodeError(null);
    if (digit && index < OTP_LENGTH - 1) otpRefs.current[index + 1]?.focus();
  };

  const handleOtpKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      otpRefs.current[index - 1]?.focus();
    }
  };

  const handleOtpPaste = (e: ClipboardEvent<HTMLInputElement>) => {
    const text = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, OTP_LENGTH);
    if (!text) return;
    e.preventDefault();
    setCode(text);
    setCodeError(null);
    otpRefs.current[Math.min(text.length, OTP_LENGTH - 1)]?.focus();
  };

  const handleVerify = async (e: FormEvent) => {
    e.preventDefault();
    if (!registeredEmail || verifying) return;
    if (code.length !== OTP_LENGTH) {
      setCodeError("Enter the 6-digit code from the email.");
      return;
    }
    setVerifying(true);
    setCodeError(null);
    try {
      await api.post<null>("/api/v1/auth/verify-code", { email: registeredEmail, code });
      toast("success", "Email verified", "Your account is now active. Welcome to Sewage Alert Hyderabad!");
      setVerifyStep("success");
    } catch (err) {
      setCodeError(err instanceof ApiError ? err.message : "Unable to verify the code. Please try again.");
    } finally {
      setVerifying(false);
    }
  };

  const handleResend = async () => {
    if (!registeredEmail || resending || cooldown > 0) return;
    setResending(true);
    try {
      await api.post<null>("/api/v1/auth/resend-verification", { email: registeredEmail });
      toast("success", "Verification email sent", "Check your inbox — the code and link expire in 30 minutes.");
      setCode("");
      setCodeError(null);
      startCooldown();
    } catch (err) {
      toast("error", "Could not resend", err instanceof ApiError ? err.message : "Please try again later.");
    } finally {
      setResending(false);
    }
  };

  // ── Verified — account active ─────────────────────────────────────────────
  if (verifyStep === "success") {
    return (
      <AuthLayout>
        <div className="animate-fade-in">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
            <CheckCircle2 className="h-7 w-7" />
          </div>
          <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">You&apos;re all set!</h1>
          <p className="mt-3 max-w-md leading-relaxed text-muted">
            Your email{" "}
            <span className="font-semibold text-ink">{registeredEmail}</span> has been verified and your
            account is now active. Sign in to start reporting issues.
          </p>
          <Link
            to="/login"
            className="mt-8 inline-flex w-full items-center justify-center gap-1.5 rounded-xl bg-brand px-4 py-2.5 text-sm font-semibold text-white shadow-lift transition-all hover:bg-brand-dark"
          >
            Go to login
            <ArrowRight className="h-4 w-4" aria-hidden />
          </Link>
        </div>
      </AuthLayout>
    );
  }

  // ── Verification-code step (asked during registration itself) ─────────────
  if (verifyStep === "code") {
    return (
      <AuthLayout>
        <div className="animate-fade-in">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-accent-soft text-brand">
            <ShieldCheck className="h-7 w-7" />
          </div>
          <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Verify your email</h1>
          <p className="mt-3 max-w-md leading-relaxed text-muted">
            We sent a <span className="font-semibold text-ink">6-digit verification code</span> to{" "}
            <span className="font-semibold text-ink">{registeredEmail}</span>. Enter it below to
            activate your account — it expires in 30 minutes.
          </p>

          <form onSubmit={handleVerify} className="mt-8 space-y-5" noValidate>
            {codeError && (
              <div
                role="alert"
                className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700 animate-fade-in"
              >
                {codeError}
              </div>
            )}

            <div className="flex justify-between gap-2 sm:gap-3">
              {Array.from({ length: OTP_LENGTH }).map((_, index) => (
                <input
                  key={index}
                  ref={(el) => {
                    otpRefs.current[index] = el;
                  }}
                  value={code[index] ?? ""}
                  onChange={(e) => handleOtpChange(index, e.target.value)}
                  onKeyDown={(e) => handleOtpKeyDown(index, e)}
                  onPaste={handleOtpPaste}
                  inputMode="numeric"
                  autoComplete={index === 0 ? "one-time-code" : "off"}
                  maxLength={1}
                  aria-label={`Digit ${index + 1} of verification code`}
                  className="h-14 w-11 rounded-xl border border-line bg-white text-center text-2xl font-bold text-ink outline-none transition-all focus:border-brand focus:ring-4 focus:ring-brand/15 sm:h-16 sm:w-14"
                />
              ))}
            </div>

            <Button type="submit" size="lg" fullWidth loading={verifying} icon={<ShieldCheck className="h-4 w-4" />}>
              Verify &amp; continue
            </Button>

            <p className="text-center text-sm text-muted">
              Didn&apos;t get the code?{" "}
              <button
                type="button"
                onClick={handleResend}
                disabled={resending || verifying || cooldown > 0}
                className="font-semibold text-brand transition-colors hover:underline disabled:cursor-not-allowed disabled:text-muted"
              >
                {resending
                  ? "Sending…"
                  : cooldown > 0
                    ? `Resend in ${cooldown}s`
                    : "Resend code"}
              </button>
              <span className="mx-1.5 text-line">•</span>
              <Link to="/login" className="font-semibold text-muted transition-colors hover:text-brand hover:underline">
                Verify later
              </Link>
            </p>
          </form>

          <p className="mt-6 rounded-xl border border-line bg-canvas px-4 py-3 text-sm leading-relaxed text-muted">
            <MailCheck className="mr-1.5 inline h-4 w-4 text-brand" aria-hidden />
            Check your spam / promotions folder too — the email is also sent from the EmailJS
            service address.
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

        <p className="flex items-start gap-2 text-xs leading-relaxed text-muted">
          <ShieldCheck className="mt-0.5 h-4 w-4 shrink-0 text-brand" aria-hidden />
          After creating your account we&apos;ll ask you for a 6-digit code emailed to you — this
          verifies your email before you can sign in.
        </p>

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
