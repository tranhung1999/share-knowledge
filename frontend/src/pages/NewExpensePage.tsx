import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import ExpenseForm from '../components/expense/ExpenseForm';
import { ExpenseFormData } from '../types';
import expenseService from '../services/expenseService';
import toast from 'react-hot-toast';

const NewExpensePage: React.FC = () => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (data: ExpenseFormData, action: 'save' | 'submit') => {
    setIsLoading(true);
    try {
      const created = await expenseService.createExpense(data);
      if (action === 'submit') {
        await expenseService.submitExpense(created.data.id);
        toast.success('Expense submitted for approval');
      } else {
        toast.success('Expense saved as draft');
      }
      navigate('/expenses');
    } catch {
      toast.error('Failed to save expense');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-600 p-1">
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">New Expense</h1>
          <p className="text-gray-500">Submit a new expense claim</p>
        </div>
      </div>

      <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
        <ExpenseForm onSubmit={handleSubmit} isLoading={isLoading} />
      </div>
    </div>
  );
};

export default NewExpensePage;
