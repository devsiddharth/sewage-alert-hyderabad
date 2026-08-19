import { useCallback, useEffect, useState } from "react";
import {
  Award, Calendar, IndianRupee, Truck, Users, TrendingUp, AlertCircle,
} from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";

import type { NgoDashboard } from "@/types";

function StatCard({ icon, label, value, color }: { icon: React.ReactNode; label: string; value: string | number; color: string }) {
  return (
    <Card className="p-5">
      <div className="flex items-center gap-4">
        <span className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${color}`}>
          {icon}
        </span>
        <div>
          <p className="text-2xl font-bold text-ink">{value}</p>
          <p className="text-sm text-muted">{label}</p>
        </div>
      </div>
    </Card>
  );
}

export function NgoDashboardPage() {
  const [data, setData] = useState<NgoDashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<NgoDashboard>("/api/v1/ngo/dashboard");
      setData(res);
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) {
        setError("Your NGO application has not been approved yet. Please wait for admin verification.");
      } else {
        setError(err instanceof ApiError ? err.message : "Failed to load dashboard.");
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  if (loading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-64 rounded-xl" />
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[0, 1, 2, 3].map((i) => <Skeleton key={i} className="h-24 rounded-2xl" />)}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <Card className="mx-auto max-w-lg text-center">
        <div className="px-6 py-12">
          <AlertCircle className="mx-auto h-12 w-12 text-amber-400" />
          <h2 className="mt-4 text-lg font-semibold text-ink">Unable to load dashboard</h2>
          <p className="mt-2 text-sm text-muted">{error}</p>
        </div>
      </Card>
    );
  }

  if (!data) return null;

  const org = data.organization;

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink">{org.organizationName}</h1>
        <p className="mt-1 text-muted">{org.mission || "NGO Dashboard Overview"}</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          icon={<Calendar className="h-5 w-5 text-blue-600" />}
          label="Total Events"
          value={data.totalEvents}
          color="bg-blue-50"
        />
        <StatCard
          icon={<Truck className="h-5 w-5 text-emerald-600" />}
          label="Total Drives"
          value={data.totalDrives}
          color="bg-emerald-50"
        />
        <StatCard
          icon={<Award className="h-5 w-5 text-amber-600" />}
          label="Achievements"
          value={data.totalAchievements}
          color="bg-amber-50"
        />
        <StatCard
          icon={<Users className="h-5 w-5 text-purple-600" />}
          label="Participants"
          value={data.totalParticipants}
          color="bg-purple-50"
        />
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <StatCard
          icon={<IndianRupee className="h-5 w-5 text-green-600" />}
          label="Funds Received"
          value={`₹${(data.totalFundsReceived ?? 0).toLocaleString("en-IN")}`}
          color="bg-green-50"
        />
        <StatCard
          icon={<IndianRupee className="h-5 w-5 text-red-500" />}
          label="Total Expenses"
          value={`₹${(data.totalExpenses ?? 0).toLocaleString("en-IN")}`}
          color="bg-red-50"
        />
        <StatCard
          icon={<TrendingUp className="h-5 w-5 text-cyan-600" />}
          label="Remaining Balance"
          value={`₹${(data.remainingBalance ?? 0).toLocaleString("en-IN")}`}
          color="bg-cyan-50"
        />
      </div>

      {/* Events summary */}
      <div className="grid gap-4 sm:grid-cols-2">
        <Card className="p-5">
          <h3 className="text-sm font-semibold text-muted uppercase tracking-wider">Events</h3>
          <div className="mt-3 space-y-2 text-sm">
            <div className="flex justify-between"><span className="text-muted">Pending Approval</span><Badge tone="slate">{data.pendingEvents}</Badge></div>
            <div className="flex justify-between"><span className="text-muted">Published</span><Badge tone="green">{data.publishedEvents}</Badge></div>
          </div>
        </Card>
        <Card className="p-5">
          <h3 className="text-sm font-semibold text-muted uppercase tracking-wider">Progress</h3>
          {data.progress ? (
            <div className="mt-3 space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-muted">Events Conducted</span><span className="font-medium text-ink">{data.progress.eventsConducted}</span></div>
              <div className="flex justify-between"><span className="text-muted">Drives Conducted</span><span className="font-medium text-ink">{data.progress.drivesConducted}</span></div>
              <div className="flex justify-between"><span className="text-muted">People Reached</span><span className="font-medium text-ink">{data.progress.peopleReached.toLocaleString()}</span></div>
              <div className="flex justify-between"><span className="text-muted">Volunteers</span><span className="font-medium text-ink">{data.progress.volunteersInvolved}</span></div>
            </div>
          ) : (
            <p className="mt-3 text-sm text-muted">No progress data yet.</p>
          )}
        </Card>
      </div>

      {/* Quick info */}
      <Card className="p-5">
        <h3 className="text-sm font-semibold text-muted uppercase tracking-wider">Organization Info</h3>
        <div className="mt-3 grid gap-3 sm:grid-cols-2 text-sm">
          <div><span className="text-muted">Email:</span> <span className="font-medium text-ink">{org.officialEmail}</span></div>
          <div><span className="text-muted">Phone:</span> <span className="font-medium text-ink">{org.officialPhone || "—"}</span></div>
          <div><span className="text-muted">Registration:</span> <span className="font-medium text-ink">{org.registrationNumber || "—"}</span></div>
          <div><span className="text-muted">Status:</span> <Badge tone={org.status === "APPROVED" ? "green" : org.status === "REJECTED" ? "red" : "slate"}>{org.status}</Badge></div>
          <div className="sm:col-span-2"><span className="text-muted">Operating Areas:</span> <span className="font-medium text-ink">{org.operatingAreas || "—"}</span></div>
          <div className="sm:col-span-2"><span className="text-muted">Areas of Focus:</span> <span className="font-medium text-ink">{org.areasOfFocus || "—"}</span></div>
        </div>
      </Card>
    </div>
  );
}
