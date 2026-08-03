import { Waves } from "lucide-react";
import { ResourceManager } from "@/components/admin/ResourceManager";
import { api } from "@/lib/api";
import type { Lake } from "@/types";

export function LakesPage() {
  return (
    <ResourceManager<Lake>
      title="Lakes"
      description="Lake restoration status and environmental updates."
      fetchItems={() => api.get<Lake[]>("/api/v1/lakes")}
      createItem={(data) => api.post<Lake>("/api/v1/lakes", data)}
      updateItem={(id, data) => api.put<Lake>(`/api/v1/lakes/${id}`, data)}
      deleteItem={(id) => api.del(`/api/v1/lakes/${id}`)}
      fields={[
        { name: "name", label: "Lake name", type: "text", required: true },
        { name: "location", label: "Location", type: "text", required: true },
        { name: "restorationStatus", label: "Restoration status", type: "text", placeholder: "e.g. In progress, Completed" },
        { name: "waterSource", label: "Water source", type: "text" },
        { name: "connectedStpId", label: "Connected STP ID", type: "number" },
        { name: "environmentalUpdates", label: "Environmental updates", type: "textarea" },
        { name: "description", label: "Description", type: "textarea", fullWidth: true },
      ]}
      searchKeys={["name", "location", "restorationStatus"]}
      getKey={(l) => `lake-${l.id}`}
      initialValues={() => ({ name: "", location: "", restorationStatus: "", waterSource: "", connectedStpId: null, environmentalUpdates: "", description: "" })}
      renderItem={(l) => (
        <div>
          <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-cyan-50 text-cyan-600">
              <Waves className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <h3 className="truncate text-[15px] font-semibold text-ink">{l.name}</h3>
              <p className="truncate text-xs text-muted">{l.location}</p>
            </div>
          </div>
          {l.restorationStatus && (
            <p className="mt-3 inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-2.5 py-0.5 text-xs font-medium text-emerald-700">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" aria-hidden />
              {l.restorationStatus}
            </p>
          )}
          {l.description && <p className="mt-3 line-clamp-2 text-sm text-muted">{l.description}</p>}
          {l.environmentalUpdates && (
            <p className="mt-2 text-xs text-cyan-700">{l.environmentalUpdates}</p>
          )}
        </div>
      )}
    />
  );
}
