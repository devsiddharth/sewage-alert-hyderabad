import { useCallback, useEffect, useState } from "react";
import { BarChart3, Calendar, Map, Target, Truck, Users, Heart } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Skeleton } from "@/components/ui/States";
import { api } from "@/lib/api";
import type { NgoProgress } from "@/types";

function MetricCard({ icon, label, value, color }: { icon: React.ReactNode; label: string; value: number; color: string }) {
  return (
    <Card className="p-5">
      <div className="flex items-center gap-4">
        <span className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl ${color}`}>{icon}</span>
        <div>
          <p className="text-2xl font-bold text-ink">{value.toLocaleString()}</p>
          <p className="text-sm text-muted">{label}</p>
        </div>
      </div>
    </Card>
  );
}

export function NgoProgressPage() {
  const [progress, setProgress] = useState<NgoProgress | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<NgoProgress>("/api/v1/ngo/progress");
      setProgress(res);
    } catch { /* silent */ } finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  if (loading) return <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">{[0, 1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-24 rounded-2xl" />)}</div>;

  if (!progress) {
    return (
      <div className="space-y-6 animate-fade-in">
        <h1 className="text-2xl font-bold tracking-tight text-ink">Progress</h1>
        <Card className="p-12 text-center">
          <BarChart3 className="mx-auto h-10 w-10 text-muted/40" />
          <h3 className="mt-3 text-base font-semibold text-ink">No progress data</h3>
          <p className="mt-1 text-sm text-muted">Progress metrics are computed from your events, drives, and activities.</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-ink">Progress</h1>
        <p className="mt-1 text-muted">System-tracked metrics from your organization's activities. These are computed automatically and cannot be manually edited.</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <MetricCard icon={<Target className="h-5 w-5 text-blue-600" />} label="Complaints Addressed" value={progress.complaintsAddressed} color="bg-blue-50" />
        <MetricCard icon={<Map className="h-5 w-5 text-emerald-600" />} label="Areas Covered" value={progress.areasCovered} color="bg-emerald-50" />
        <MetricCard icon={<Truck className="h-5 w-5 text-purple-600" />} label="Drives Conducted" value={progress.drivesConducted} color="bg-purple-50" />
        <MetricCard icon={<Calendar className="h-5 w-5 text-amber-600" />} label="Events Conducted" value={progress.eventsConducted} color="bg-amber-50" />
        <MetricCard icon={<Users className="h-5 w-5 text-cyan-600" />} label="Volunteers Involved" value={progress.volunteersInvolved} color="bg-cyan-50" />
        <MetricCard icon={<Heart className="h-5 w-5 text-red-500" />} label="People Reached" value={progress.peopleReached} color="bg-red-50" />
      </div>
    </div>
  );
}
