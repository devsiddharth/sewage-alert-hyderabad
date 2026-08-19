import { useCallback, useEffect, useState, type FormEvent } from "react";
import { TrendingDown, TrendingUp, AlertCircle, CheckCircle2 } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Field, Input, Textarea } from "@/components/ui/Field";
import { Modal } from "@/components/ui/Modal";
import { Skeleton } from "@/components/ui/States";
import { api, ApiError } from "@/lib/api";
import { useToast } from "@/lib/toast";
import { formatDate } from "@/lib/utils";
import type { NgoFund, NgoExpense } from "@/types";

export function NgoFunds() {
  const { toast } = useToast();
  const [funds, setFunds] = useState<NgoFund[]>([]);
  const [expenses, setExpenses] = useState<NgoExpense[]>([]);
  const [loading, setLoading] = useState(true);
  const [showFund, setShowFund] = useState(false);
  const [showExpense, setShowExpense] = useState(false);
  const [creating, setCreating] = useState(false);

  const [fundForm, setFundForm] = useState({ source: "", amount: "", projectName: "", description: "", receivedDate: "" });
  const [expenseForm, setExpenseForm] = useState({ fundRecordId: "", category: "", amount: "", description: "", expenseDate: "" });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [f, e] = await Promise.all([
        api.get<NgoFund[]>("/api/v1/ngo/funds"),
        api.get<NgoExpense[]>("/api/v1/ngo/expenses"),
      ]);
      setFunds(f);
      setExpenses(e);
    } catch { /* silent */ } finally { setLoading(false); }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const totalFunds = funds.reduce((s, f) => s + f.amount, 0);
  const totalExpenses = expenses.reduce((s, e) => s + e.amount, 0);
  const remaining = totalFunds - totalExpenses;

  const updateFund = (field: string, value: string) => setFundForm((f) => ({ ...f, [field]: value }));
  const updateExpense = (field: string, value: string) => setExpenseForm((f) => ({ ...f, [field]: value }));

  const handleAddFund = async (e: FormEvent) => {
    e.preventDefault();
    if (!fundForm.source || !fundForm.amount) { toast("error", "Missing", "Source and amount required."); return; }
    setCreating(true);
    try {
      await api.post("/api/v1/ngo/funds", {
        source: fundForm.source,
        amount: parseFloat(fundForm.amount),
        projectName: fundForm.projectName || null,
        description: fundForm.description || null,
        receivedDate: fundForm.receivedDate || new Date().toISOString().split("T")[0],
      });
      toast("success", "Fund record added", "");
      setShowFund(false);
      setFundForm({ source: "", amount: "", projectName: "", description: "", receivedDate: "" });
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not add fund record.");
    } finally { setCreating(false); }
  };

  const handleAddExpense = async (e: FormEvent) => {
    e.preventDefault();
    if (!expenseForm.category || !expenseForm.amount) { toast("error", "Missing", "Category and amount required."); return; }
    if (parseFloat(expenseForm.amount) <= 0) { toast("error", "Invalid", "Amount must be positive."); return; }
    setCreating(true);
    try {
      await api.post("/api/v1/ngo/expenses", {
        fundRecordId: expenseForm.fundRecordId ? parseInt(expenseForm.fundRecordId) : null,
        category: expenseForm.category,
        amount: parseFloat(expenseForm.amount),
        description: expenseForm.description || null,
        expenseDate: expenseForm.expenseDate || new Date().toISOString().split("T")[0],
      });
      toast("success", "Expense recorded", "");
      setShowExpense(false);
      setExpenseForm({ fundRecordId: "", category: "", amount: "", description: "", expenseDate: "" });
      void load();
    } catch (err) {
      toast("error", "Failed", err instanceof ApiError ? err.message : "Could not record expense.");
    } finally { setCreating(false); }
  };

  if (loading) return <div className="space-y-4">{[0, 1, 2].map((i) => <Skeleton key={i} className="h-32 rounded-2xl" />)}</div>;

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-ink">Funds & Expenses</h1>
          <p className="mt-1 text-muted">Transparent financial records for your organization.</p>
        </div>
        <div className="flex gap-2">
          <Button onClick={() => setShowFund(true)} icon={<TrendingUp className="h-4 w-4" />}>Add Fund</Button>
          <Button onClick={() => setShowExpense(true)} icon={<TrendingDown className="h-4 w-4" />}>Record Expense</Button>
        </div>
      </div>

      {/* Summary */}
      <div className="grid gap-4 sm:grid-cols-3">
        <Card className="p-5 border-l-4 border-l-green-500">
          <p className="text-sm text-muted">Total Funds Received</p>
          <p className="mt-1 text-2xl font-bold text-green-600">₹{totalFunds.toLocaleString("en-IN")}</p>
        </Card>
        <Card className="p-5 border-l-4 border-l-red-500">
          <p className="text-sm text-muted">Total Expenses</p>
          <p className="mt-1 text-2xl font-bold text-red-500">₹{totalExpenses.toLocaleString("en-IN")}</p>
        </Card>
        <Card className={`p-5 border-l-4 ${remaining >= 0 ? "border-l-blue-500" : "border-l-amber-500"}`}>
          <p className="text-sm text-muted">Remaining Balance</p>
          <p className={`mt-1 text-2xl font-bold ${remaining >= 0 ? "text-blue-600" : "text-amber-500"}`}>₹{remaining.toLocaleString("en-IN")}</p>
        </Card>
      </div>

      {/* Funds */}
      <Card className="p-5">
        <h2 className="text-lg font-semibold text-ink mb-4">Fund Records</h2>
        {funds.length === 0 ? (
          <p className="text-sm text-muted text-center py-6">No fund records yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="text-left text-xs text-muted border-b border-line">
                <th className="pb-2 pr-4">Source</th><th className="pb-2 pr-4">Amount</th><th className="pb-2 pr-4">Project</th><th className="pb-2 pr-4">Date</th><th className="pb-2">Description</th>
              </tr></thead>
              <tbody className="divide-y divide-line">
                {funds.map((f) => (
                  <tr key={f.id}>
                    <td className="py-3 pr-4 font-medium text-ink">{f.source}</td>
                    <td className="py-3 pr-4 text-green-600 font-semibold">₹{f.amount.toLocaleString("en-IN")}</td>
                    <td className="py-3 pr-4 text-muted">{f.projectName || "—"}</td>
                    <td className="py-3 pr-4 text-muted">{formatDate(f.receivedDate)}</td>
                    <td className="py-3 text-muted">{f.description || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {/* Expenses */}
      <Card className="p-5">
        <h2 className="text-lg font-semibold text-ink mb-4">Expense Records</h2>
        {expenses.length === 0 ? (
          <p className="text-sm text-muted text-center py-6">No expense records yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="text-left text-xs text-muted border-b border-line">
                <th className="pb-2 pr-4">Category</th><th className="pb-2 pr-4">Amount</th><th className="pb-2 pr-4">Date</th><th className="pb-2">Description</th>
              </tr></thead>
              <tbody className="divide-y divide-line">
                {expenses.map((ex) => (
                  <tr key={ex.id}>
                    <td className="py-3 pr-4 font-medium text-ink">{ex.category}</td>
                    <td className="py-3 pr-4 text-red-500 font-semibold">₹{ex.amount.toLocaleString("en-IN")}</td>
                    <td className="py-3 pr-4 text-muted">{formatDate(ex.expenseDate)}</td>
                    <td className="py-3 text-muted">{ex.description || "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>

      {/* Add Fund Modal */}
      <Modal open={showFund} onClose={() => setShowFund(false)} title="Add Fund Record">
          <form onSubmit={handleAddFund} className="space-y-4">
            <Field label="Source" required>
              <Input value={fundForm.source} onChange={(e) => updateFund("source", e.target.value)} placeholder="e.g. Municipal Corporation Grant" />
            </Field>
            <Field label="Amount (₹)" required>
              <Input type="number" step="0.01" min="0" value={fundForm.amount} onChange={(e) => updateFund("amount", e.target.value)} placeholder="50000" />
            </Field>
            <Field label="Project Name">
              <Input value={fundForm.projectName} onChange={(e) => updateFund("projectName", e.target.value)} placeholder="e.g. Miyapur Awareness Drive" />
            </Field>
            <Field label="Received Date">
              <Input type="date" value={fundForm.receivedDate} onChange={(e) => updateFund("receivedDate", e.target.value)} />
            </Field>
            <Field label="Description">
              <Textarea rows={2} value={fundForm.description} onChange={(e) => updateFund("description", e.target.value)} />
            </Field>
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" type="button" onClick={() => setShowFund(false)}>Cancel</Button>
              <Button type="submit" loading={creating} icon={<CheckCircle2 className="h-4 w-4" />}>Save</Button>
            </div>
          </form>
      </Modal>

      {/* Add Expense Modal */}
      <Modal open={showExpense} onClose={() => setShowExpense(false)} title="Record Expense">
          <form onSubmit={handleAddExpense} className="space-y-4">
            <Field label="Category" required>
              <Input value={expenseForm.category} onChange={(e) => updateExpense("category", e.target.value)} placeholder="e.g. Equipment, Transportation, Materials" />
            </Field>
            <Field label="Amount (₹)" required>
              <Input type="number" step="0.01" min="0.01" value={expenseForm.amount} onChange={(e) => updateExpense("amount", e.target.value)} placeholder="12000" />
            </Field>
            <Field label="Link to Fund Record">
              <select className="w-full rounded-xl border border-line bg-white px-4 py-2.5 text-sm" value={expenseForm.fundRecordId} onChange={(e) => updateExpense("fundRecordId", e.target.value)}>
                <option value="">None</option>
                {funds.map((f) => <option key={f.id} value={String(f.id)}>{f.source} — ₹{f.amount.toLocaleString("en-IN")}</option>)}
              </select>
            </Field>
            <Field label="Expense Date">
              <Input type="date" value={expenseForm.expenseDate} onChange={(e) => updateExpense("expenseDate", e.target.value)} />
            </Field>
            <Field label="Description">
              <Textarea rows={2} value={expenseForm.description} onChange={(e) => updateExpense("description", e.target.value)} />
            </Field>
            {expenseForm.amount && parseFloat(expenseForm.amount) > remaining && remaining > 0 && (
              <div className="flex items-center gap-2 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                <AlertCircle className="h-4 w-4 shrink-0" /> This expense exceeds the remaining balance.
              </div>
            )}
            <div className="flex justify-end gap-3 pt-2">
              <Button variant="outline" type="button" onClick={() => setShowExpense(false)}>Cancel</Button>
              <Button type="submit" loading={creating} icon={<CheckCircle2 className="h-4 w-4" />}>Save</Button>
            </div>
          </form>
      </Modal>
    </div>
  );
}
