import { CalendarDays, Users } from "lucide-react";
import { ResourceManager } from "@/components/admin/ResourceManager";
import { api } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Event } from "@/types";

export function EventsPage() {
  return (
    <ResourceManager<Event>
      title="Events"
      description="Awareness drives, lake clean-ups and citizen workshops — managed by authorities."
      fetchItems={() => api.get<Event[]>("/api/v1/events")}
      createItem={(data) => api.post<Event>("/api/v1/events", data)}
      updateItem={(id, data) => api.put<Event>(`/api/v1/events/${id}`, data)}
      deleteItem={(id) => api.del(`/api/v1/events/${id}`)}
      fields={[
        { name: "title", label: "Title", type: "text", required: true, fullWidth: true },
        { name: "description", label: "Description", type: "textarea", required: true, fullWidth: true },
        { name: "location", label: "Location", type: "text", required: true },
        { name: "eventDate", label: "Event date", type: "date", required: true },
        { name: "organizerName", label: "Organizer", type: "text", required: true },
        { name: "capacity", label: "Capacity", type: "number", placeholder: "e.g. 200" },
      ]}
      searchKeys={["title", "location", "organizerName"]}
      getKey={(e) => `event-${e.id}`}
      initialValues={() => ({ title: "", description: "", location: "", eventDate: "", organizerName: "", capacity: null })}
      renderItem={(e) => (
        <div>
          <div className="flex items-center gap-3">
            <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
              <CalendarDays className="h-5 w-5" />
            </span>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-brand">{formatDate(e.eventDate)}</p>
              <p className="truncate text-xs text-muted">{e.location}</p>
            </div>
          </div>
          <h3 className="mt-3 line-clamp-2 text-[15px] font-semibold text-ink">{e.title}</h3>
          <p className="mt-1 line-clamp-2 text-sm text-muted">{e.description}</p>
          <p className="mt-3 inline-flex items-center gap-1.5 text-xs font-medium text-muted">
            <Users className="h-3.5 w-3.5" aria-hidden />
            {e.registeredCount} registered{e.capacity ? ` of ${e.capacity}` : ""} · by {e.organizerName}
          </p>
        </div>
      )}
    />
  );
}
