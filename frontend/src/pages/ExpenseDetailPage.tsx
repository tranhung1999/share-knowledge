import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, CheckCircle, XCircle, DollarSign } from 'lucide-react';
import { Expense } from '../types';
import expenseService from '../services/expenseService';
import approvalService from '../services/approvalService';
import StatusBadge from '../components/common/StatusBadge';
import AttachmentUpload from '../components/expense/AttachmentUpload';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatCurrency, formatDate, formatDateTime, EXPENSE_TYPE_LABELS } from '../utils/formatters';
import { useAppSelector } from '../hooks/useAppDispatch';
import toast from 'react-hot-toast';

const ExpenseDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const user = useAppSelector(s => s.auth.user);
  const [expense, setExpense] = useState<Expense | null>(null);
  const [loading, setLoading] = useState(true);
  const [comment, setComment] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    expenseService.getExpenseById(Number(id))
      .then(r => setExpense(r.data))
      .catch(() => toast.error('Failed to load expense'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAction = async (action: 'approve' | 'reject' | 'pay') => {
    if (!expense) return;
    setActionLoading(true);
    try {
      let updated: Expense;
      if (action === 'approve') updated = (await approvalService.approveExpense(expense.id, comment)).data;
      else if (action === 'reject') updated = (await approvalService.rejectExpense(expense.id, comment)).data;
      else updated = (await approvalService.markAsPaid(expense.id, comment)).data;
      setExpense(updated);
      setComment('');
      toast.success(`Expense ${action === 'pay' ? 'marked as paid' : action + 'd'} successfully`);
    } catch {
      toast.error('Action failed');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <LoadingSpinner size="lg" className="mt-20" />;
  if (!expense) return <p className="text-red-500">Expense not found</p>;

  const canApproveReject = (user?.role === 'MANAGER' || user?.role === 'ADMIN')
    && expense.status === 'SUBMITTED';
  const canPay = (user?.role === 'ACCOUNTANT' || user?.role === 'ADMIN')
    && expense.status === 'APPROVED';
  const isOwner = user?.id === expense.employeeId;
  const canUpload = isOwner && expense.status === 'DRAFT';

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-600 p-1">
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="flex-1">
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">Expense #{expense.id}</h1>
            <StatusBadge status={expense.status} />
          </div>
          <p className="text-gray-500">Submitted by {expense.employeeName}</p>
        </div>
      </div>

      {/* Details */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Details</h2>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <p className="text-sm text-gray-500">Type</p>
            <p className="font-medium">{EXPENSE_TYPE_LABELS[expense.expenseType]}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Amount</p>
            <p className="font-semibold text-lg text-green-700">{formatCurrency(expense.amount)}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Date</p>
            <p className="font-medium">{formatDate(expense.expenseDate)}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Department</p>
            <p className="font-medium">{expense.department || 'N/A'}</p>
          </div>
          <div className="col-span-2">
            <p className="text-sm text-gray-500">Description</p>
            <p className="mt-1 text-gray-700">{expense.description}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Created</p>
            <p className="font-medium">{formatDateTime(expense.createdAt)}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Last Updated</p>
            <p className="font-medium">{formatDateTime(expense.updatedAt)}</p>
          </div>
        </div>
      </div>

      {/* Attachments */}
      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Attachments</h2>
        <AttachmentUpload
          expenseId={expense.id}
          attachments={expense.attachments}
          readonly={!canUpload}
          onUpdate={attachments => setExpense({ ...expense, attachments })}
        />
      </div>

      {/* Approval History */}
      {expense.approvals.length > 0 && (
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Approval History</h2>
          <div className="space-y-3">
            {expense.approvals.map(a => (
              <div key={a.id} className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
                <div className={`mt-0.5 p-1 rounded-full ${a.action === 'APPROVE' || a.action === 'PAY' ? 'bg-green-100' : 'bg-red-100'}`}>
                  {a.action === 'APPROVE' || a.action === 'PAY'
                    ? <CheckCircle className="h-4 w-4 text-green-600" />
                    : <XCircle className="h-4 w-4 text-red-600" />}
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">
                    {a.action} by {a.approverName}
                  </p>
                  {a.comment && <p className="text-sm text-gray-600 mt-0.5">{a.comment}</p>}
                  <p className="text-xs text-gray-400 mt-1">{formatDateTime(a.createdAt)}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Action Panel */}
      {(canApproveReject || canPay) && (
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Take Action</h2>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">Comment (optional)</label>
            <textarea
              rows={3}
              value={comment}
              onChange={e => setComment(e.target.value)}
              className="w-full border border-gray-300 rounded-md px-3 py-2"
              placeholder="Add a comment..."
            />
          </div>
          <div className="flex gap-3">
            {canApproveReject && (<>
              <button
                onClick={() => handleAction('approve')}
                disabled={actionLoading}
                className="flex items-center gap-2 px-5 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-60"
              >
                <CheckCircle className="h-4 w-4" /> Approve
              </button>
              <button
                onClick={() => handleAction('reject')}
                disabled={actionLoading}
                className="flex items-center gap-2 px-5 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-60"
              >
                <XCircle className="h-4 w-4" /> Reject
              </button>
            </>)}
            {canPay && (
              <button
                onClick={() => handleAction('pay')}
                disabled={actionLoading}
                className="flex items-center gap-2 px-5 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 disabled:opacity-60"
              >
                <DollarSign className="h-4 w-4" /> Mark as Paid
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default ExpenseDetailPage;
