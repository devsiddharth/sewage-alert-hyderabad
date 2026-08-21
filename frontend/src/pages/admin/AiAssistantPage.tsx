import { AiAssistant } from "@/components/ai/AiAssistant";

export function AiAssistantPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">
          AI Assistant
        </h1>
        <p className="mt-1 text-sm text-muted sm:text-base">
          Get platform-wide insights, complaint analytics, and NGO activity summaries.
        </p>
      </div>

      <AiAssistant mode="admin" />

      <div className="rounded-2xl border border-line bg-white p-5">
        <h2 className="text-sm font-semibold text-ink">What can I ask?</h2>
        <ul className="mt-3 space-y-2 text-sm text-muted">
          <li>• Show complaint hotspots and recurring problem areas</li>
          <li>• Summarize this month's complaints</li>
          <li>• Which NGOs are most active?</li>
          <li>• Which areas need attention?</li>
          <li>• What complaint categories are most common?</li>
          <li>• Show worsening complaint trends</li>
          <li>• Summarize recent NGO activity</li>
          <li>• Show infrastructure status (pipelines, lakes, STPs)</li>
          <li>• Which treatment plants have the highest capacity?</li>
          <li>• What is the pipeline maintenance status?</li>
          <li>• Which pipeline areas have the most complaints?</li>
          <li>• Correlate complaints with infrastructure locations</li>
          <li>• Are there complaint patterns near lakes or STPs?</li>
        </ul>
      </div>
    </div>
  );
}
