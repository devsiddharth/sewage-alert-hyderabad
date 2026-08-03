import { Droplets, Factory } from "lucide-react";
import { ResourceManager } from "@/components/admin/ResourceManager";
import { api } from "@/lib/api";
import type { TreatmentPlant } from "@/types";

export function PlantsPage() {
  return (
    <ResourceManager<TreatmentPlant>
      title="Treatment plants"
      description="Sewage Treatment Plants (STPs) across Hyderabad and their capacity."
      fetchItems={() => api.get<TreatmentPlant[]>("/api/v1/treatment-plants")}
      createItem={(data) => api.post<TreatmentPlant>("/api/v1/treatment-plants", data)}
      updateItem={(id, data) => api.put<TreatmentPlant>(`/api/v1/treatment-plants/${id}`, data)}
      deleteItem={(id) => api.del(`/api/v1/treatment-plants/${id}`)}
      fields={[
        { name: "name", label: "Plant name", type: "text", required: true },
        { name: "location", label: "Location", type: "text", required: true },
        { name: "capacityMld", label: "Capacity (MLD)", type: "number", hint: "Million litres per day" },
        { name: "treatmentMethod", label: "Treatment method", type: "text", required: true, placeholder: "e.g. SBR, MBBR" },
        { name: "waterReuseInfo", label: "Water reuse info", type: "text" },
        { name: "description", label: "Description", type: "textarea", fullWidth: true },
      ]}
      searchKeys={["name", "location", "treatmentMethod"]}
      getKey={(p) => `plant-${p.id}`}
      initialValues={() => ({ name: "", location: "", capacityMld: null, treatmentMethod: "", waterReuseInfo: "", description: "" })}
      renderItem={(p) => (
        <div>
          <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand/8 text-brand">
              <Factory className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <h3 className="truncate text-[15px] font-semibold text-ink">{p.name}</h3>
              <p className="truncate text-xs text-muted">{p.location}</p>
            </div>
          </div>
          <p className="mt-3 line-clamp-2 text-sm text-muted">{p.description}</p>
          <div className="mt-3 flex flex-wrap items-center gap-3 text-xs text-muted">
            <span className="inline-flex items-center gap-1 rounded-full bg-blue-50 px-2.5 py-0.5 font-medium text-blue-700">
              <Droplets className="h-3 w-3" aria-hidden />
              {p.capacityMld ?? "—"} MLD
            </span>
            <span>{p.treatmentMethod}</span>
          </div>
          {p.waterReuseInfo && (
            <p className="mt-2 text-xs text-emerald-700">Reuse: {p.waterReuseInfo}</p>
          )}
        </div>
      )}
    />
  );
}
