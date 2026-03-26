import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ExpenseFormData, ExpenseType } from '../../types';
import { EXPENSE_TYPE_LABELS } from '../../utils/formatters';

const schema = z.object({
  expenseType: z.string().min(1, 'Expense type is required'),
  amount: z.coerce.number().positive('Amount must be greater than 0').max(999999999),
  expenseDate: z.string().min(1, 'Date is required'),
  description: z.string().min(5, 'Min 5 characters').max(2000, 'Max 2000 characters'),
});

interface Props {
  defaultValues?: Partial<ExpenseFormData>;
  onSubmit: (data: ExpenseFormData, action: 'save' | 'submit') => Promise<void>;
  isLoading?: boolean;
}

const EXPENSE_TYPES = Object.keys(EXPENSE_TYPE_LABELS) as ExpenseType[];

const ExpenseForm: React.FC<Props> = ({ defaultValues, onSubmit, isLoading }) => {
  const { register, handleSubmit, formState: { errors } } = useForm<ExpenseFormData>({
    resolver: zodResolver(schema),
    defaultValues: defaultValues ?? { expenseDate: new Date().toISOString().split('T')[0] },
  });

  const handleAction = (action: 'save' | 'submit') =>
    handleSubmit(data => onSubmit(data as ExpenseFormData, action))();

  return (
    <form className="space-y-6" onSubmit={e => e.preventDefault()}>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Expense Type *</label>
          <select
            {...register('expenseType')}
            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
          >
            <option value="">Select type...</option>
            {EXPENSE_TYPES.map(t => (
              <option key={t} value={t}>{EXPENSE_TYPE_LABELS[t]}</option>
            ))}
          </select>
          {errors.expenseType && (
            <p className="mt-1 text-sm text-red-600">{errors.expenseType.message}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Amount (USD) *</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            {...register('amount')}
            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
            placeholder="0.00"
          />
          {errors.amount && (
            <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Expense Date *</label>
          <input
            type="date"
            {...register('expenseDate')}
            max={new Date().toISOString().split('T')[0]}
            className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
          />
          {errors.expenseDate && (
            <p className="mt-1 text-sm text-red-600">{errors.expenseDate.message}</p>
          )}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Description *</label>
        <textarea
          rows={4}
          {...register('description')}
          className="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
          placeholder="Describe the expense..."
        />
        {errors.description && (
          <p className="mt-1 text-sm text-red-600">{errors.description.message}</p>
        )}
      </div>

      <div className="flex gap-3 pt-2">
        <button
          type="button"
          onClick={() => handleAction('save')}
          disabled={isLoading}
          className="px-6 py-2.5 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 disabled:opacity-50 font-medium"
        >
          {isLoading ? 'Saving...' : 'Save as Draft'}
        </button>
        <button
          type="button"
          onClick={() => handleAction('submit')}
          disabled={isLoading}
          className="px-6 py-2.5 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50 font-medium"
        >
          {isLoading ? 'Submitting...' : 'Submit for Approval'}
        </button>
      </div>
    </form>
  );
};

export default ExpenseForm;
