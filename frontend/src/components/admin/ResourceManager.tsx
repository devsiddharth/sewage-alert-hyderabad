import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { Pencil, Plus, Search, Trash2 } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Select, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { EmptyState, ErrorState, Skeleton } from "@/components/ui/States";
import { useToast } from "@/lib/toast";
import { ApiError } from "@/lib/api";
import { cn } from "@/lib/cn";

export interface FieldSchema {
  name: string;
  label: string;
  type: "text" | "textarea" | "number" | "date" | "select";
  options?: string[];
  required?: boolean;
  hint?: string;
  fullWidth?: boolean;
  placeholder?: string;
}

interface ResourceManagerProps<T extends { id: number }> {
  title: string;
  description?: string;
  fetchItems: () => Promise<T[]>;
  createItem: (data: Record<string, unknown>) => Promise<T>;
  updateItem: (id: number, data: Record<string, unknown>) => Promise<T>;
  deleteItem: (id: number) => Promise<void>;
  fields: FieldSchema[];
  searchKeys: (keyof T & string)[];
  renderItem: (item: T) => ReactNode;
  getKey: (item: T) => string;
  initialValues: () => Record<string, unknown>;
  parseBeforeSubmit?: (values: Record<string, unknown>) => Record<string, unknown>;
}

