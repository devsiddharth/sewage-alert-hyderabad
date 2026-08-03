import { GitBranch } from "lucide-react";
import { ResourceManager } from "@/components/admin/ResourceManager";
import { api } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Pipeline } from "@/types";

// Matches Pipeline.OperationalStatus in community-service (model/Pipeline.java)
const STATUSES = ["ACTIVE", "UNDER_MAINTENANCE", "DECOMMISSIONED"];

export function PipelinesPage() {
  return (
    <ResourceManager<Pipeline>
      title="Pipelines"
      description="Sewage pipeline infrastructure by locality."
      fetchItems={() => api.get<Pipeline[]>("/api/v1/pipelines")}
      createItem={(data) => api.post<Pipeline>("/api/v1/pipelines", data)}
      updateItem={(id, data) => api.put<Pipeline>(`/api/v1/pipelines/${id}`, data)}
      deleteItem={(id) => api.del(`/api/v1/pipelines/${id}`)}
      fields={[
        { name: "locality", label: "Locality", type: "text", required: true },
        { name: "installationYear", label: "Installation year", type: "number" },
        { name: "designedCapacity", label: "Designed capacity (thousands)", type: "number" },
        { name: "maintenanceDate", label: "Next maintenance", type: "date" },
        { name: "operationalStatus", label: "Operational status", type: "select", options: STATUSES, required: true },
        { name: "notes", label: "Notes", type: "textarea", fullWidth: true },
      ]}
      searchKeys={["locality", "operationalStatus"]}
      getKey={(p) => `pipeline-${p.id}`}
      initialValues={() => ({ locality: "", installationYear: null, designedCapacity: null, maintenanceDate: "", operationalStatus: "OPERATIONAL", notes: "" })}
      renderItem={(p) => (
        <div>
          <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-amber-50 text-amber-600">
              <GitBranch className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <h3 className="truncate text-[15px] font-semibold text-ink">{p.locality}</h3>
              <p className="text-xs text-muted">
                {p.installationYear ? `Installed ${p.installationYear}` : "Year unknown"}
              </p>
            </div>
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            <span className="rounded-full bg-canvas px-2.5 py-0.5 text-xs font-medium text-muted">
              {p.operationalStatus.replace(/_/g, " ")}
            </span>
            {p.designedCapacity != null && (
              <span className="rounded-full bg-canvas px-2.5 py-0.5 text-xs font-medium text-muted">
                {p.designedCapacity} MLD
              </span>
            )}
            {p.maintenanceDate && (
              <span className="rounded-full bg-canvas px-2.5 py-0.5 text-xs font-medium text-muted">
                Maintenance {formatDate(p.maintenanceDate)}
              </span>
            )}
          </div>
          {p.notes && <p className="mt-3 line-clamp-2 text-sm text-muted">{p.notes}</p>}
        </div>
      )}
    />
  );
}
