import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ArrowLeft, ArrowRight, CheckCircle2, Loader2, MailCheck, RefreshCw, XCircle } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Field";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";

type VerifyState = "loading" | "success" | "error";

export function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const { toast } = useToast();

  const [state, setState] = useState<VerifyState>("loading");
  const [message, setMessage] = useState<string | null>(null);
  const [resendEmail, setResendEmail] = useState("");
  const [resending, setResending] = useState(false);
  const [cooldown, setCooldown] = useState(0);
  const ranRef = useRef(false);
  const timerRef = useRef<number | null>(null);

  useEffect(() => {
    return () => {
      if (timerRef.current) window.clearInterval(timerRef.current);
    };
  }, []);

  useEffect(() => {
    if (ranRef.current) return;
    ranRef.current = true;

    // Strip the one-time token from the address bar so it can't be re-shared via URL,
    // history, or referrer headers after the verification attempt.
    window.history.replaceState(null, "", "/verify-email");

    const verify = async () => {
      if (!token) {
        setState("error");
        setMessage("The verification link is invalid or has expired.");
        return;
      }
      try {
        await api.get<null>(`/api/v1/auth/verify-email?token=${encodeURIComponent(token)}`);
        setState("success");
      } catch (err) {
        setState("error");
        setMessage(
          err instanceof ApiError ? err.message : "The verification link is invalid or has expired."
        );
      }
    };
    void verify();
  }, [token]);

  const handleResend = async (e: FormEvent) => {
    e.preventDefault();
    if (resending || cooldown > 0) return;
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(resendEmail)) {
      setMessage("Please enter a valid email address.");
      return;
    }
    setResending(true);
    try {
      await api.post<null>("/api/v1/auth/resend-verification", { email: resendEmail.trim() });
      toast("success", "Verification email sent", "Check your inbox — the link expires in 30 minutes.");
      setCooldown(60);
      timerRef.current = window.setInterval(() => {
        setCooldown((prev) => {
          if (prev <= 1) {
            if (timerRef.current) window.clearInterval(timerRef.current);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err) {
      toast("error", "Could not resend", err instanceof ApiError ? err.message : "Please try again later.");
    } finally {
      setResending(false);
    }
  };

  return (
    <AuthLayout>
      <Link to="/login" className="inline-flex items-center gap-1.5 text-sm font-semibold text-muted transition-colors hover:text-brand">
        <ArrowLeft className="h-4 w-4" /> Back to sign in
      </Link>

      <div className="mt-8 animate-fade-in">
        {state === "loading" && (
          <div className="flex flex-col items-start">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-accent-soft text-brand">
              <Loader2 className="h-7 w-7 animate-spin" />
            </div>
            <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Email Verification</h1>
            <p className="mt-3 text-muted">Verifying your email address, please wait…</p>
          </div>
        )}

        {state === "success" && (
          <div>
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-emerald-50 text-emerald-600">
              <CheckCircle2 className="h-7 w-7" />
            </div>
            <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Email Verification</h1>
            <p className="mt-3 max-w-md leading-relaxed text-muted">
              ✓ Your email has been verified successfully.
            </p>
            <p className="mt-1 max-w-md leading-relaxed text-muted">
              Your Sewage Alert Hyderabad account is now active. You can sign in and start reporting issues.
            </p>
            <Link
              to="/login"
              className="mt-8 inline-flex items-center gap-1.5 rounded-xl bg-brand px-5 py-2.5 text-sm font-semibold text-white shadow-lift transition-all hover:bg-brand-dark"
            >
              Go to Login
              <ArrowRight className="h-4 w-4" aria-hidden />
            </Link>
          </div>
        )}

        {state === "error" && (
          <div>
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-red-50 text-red-600">
              <XCircle className="h-7 w-7" />
            </div>
            <h1 className="mt-6 text-3xl font-bold tracking-tight text-ink">Email Verification</h1>
            <p className="mt-3 max-w-md leading-relaxed text-muted">{message}</p>

            <form onSubmit={handleResend} className="mt-8 space-y-4">
              <Field label="Your email address" hint="We'll send you a fresh verification link.">
                <Input
                  type="email"
                  autoComplete="email"
                  placeholder="you@example.com"
                  value={resendEmail}
                  onChange={(e) => setResendEmail(e.target.value)}
                />
              </Field>
              <div className="flex flex-col gap-3 sm:flex-row">
                <Button
                  type="submit"
                  disabled={resending || cooldown > 0}
                  icon={<RefreshCw className="h-4 w-4" />}
                >
                  {resending
                    ? "Sending…"
                    : cooldown > 0
                      ? `Resend available in ${cooldown}s`
                      : "Resend verification email"}
                </Button>
                <Link
                  to="/login"
                  className="inline-flex items-center justify-center gap-1.5 rounded-xl border border-line bg-white px-4 py-2.5 text-sm font-semibold text-ink transition-colors hover:bg-canvas"
                >
                  Go to Login
                  <ArrowRight className="h-4 w-4" aria-hidden />
                </Link>
              </div>
            </form>

            <p className="mt-8 text-sm text-muted">
              Didn&apos;t receive the first email?{" "}
              <span className="inline-flex items-center gap-1.5 align-middle">
                <MailCheck className="h-4 w-4 text-brand" aria-hidden />
                Check your spam folder too.
              </span>
            </p>
          </div>
        )}
      </div>
    </AuthLayout>
  );
}
