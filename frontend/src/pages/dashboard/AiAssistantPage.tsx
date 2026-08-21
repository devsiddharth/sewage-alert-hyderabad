import { AiAssistant } from "@/components/ai/AiAssistant";

export function AiAssistantPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">
          AI Assistant
        </h1>
        <p className="mt-1 text-sm text-muted sm:text-base">
          Ask anything about sewage issues, events, NGOs, drives, and the platform.
        </p>
      </div>

      <AiAssistant mode="user" />

      <div className="rounded-2xl border border-line bg-white p-5">
        <h2 className="text-sm font-semibold text-ink">Suggested questions</h2>
        <ul className="mt-3 space-y-2 text-sm text-muted">
          <li>• What NGO drives are happening near me?</li>
          <li>• Which events are happening this weekend?</li>
          <li>• How can I report sewage overflow?</li>
          <li>• What NGOs work on sanitation?</li>
          <li>• Show me upcoming cleanup drives</li>
          <li>• What articles are related to sewage management?</li>
          <li>• What lakes are in Hyderabad and their restoration status?</li>
          <li>• Tell me about the sewage treatment plants</li>
          <li>• Which areas have pipeline infrastructure?</li>
          <li>• Show me the infrastructure overview</li>
          <li>• Are there complaints near treatment plants?</li>
          <li>• Which infrastructure areas have the most complaints?</li>
        </ul>
      </div>
    </div>
  );
}
