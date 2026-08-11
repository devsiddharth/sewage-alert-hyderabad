import { useState, type FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff, LogIn, MailWarning, RefreshCw } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { homePathFor } from "@/lib/utils";

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: string } | null)?.from;

  const { toast } = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [show, setShow] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [emailUnverified, setEmailUnverified] = useState(false);
  const [resending, setResending] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    if (!email.trim() || !password) {
      setError("Please enter your email and password.");
      return;
    }
    setLoading(true);
    setEmailUnverified(false);
    try {
      const auth = await login(email.trim(), password);
      navigate(from ?? homePathFor(auth.role), { replace: true });
    } catch (err) {
      if (err instanceof ApiError && err.code === "EMAIL_NOT_VERIFIED") {
        setEmailUnverified(true);
      }
      setError(err instanceof ApiError ? err.message : "Unable to sign in. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (resending) return;
    setResending(true);
    try {
      await api.post<null>("/api/v1/auth/resend-verification", { email: email.trim() });
      toast("success", "Verification email sent", "Check your inbox — the link expires in 30 minutes.");
    } catch (resendErr) {
      toast(
        "error",
        "Could not resend",
        resendErr instanceof ApiError ? resendErr.message : "Please try again later."
      );
    } finally {
      setResending(false);
    }
  };

  return (
    <AuthLayout>
      <h1 className="text-3xl font-bold tracking-tight text-ink">Welcome back</h1>
      <p className="mt-2 text-muted">Sign in to report issues and track your complaints.</p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-5" noValidate>
        {error && (
          <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700 animate-fade-in">
            {error}
          </div>
        )}
        {emailUnverified && (
          <div className="flex flex-col gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 animate-fade-in">
            <p className="flex items-start gap-2 text-sm font-medium text-amber-800">
              <MailWarning className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
              Your email hasn&apos;t been verified yet. Check your inbox for the verification link,
              or resend it below.
            </p>
            <Button
              type="button"
              size="sm"
              variant="outline"
              onClick={handleResend}
              disabled={resending}
              icon={<RefreshCw className="h-4 w-4" />}
            >
              {resending ? "Sending…" : "Resend verification email"}
            </Button>
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
        <Field label="Password" required>
          <div className="relative">
            <Input
              type={show ? "text" : "password"}
              autoComplete="current-password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="pr-11"
            />
            <button
              type="button"
              onClick={() => setShow((v) => !v)}
              className="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-muted transition-colors hover:text-ink"
              aria-label={show ? "Hide password" : "Show password"}
            >
              {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
        </Field>

        <div className="flex items-center justify-between text-sm">
          <label className="flex items-center gap-2 text-muted">
            <input type="checkbox" name="remember" className="h-4 w-4 rounded border-line text-brand focus:ring-accent" defaultChecked />
            Remember me
          </label>
          <Link to="/forgot-password" className="font-semibold text-brand hover:underline">
            Forgot password?
          </Link>
        </div>

        <Button type="submit" size="lg" fullWidth loading={loading} icon={<LogIn className="h-4 w-4" />}>
          Sign in
        </Button>
      </form>

      <p className="mt-8 text-center text-sm text-muted">
        New to SewageAlert?{" "}
        <Link to="/register" className="font-semibold text-brand hover:underline">
          Create an account
          <ArrowRight className="ml-0.5 inline h-3.5 w-3.5" aria-hidden />
        </Link>
      </p>
    </AuthLayout>
  );
}
