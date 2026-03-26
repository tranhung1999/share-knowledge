import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, Eye, Edit, Trash2, Send } from 'lucide-react';
import { Expense, ExpenseStatus } from '../types';
import expenseService from '../services/expenseService';
import StatusBadge from '../components/common/StatusBadge';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ConfirmDialog from '../components/common/ConfirmDialog';
import { formatCurrency, formatDate, EXPENSE_TYPE_LABELS } from '../utils/formatters';
import toast from 'react-hot-toast';

const ExpensesPage: React.FC = () => {
  const navigate = useNavigate();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<ExpenseStatus | ''>('');
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);

  const fetchExpenses = useCallback(async () => {
    setLoading(true);
    try {
      const resp = await expenseService.getMyExpenses({
        status: statusFilter || undefined,
        page,
        size: 20,
      });
      setExpenses(resp.data.content);
      setTotalElements(resp.data.totalElements);
    } catch {
      toast.error('Failed to load expenses');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, page]);

  useEffect(() => { fetchExpenses(); }, [fetchExpenses]);

  const handleSubmit = async (id: number) => {
    try {
      await expenseService.submitExpense(id);
      toast.success('Expense submitted for approval');
      fetchExpenses();
    } catch {
      toast.error('Failed to submit expense');
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await expenseService.deleteExpense(deleteTarget);
      toast.success('Expense deleted');
      fetchExpenses();
    } catch {
      toast.error('Failed to delete expense');
    } finally {
      setDeleteTarget(null);
    }
  };

  const STATUSES: ExpenseStatus[] = ['DRAFT','SUBMITTED','APPROVED','REJECTED','PAID'];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Expenses</h1>
          <p className="text-gray-500">{totalElements} total expenses</p>
        </div>
        <button
          onClick={() => navigate('/expenses/new')}
          className="flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700"
        >
          <Plus className="h-4 w-4" /> New Expense
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
        <div className="flex gap-2 flex-wrap">
          <button
            onClick={() => { setStatusFilter(''); setPage(0); }}
            className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors
              ${statusFilter === '' ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
          >
            All
          </button>
          {STATUSES.map(s => (
            <button key={s}
              onClick={() => { setStatusFilter(s); setPage(0); }}
              className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors
                ${statusFilter === s ? 'bg-primary-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <LoadingSpinner className="py-12" />
        ) : expenses.length === 0 ? (
          <div className="py-12 text-center text-gray-500">
            <FileText className="h-12 w-12 mx-auto mb-3 text-gray-300" />
            <p>No expenses found</p>
            <button
              onClick={() => navigate('/expenses/new')}
              className="mt-3 text-primary-600 hover:underline text-sm"
            >
              Create your first expense
            </button>
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                {['Date','Type','Amount','Description','Status','Actions'].map(h => (
                  <th key={h} className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {expenses.map(exp => (
                <tr key={exp.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                    {formatDate(exp.expenseDate)}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900 whitespace-nowrap">
                    {EXPENSE_TYPE_LABELS[exp.expenseType]}
                  </td>
                  <td className="px-6 py-4 text-sm font-semibold text-gray-900 whitespace-nowrap">
                    {formatCurrency(exp.amount)}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
                    {exp.description}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <StatusBadge status={exp.status} />
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center gap-2">
                      <button title="View"
                        onClick={() => navigate(`/expenses/${exp.id}`)}
                        className="text-gray-400 hover:text-primary-600 p-1">
                        <Eye className="h-4 w-4" />
                      </button>
                      {exp.status === 'DRAFT' && (<>
                        <button title="Edit"
                          onClick={() => navigate(`/expenses/${exp.id}/edit`)}
                          className="text-gray-400 hover:text-blue-600 p-1">
                          <Edit className="h-4 w-4" />
                        </button>
                        <button title="Submit"
                          onClick={() => handleSubmit(exp.id)}
                          className="text-gray-400 hover:text-green-600 p-1">
                          <Send className="h-4 w-4" />
                        </button>
                        <button title="Delete"
                          onClick={() => setDeleteTarget(exp.id)}
                          className="text-gray-400 hover:text-red-600 p-1">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </>)}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <ConfirmDialog
        isOpen={deleteTarget !== null}
        title="Delete Expense"
        message="Are you sure you want to delete this expense? This action cannot be undone."
        confirmLabel="Delete"
        onConfirm={handleDelete}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  );
};

// Needed as placeholder – imported in file above
function FileText(props: { className?: string }) {
  return <svg className={props.className} fill="none" viewBox="0 0 24 24" stroke="currentColor">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5}
      d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
  </svg>;
}

export default ExpensesPage;
