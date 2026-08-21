import { useCallback, useRef, useState } from "react";
import { Bot, Send, Sparkles, Loader2, MessageSquare, X, Lightbulb } from "lucide-react";
import { aiService } from "@/services/ai";
import type { AiChatResponse } from "@/types";
import { cn } from "@/lib/cn";

// ---------------------------------------------------------------------------
// AiAssistant: Reusable chat widget for AI-powered platform assistant.
//
// Props:
//   - mode: Determines which API endpoint to use ("user" | "ngo" | "admin")
//   - className: Optional additional CSS classes
//   - compact: If true, renders as a floating button that expands into a chat panel
//   - defaultOpen: If compact, whether the panel starts open
// ---------------------------------------------------------------------------

interface Message {
  id: string;
  role: "user" | "assistant";
  content: string;
  intent?: AiChatResponse["intent"];
  dataUsed?: boolean;
  suggestion?: string | null;
  timestamp: Date;
}

interface AiAssistantProps {
  mode?: "user" | "ngo" | "admin";
  className?: string;
  compact?: boolean;
  defaultOpen?: boolean;
}

const SUGGESTIONS: Record<string, string[]> = {
  user: [
    "What NGO drives are happening near me?",
    "How can I report a sewage issue?",
    "Show me upcoming events",
    "What lakes are in Hyderabad?",
    "Tell me about treatment plants",
    "Which areas have pipeline issues?",
    "Are there complaints near treatment plants?",
    "Which infrastructure areas have the most complaints?",
  ],
  ngo: [
    "Summarize our recent drives",
    "How many volunteers participated?",
    "What areas should we focus on?",
    "Generate an event description",
  ],
  admin: [
    "Show complaint hotspots",
    "Which areas have recurring complaints?",
    "Summarize this month's complaints",
    "Which NGOs are most active?",
    "Show infrastructure status",
    "Which treatment plants are active?",
    "Which pipeline areas have the most complaints?",
    "Correlate complaints with infrastructure",
  ],
};

export function AiAssistant({
  mode = "user",
  className,
  compact = false,
  defaultOpen = false,
}: AiAssistantProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  const sendMessage = useCallback(
    async (text: string) => {
      if (!text.trim() || loading) return;

      const userMessage: Message = {
        id: crypto.randomUUID(),
        role: "user",
        content: text.trim(),
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, userMessage]);
      setInput("");
      setLoading(true);

      try {
        const sendFn =
          mode === "ngo"
            ? aiService.ngoQuery
            : mode === "admin"
            ? aiService.adminQuery
            : aiService.userQuery;

        const response = await sendFn(text.trim());

        const assistantMessage: Message = {
          id: crypto.randomUUID(),
          role: "assistant",
          content: response.response,
          intent: response.intent,
          dataUsed: response.dataUsed,
          suggestion: response.suggestion,
          timestamp: new Date(),
        };

        setMessages((prev) => [...prev, assistantMessage]);
      } catch {
        const errorMessage: Message = {
          id: crypto.randomUUID(),
          role: "assistant",
          content:
            "I'm sorry, I couldn't process your request right now. Please try again later.",
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, errorMessage]);
      } finally {
        setLoading(false);
        setTimeout(scrollToBottom, 50);
      }
    },
    [loading, mode, scrollToBottom]
  );

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage(input);
    }
  };

  const handleSuggestionClick = (suggestion: string) => {
    sendMessage(suggestion);
  };

  const clearChat = () => {
    setMessages([]);
  };

  // ---- Compact mode: floating button + slide-out panel ----
  if (compact) {
    return (
      <div className={cn("fixed bottom-6 right-6 z-50", className)}>
        {isOpen && (
          <div className="mb-3 w-[380px] max-w-[calc(100vw-3rem)] overflow-hidden rounded-2xl border border-line bg-white shadow-2xl animate-fade-in">
            {/* Header */}
            <div className="flex items-center justify-between border-b border-line bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-3">
              <div className="flex items-center gap-2">
                <Bot className="h-5 w-5 text-white" />
                <span className="text-sm font-semibold text-white">AI Assistant</span>
              </div>
              <div className="flex items-center gap-1">
                {messages.length > 0 && (
                  <button
                    onClick={clearChat}
                    className="rounded-lg p-1.5 text-white/70 transition-colors hover:bg-white/10 hover:text-white"
                    title="Clear chat"
                  >
                    <MessageSquare className="h-4 w-4" />
                  </button>
                )}
                <button
                  onClick={() => setIsOpen(false)}
                  className="rounded-lg p-1.5 text-white/70 transition-colors hover:bg-white/10 hover:text-white"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            </div>

            {/* Chat content */}
            <ChatContent
              messages={messages}
              loading={loading}
              input={input}
              setInput={setInput}
              sendMessage={sendMessage}
              handleKeyDown={handleKeyDown}
              handleSuggestionClick={handleSuggestionClick}
              messagesEndRef={messagesEndRef}
              suggestions={SUGGESTIONS[mode] ?? SUGGESTIONS.user}
            />
          </div>
        )}

        <button
          onClick={() => {
            setIsOpen((v) => !v);
            setTimeout(scrollToBottom, 100);
          }}
          className={cn(
            "flex h-14 w-14 items-center justify-center rounded-full bg-gradient-to-br from-blue-600 to-indigo-600 text-white shadow-lg transition-all hover:scale-105 hover:shadow-xl",
            isOpen && "bg-gradient-to-br from-gray-500 to-gray-600"
          )}
          title={isOpen ? "Close AI assistant" : "Open AI assistant"}
        >
          {isOpen ? <X className="h-6 w-6" /> : <Bot className="h-6 w-6" />}
        </button>
      </div>
    );
  }

  // ---- Full mode: embedded in page layout ----
  return (
    <div className={cn("flex flex-col rounded-2xl border border-line bg-white shadow-sm", className)}>
      {/* Header */}
      <div className="flex items-center justify-between border-b border-line px-5 py-4">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-indigo-500">
            <Bot className="h-5 w-5 text-white" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-ink">AI Assistant</h3>
            <p className="text-xs text-muted">Ask anything about the platform</p>
          </div>
        </div>
        {messages.length > 0 && (
          <button
            onClick={clearChat}
            className="rounded-lg px-3 py-1.5 text-xs font-medium text-muted transition-colors hover:bg-canvas hover:text-ink"
          >
            Clear chat
          </button>
        )}
      </div>

      {/* Chat content */}
      <ChatContent
        messages={messages}
        loading={loading}
        input={input}
        setInput={setInput}
        sendMessage={sendMessage}
        handleKeyDown={handleKeyDown}
        handleSuggestionClick={handleSuggestionClick}
        messagesEndRef={messagesEndRef}
        suggestions={SUGGESTIONS[mode] ?? SUGGESTIONS.user}
      />
    </div>
  );
}

