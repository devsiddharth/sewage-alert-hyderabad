import { FileText } from "lucide-react";
import { ResourceManager } from "@/components/admin/ResourceManager";
import { api } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import type { Article } from "@/types";

export function ArticlesPage() {
  return (
    <ResourceManager<Article>
      title="Articles"
      description="Educational content about sewage treatment, water conservation and lake restoration."
      fetchItems={() => api.get<Article[]>("/api/v1/articles")}
      createItem={(data) => api.post<Article>("/api/v1/articles", data)}
      updateItem={(id, data) => api.put<Article>(`/api/v1/articles/${id}`, data)}
      deleteItem={(id) => api.del(`/api/v1/articles/${id}`)}
      fields={[
        { name: "title", label: "Title", type: "text", required: true, fullWidth: true },
        { name: "category", label: "Category", type: "text", required: true, placeholder: "e.g. Water Conservation" },
        { name: "authorName", label: "Author", type: "text", required: true },
        { name: "content", label: "Content", type: "textarea", required: true, fullWidth: true },
      ]}
      searchKeys={["title", "category", "authorName"]}
      getKey={(a) => `article-${a.id}`}
      initialValues={() => ({ title: "", category: "", authorName: "", content: "" })}
      renderItem={(a) => (
        <div>
          <div className="flex items-center gap-2">
            <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-accent-soft text-brand">
              <FileText className="h-4 w-4" />
            </span>
            <span className="rounded-full bg-canvas px-2.5 py-0.5 text-xs font-medium text-muted">{a.category}</span>
          </div>
          <h3 className="mt-3 line-clamp-2 text-[15px] font-semibold text-ink">{a.title}</h3>
          <p className="mt-1 line-clamp-2 text-sm text-muted">{a.content}</p>
          <p className="mt-3 text-xs text-muted">
            {a.authorName} · {formatDate(a.publishedAt)}
          </p>
        </div>
      )}
    />
  );
}
