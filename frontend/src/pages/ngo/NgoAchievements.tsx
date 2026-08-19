import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Award, Plus, Trash2, CheckCircle2 } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { formatDate } from "@/lib/utils";
import type { NgoAchievement } from "@/types";

export function NgoAchievements() {
  const { toast } = useToast();
  const [items, setItems] = useState<NgoAchievement[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ title: "", description: "", date: "", evidence: "" });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get<NgoAchievement[]>("/api/v1/ngo/achievements");
      setItems(res);
    } catch { /* silent */ } finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const update = (field: string, value: string) => setForm((f) => ({ ...f, [field]: value }));

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    if (!form.title) { toast("error", "Missing", "Title is required."); return; }
    setCreating(true);
    try {
      await api.post("/api/v1/ngo/achievements", {
        title: form.title,
        description: form.description || null,
        date: form.date || null,
        evidence: form.evidence || null,
      });
      toast("success", "Achievement added!", "");
      setShowCreate(false);
      setForm({ title: "", description: "", date: "", evidence: "" });
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not add achievement.");
    } finally { setCreating(false); }
  };

  const handleDelete = async (id: number) => {
    if (!confirm("Delete this achievement?")) return;
    try {
      await api.del(`/api/v1/ngo/achievements/${id}`);
      toast("success", "Deleted", "");
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not delete.");
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink">Achievements</h1>
          <p className="mt-1 text-muted">Document your NGO's accomplishments and milestones.</p>
        </div>
        <Button onClick={() => setShowCreate(true)} icon={<Plus className="h-4 w-4" />}>Add Achievement</Button>
      </div>

      {loading ? (
        <div className="grid gap-4 sm:grid-cols-2">{[0, 1, 2].map((i) => <Skeleton key={i} className="h-32 rounded-2xl" />)}</div>
      ) : items.length === 0 ? (
        <Card className="p-12 text-center">
          <Award className="mx-auto h-10 w-10 text-muted/40" />
          <h3 className="mt-3 text-base font-semibold text-ink">No achievements yet</h3>
          <p className="mt-1 text-sm text-muted">Start documenting your accomplishments.</p>
        </Card>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {items.map((a) => (
            <Card key={a.id} className="p-5 hover:shadow-lift transition-all">
              <div className="flex items-start justify-between">
                <h3 className="text-[15px] font-semibold text-ink">{a.title}</h3>
                <Button variant="outline" size="sm" onClick={() => handleDelete(a.id)} icon={<Trash2 className="h-3.5 w-3.5" />}>
                  <span className="sr-only">Delete</span>
                </Button>
              </div>
              {a.description && <p className="mt-2 text-sm text-muted">{a.description}</p>}
              <div className="mt-3 flex flex-wrap gap-3 text-xs text-muted">
                {a.date && <span>{formatDate(a.date)}</span>}
                {a.evidence && <span className="text-brand">{a.evidence}</span>}
              </div>
            </Card>
          ))}
        </div>
      )}

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="Add Achievement">
          <form onSubmit={handleCreate} className="space-y-4">
            <Field label="Title" required>
              <Input value={form.title} onChange={(e) => update("title", e.target.value)} placeholder="e.g. 100 Trees Planted" />
            </Field>
            <Field label="Description">
              <Textarea rows={3} value={form.description} onChange={(e) => update("description", e.target.value)} />
            </Field>
            <div className="grid gap-4 sm:grid-cols-2">
              <Field label="Date">
                <Input type="date" value={form.date} onChange={(e) => update("date", e.target.value)} />
              </Field>
              <Field label="Evidence / Source">
                <Input value={form.evidence} onChange={(e) => update("evidence", e.target.value)} placeholder="Link or reference" />
              </Field>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" type="button" onClick={() => setShowCreate(false)}>Cancel</Button>
              <Button type="submit" loading={creating} icon={<CheckCircle2 className="h-4 w-4" />}>Save</Button>
            </div>
          </form>
      </Modal>
    </div>
  );
}