// ---- Inner chat content (shared between compact and full modes) ----

function ChatContent({
  messages,
  loading,
  input,
  setInput,
  sendMessage,
  handleKeyDown,
  handleSuggestionClick,
  messagesEndRef,
  suggestions,
}: {
  messages: Message[];
  loading: boolean;
  input: string;
  setInput: (v: string) => void;
  sendMessage: (v: string) => void;
  handleKeyDown: (e: React.KeyboardEvent) => void;
  handleSuggestionClick: (v: string) => void;
  messagesEndRef: React.RefObject<HTMLDivElement>;
  suggestions: string[];
}) {
  return (
    <>
      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-4" style={{ minHeight: 300, maxHeight: 400 }}>
        {messages.length === 0 ? (
          <div className="flex h-full flex-col items-center justify-center text-center">
            <Sparkles className="h-10 w-10 text-blue-400" />
            <p className="mt-3 text-sm font-medium text-ink">How can I help you today?</p>
            <p className="mt-1 text-xs text-muted">
              Ask about events, drives, NGOs, complaints, or anything on the platform.
            </p>
            <div className="mt-4 flex flex-wrap justify-center gap-2">
              {suggestions.map((s) => (
                <button
                  key={s}
                  onClick={() => handleSuggestionClick(s)}
                  className="rounded-full border border-line bg-canvas/50 px-3 py-1.5 text-xs font-medium text-muted transition-colors hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
                >
                  {s}
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={cn(
                  "flex",
                  msg.role === "user" ? "justify-end" : "justify-start"
                )}
              >
                <div
                  className={cn(
                    "max-w-[85%] rounded-2xl px-4 py-2.5 text-sm leading-relaxed",
                    msg.role === "user"
                      ? "bg-blue-600 text-white"
                      : "bg-canvas text-ink"
                  )}
                >
                  {msg.role === "assistant" && (
                    <div className="mb-1.5 flex items-center gap-1.5">
                      <Bot className="h-3.5 w-3.5 text-blue-500" />
                      <span className="text-[10px] font-semibold uppercase tracking-wide text-blue-500">
                        AI Assistant
                      </span>
                      {msg.dataUsed && (
                        <span className="inline-flex items-center gap-0.5 rounded-full bg-green-100 px-1.5 py-0.5 text-[9px] font-medium text-green-700">
                          <Lightbulb className="h-2.5 w-2.5" />
                          grounded
                        </span>
                      )}
                    </div>
                  )}
                  <div className="whitespace-pre-wrap">{msg.content}</div>
                  {msg.suggestion && (
                    <div className="mt-2 rounded-lg bg-blue-50 px-3 py-2 text-xs text-blue-700">
                      💡 {msg.suggestion}
                    </div>
                  )}
                </div>
              </div>
            ))}

            {loading && (
              <div className="flex justify-start">
                <div className="flex items-center gap-2 rounded-2xl bg-canvas px-4 py-3 text-sm text-muted">
                  <Loader2 className="h-4 w-4 animate-spin text-blue-500" />
                  Thinking...
                </div>
              </div>
            )}

            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      {/* Input */}
      <div className="border-t border-line px-4 py-3">
        <div className="flex items-end gap-2">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ask something..."
            rows={1}
            className="flex-1 resize-none rounded-xl border border-line bg-canvas/50 px-4 py-2.5 text-sm text-ink placeholder-muted transition-colors focus:border-blue-400 focus:outline-none focus:ring-1 focus:ring-blue-400"
            style={{ minHeight: 40, maxHeight: 100 }}
            onInput={(e) => {
              const target = e.target as HTMLTextAreaElement;
              target.style.height = "auto";
              target.style.height = Math.min(target.scrollHeight, 100) + "px";
            }}
          />
          <button
            onClick={() => sendMessage(input)}
            disabled={!input.trim() || loading}
            className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl bg-blue-600 text-white transition-colors hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Send className="h-4 w-4" />
          </button>
        </div>
      </div>
    </>
  );
}
