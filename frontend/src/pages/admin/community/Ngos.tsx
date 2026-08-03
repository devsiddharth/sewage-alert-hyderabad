import { Globe, HeartHandshake, Mail, Phone } from "lucide-react";
import { ResourceManager } from "@/components/admin/ResourceManager";
import { api } from "@/lib/api";
import type { Ngo } from "@/types";

export function NgosPage() {
  return (
    <ResourceManager<Ngo>
      title="NGOs"
      description="Partner organisations working on sanitation, water and environment."
      fetchItems={() => api.get<Ngo[]>("/api/v1/ngos")}
      createItem={(data) => api.post<Ngo>("/api/v1/ngos", data)}
      updateItem={(id, data) => api.put<Ngo>(`/api/v1/ngos/${id}`, data)}
      deleteItem={(id) => api.del(`/api/v1/ngos/${id}`)}
      fields={[
        { name: "name", label: "Name", type: "text", required: true },
        { name: "contactPerson", label: "Contact person", type: "text", required: true },
        { name: "email", label: "Email", type: "text", required: true },
        { name: "phone", label: "Phone", type: "text" },
        { name: "website", label: "Website", type: "text", placeholder: "https://…" },
        { name: "description", label: "Description", type: "textarea", fullWidth: true },
      ]}
      searchKeys={["name", "contactPerson", "email"]}
      getKey={(n) => `ngo-${n.id}`}
      initialValues={() => ({ name: "", contactPerson: "", email: "", phone: "", website: "", description: "" })}
      renderItem={(n) => (
        <div>
          <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
              <HeartHandshake className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <h3 className="truncate text-[15px] font-semibold text-ink">{n.name}</h3>
              <p className="truncate text-xs text-muted">{n.contactPerson}</p>
            </div>
          </div>
          <p className="mt-3 line-clamp-2 text-sm text-muted">{n.description}</p>
          <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted">
            {n.phone && (
              <span className="inline-flex items-center gap-1">
                <Phone className="h-3 w-3" aria-hidden /> {n.phone}
              </span>
            )}
            {n.email && (
              <span className="inline-flex items-center gap-1">
                <Mail className="h-3 w-3" aria-hidden /> {n.email}
              </span>
            )}
            {n.website && (
              <a
                href={n.website}
                target="_blank"
                rel="noopener noreferrer"
                onClick={(e) => e.stopPropagation()}
                className="inline-flex items-center gap-1 text-brand hover:underline"
              >
                <Globe className="h-3 w-3" aria-hidden /> Website
              </a>
            )}
          </div>
        </div>
      )}
    />
  );
}
