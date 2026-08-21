import { useState } from "react";
import { Sparkles, Loader2, FileText } from "lucide-react";
import { AiAssistant } from "@/components/ai/AiAssistant";
import { aiService } from "@/services/ai";


export function AiAssistantPage() {
  const [articleTopic, setArticleTopic] = useState("");
  const [articleResult, setArticleResult] = useState<string | null>(null);
  const [generating, setGenerating] = useState(false);

  const handleGenerateArticle = async () => {
    if (!articleTopic.trim() || generating) return;
    setGenerating(true);
    setArticleResult(null);
    try {
      const result = await aiService.generateArticle(articleTopic);
      setArticleResult(result.response);
    } catch {
      setArticleResult("Unable to generate article. Please try again later.");
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold tracking-tight text-ink sm:text-2xl lg:text-3xl">
          AI Assistant
        </h1>
        <p className="mt-1 text-sm text-muted sm:text-base">
          Your NGO-specific AI assistant for insights, summaries, and content generation.
        </p>
      </div>

      <AiAssistant mode="ngo" />

      {/* Article Generator */}
      <div className="rounded-2xl border border-line bg-white p-5">
        <div className="flex items-center gap-2">
          <Sparkles className="h-5 w-5 text-purple-500" />
          <h2 className="text-sm font-semibold text-ink">Article Generator</h2>
        </div>
        <p className="mt-1 text-xs text-muted">
          Generate an awareness article draft. You can edit it before publishing.
        </p>
        <div className="mt-3 flex gap-2">
          <input
            type="text"
            value={articleTopic}
            onChange={(e) => setArticleTopic(e.target.value)}
            placeholder="e.g., sewage overflow awareness for Miyapur residents"
            className="flex-1 rounded-xl border border-line bg-canvas/50 px-4 py-2.5 text-sm text-ink placeholder-muted transition-colors focus:border-blue-400 focus:outline-none focus:ring-1 focus:ring-blue-400"
            onKeyDown={(e) => {
              if (e.key === "Enter") handleGenerateArticle();
            }}
          />
          <button
            onClick={handleGenerateArticle}
            disabled={!articleTopic.trim() || generating}
            className="inline-flex items-center gap-1.5 rounded-xl bg-purple-600 px-4 py-2.5 text-sm font-medium text-white transition-colors hover:bg-purple-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {generating ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <FileText className="h-4 w-4" />
            )}
            Generate
          </button>
        </div>
        {articleResult && (
          <div className="mt-4 rounded-xl border border-line bg-canvas/30 p-4">
            <div className="flex items-center gap-1.5 text-xs font-medium text-purple-600">
              <FileText className="h-3.5 w-3.5" />
              AI-Generated Draft
            </div>
            <div className="mt-2 whitespace-pre-wrap text-sm text-ink leading-relaxed">
              {articleResult}
            </div>
            <p className="mt-3 text-xs text-muted italic">
              This is an AI-generated draft. Please review and edit before publishing through the Articles page.
            </p>
          </div>
        )}
      </div>

      <div className="rounded-2xl border border-line bg-white p-5">
        <h2 className="text-sm font-semibold text-ink">What can I ask?</h2>
        <ul className="mt-3 space-y-2 text-sm text-muted">
          <li>• Summarize our last 5 drives</li>
          <li>• How many volunteers participated this month?</li>
          <li>• Which areas had the highest complaint volume?</li>
          <li>• Which drives performed best?</li>
          <li>• What areas should our NGO focus on?</li>
          <li>• Summarize our recent activities</li>
        </ul>
      </div>
    </div>
  );
}
