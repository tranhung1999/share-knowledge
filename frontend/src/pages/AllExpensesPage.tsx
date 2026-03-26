import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Eye } from 'lucide-react';
import { Expense, ExpenseStatus } from '../types';
import expenseService from '../services/expenseService';
import StatusBadge from '../components/common/StatusBadge';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatCurrency, formatDate, EXPENSE_TYPE_LABELS } from '../utils/formatters';
import toast from 'react-hot-toast';

const AllExpensesPage: React.FC = () => {
  const navigate = useNavigate();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<ExpenseStatus | ''>('SUBMITTED');
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);

  const fetchExpenses = useCallback(async () => {
    setLoading(true);
    try {
      const resp = await expenseService.getAllExpenses({
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

  const STATUSES: Array<ExpenseStatus | ''> = ['', 'DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'PAID'];
  const STATUS_LABELS: Record<string, string> = {
    '': 'All', DRAFT: 'Draft', SUBMITTED: 'Pending',
    APPROVED: 'Approved', REJECTED: 'Rejected', PAID: 'Paid'
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">All Expenses</h1>
        <p className="text-gray-500">{totalElements} total</p>
      </div>

      <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
        <div className="flex gap-2 flex-wrap">
          {STATUSES.map(s => (
            <button key={s}
              onClick={() => { setStatusFilter(s); setPage(0); }}
              className={`px-3 py-1.5 rounded-md text-sm font-medium transition-colors
                ${statusFilter === s
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}
            >
              {STATUS_LABELS[s]}
            </button>
          ))}
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <LoadingSpinner className="py-12" />
        ) : expenses.length === 0 ? (
          <p className="py-12 text-center text-gray-500">No expenses found</p>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                {['Employee','Dept','Date','Type','Amount','Status','Actions'].map(h => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {expenses.map(exp => (
                <tr key={exp.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm">
                    <p className="font-medium text-gray-900">{exp.employeeName}</p>
                    <p className="text-gray-400 text-xs">{exp.employeeEmail}</p>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">{exp.department || '—'}</td>
                  <td className="px-4 py-3 text-sm text-gray-900 whitespace-nowrap">
                    {formatDate(exp.expenseDate)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-900">
                    {EXPENSE_TYPE_LABELS[exp.expenseType]}
                  </td>
                  <td className="px-4 py-3 text-sm font-semibold text-gray-900">
                    {formatCurrency(exp.amount)}
                  </td>
                  <td className="px-4 py-3"><StatusBadge status={exp.status} /></td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => navigate(`/expenses/${exp.id}`)}
                      className="text-gray-400 hover:text-primary-600 p-1"
                    >
                      <Eye className="h-4 w-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination */}
      {totalElements > 20 && (
        <div className="flex justify-center gap-2">
          <button
            disabled={page === 0}
            onClick={() => setPage(p => p - 1)}
            className="px-4 py-2 border rounded-md text-sm disabled:opacity-40"
          >
            Previous
          </button>
          <span className="px-4 py-2 text-sm text-gray-600">Page {page + 1}</span>
          <button
            disabled={expenses.length < 20}
            onClick={() => setPage(p => p + 1)}
            className="px-4 py-2 border rounded-md text-sm disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default AllExpensesPage;