export function ResourceManager<T extends { id: number }>({
  title,
  description,
  fetchItems,
  createItem,
  updateItem,
  deleteItem,
  fields,
  searchKeys,
  renderItem,
  getKey,
  initialValues,
  parseBeforeSubmit,
}: ResourceManagerProps<T>) {
  const { toast } = useToast();
  const [items, setItems] = useState<T[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [query, setQuery] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<T | null>(null);
  const [values, setValues] = useState<Record<string, unknown>>({});
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState<T | null>(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      setItems(await fetchItems());
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load data");
    }
  }, [fetchItems]);

  useEffect(() => {
    void load();
  }, [load]);

  const filtered = useMemo(() => {
    if (!items) return [];
    const q = query.trim().toLowerCase();
    if (!q) return items;
    return items.filter((item) =>
      searchKeys.some((key) => String(item[key] ?? "").toLowerCase().includes(q))
    );
  }, [items, query, searchKeys]);

  const openCreate = () => {
    setEditing(null);
    setValues(initialValues());
    setModalOpen(true);
  };

  const openEdit = (item: T) => {
    setEditing(item);
    const v: Record<string, unknown> = {};
    for (const f of fields) {
      v[f.name] = (item as Record<string, unknown>)[f.name] ?? "";
    }
    setValues(v);
    setModalOpen(true);
  };

  const submit = async () => {
    if (saving) return;
    for (const f of fields) {
      if (f.required && (values[f.name] === "" || values[f.name] == null)) {
        toast("error", "Missing field", `${f.label} is required.`);
        return;
      }
    }
    setSaving(true);
    try {
      const payload = parseBeforeSubmit ? parseBeforeSubmit(values) : values;
      if (editing) {
        await updateItem(editing.id, payload);
        toast("success", "Updated", "Changes saved.");
      } else {
        await createItem(payload);
        toast("success", "Created", "New entry added.");
      }
      setModalOpen(false);
      void load();
    } catch (e) {
      toast("error", "Save failed", e instanceof ApiError ? e.message : undefined);
    } finally {
      setSaving(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleting) return;
    try {
      await deleteItem(deleting.id);
      toast("success", "Deleted");
      setDeleting(null);
      void load();
    } catch (e) {
      toast("error", "Delete failed", e instanceof ApiError ? e.message : undefined);
    }
  };

  return (
    <div className="space-y-5 sm:space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">{title}</h1>
          {description && <p className="mt-1 text-muted">{description}</p>}
        </div>
        <Button onClick={openCreate} icon={<Plus className="h-4 w-4" />}>
          Add new
        </Button>
      </div>

      <Card className="p-3 sm:p-4">
        <div className="relative">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
          <Input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search…"
            className="pl-10"
            aria-label={`Search ${title.toLowerCase()}`}
          />
        </div>
      </Card>

      {error ? (
        <Card>
          <ErrorState message={error} onRetry={() => void load()} />
        </Card>
      ) : items === null ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {[0, 1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="h-40 w-full rounded-2xl" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <Card>
          <EmptyState
            title={query ? "No matches" : `No ${title.toLowerCase()} yet`}
            description={query ? "Try a different search term." : "Add your first entry to get started."}
          />
        </Card>
      ) : (
        <div className="grid gap-3 sm:grid-cols-2 sm:gap-4 lg:grid-cols-3">
          {filtered.map((item) => (
            <Card key={getKey(item)} className="group relative flex flex-col p-4 transition-shadow duration-200 hover:shadow-lift sm:p-5">
              <div className="flex-1">{renderItem(item)}</div>
              <div className="mt-4 flex items-center justify-end gap-2 border-t border-line pt-3">
                <button
                  onClick={() => openEdit(item)}
                  className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium text-muted transition-colors hover:bg-canvas hover:text-brand"
                >
                  <Pencil className="h-3.5 w-3.5" /> Edit
                </button>
                <button
                  onClick={() => setDeleting(item)}
                  className="inline-flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-sm font-medium text-muted transition-colors hover:bg-red-50 hover:text-red-600"
                >
                  <Trash2 className="h-3.5 w-3.5" /> Delete
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Create/Edit modal */}
      <Modal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        title={editing ? `Edit ${title.toLowerCase().replace(/s$/, "")}` : `Add ${title.toLowerCase().replace(/s$/, "")}`}
        description="Fields marked * are required."
        size="lg"
      >
        <div className="grid gap-5 sm:grid-cols-2">
          {fields.map((f) => (
            <div key={f.name} className={cn(f.fullWidth && "sm:col-span-2")}>
              {f.type === "textarea" ? (
                <Field label={f.label} required={f.required} hint={f.hint}>
                  <Textarea
                    rows={3}
                    value={String(values[f.name] ?? "")}
                    onChange={(e) => setValues((v) => ({ ...v, [f.name]: e.target.value }))}
                    placeholder={f.placeholder}
                  />
                </Field>
              ) : f.type === "select" ? (
                <Field label={f.label} required={f.required} hint={f.hint}>
                  <Select
                    value={String(values[f.name] ?? "")}
                    onChange={(e) => setValues((v) => ({ ...v, [f.name]: e.target.value }))}
                  >
                    {f.options?.map((o) => (
                      <option key={o} value={o}>
                        {o}
                      </option>
                    ))}
                  </Select>
                </Field>
              ) : (
                <Field label={f.label} required={f.required} hint={f.hint}>
                  <Input
                    type={f.type === "date" ? "date" : f.type === "number" ? "number" : "text"}
                    value={String(values[f.name] ?? "")}
                    onChange={(e) =>
                      setValues((v) => ({
                        ...v,
                        [f.name]: f.type === "number" ? (e.target.value === "" ? null : Number(e.target.value)) : e.target.value,
                      }))
                    }
                    placeholder={f.placeholder}
                  />
                </Field>
              )}
            </div>
          ))}
        </div>
        <div className="mt-6 flex justify-end gap-3">
          <Button variant="outline" onClick={() => setModalOpen(false)}>
            Cancel
          </Button>
          <Button onClick={() => void submit()} loading={saving}>
            {editing ? "Save changes" : "Create"}
          </Button>
        </div>
      </Modal>

      {/* Delete confirm */}
      <Modal open={deleting !== null} onClose={() => setDeleting(null)} title="Delete this entry?" size="sm">
        <p className="text-sm leading-relaxed text-muted">
          This will permanently remove the entry. This action cannot be undone.
        </p>
        <div className="mt-6 flex justify-end gap-3">
          <Button variant="outline" onClick={() => setDeleting(null)}>
            Cancel
          </Button>
          <Button variant="danger" onClick={() => void confirmDelete()}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
