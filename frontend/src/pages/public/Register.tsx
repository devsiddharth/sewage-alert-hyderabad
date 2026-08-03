import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, UserPlus } from "lucide-react";
import { AuthLayout } from "@/components/layout/AuthLayout";
import { Button } from "@/components/ui/Button";
import { Field, Input } from "@/components/ui/Field";
import { useAuth } from "@/lib/auth";
import { ApiError } from "@/lib/api";

export function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (name.trim().length < 2) return setError("Please enter your full name.");
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return setError("Please enter a valid email address.");
    if (password.length < 6) return setError("Password must be at least 6 characters.");
    if (password !== confirm) return setError("Passwords do not match.");

    setLoading(true);
    try {
      const auth = await register({
        name: name.trim(),
        email: email.trim(),
        password,
        phone: phone ? Number(phone) : null,
      });
      navigate(auth.role === "ADMIN" || auth.role === "AUTHORITY" ? "/admin" : "/dashboard", {
        replace: true,
      });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Unable to create your account. Please try again.");
    } finally {
      setLoading(false);
    }
  };

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
