import React, { useEffect, useState } from 'react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from 'recharts';
import { DollarSign, FileText, Clock, CheckCircle, XCircle, TrendingUp } from 'lucide-react';
import { DashboardSummary } from '../types';
import reportService from '../services/reportService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatCurrency } from '../utils/formatters';
import toast from 'react-hot-toast';

const PIE_COLORS = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899','#14b8a6','#f97316'];

const StatCard: React.FC<{
  title: string; value: string | number; icon: React.ReactNode; color: string;
}> = ({ title, value, icon, color }) => (
  <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-gray-500">{title}</p>
        <p className="text-2xl font-bold text-gray-900 mt-1">{value}</p>
      </div>
      <div className={`p-3 rounded-full ${color}`}>{icon}</div>
    </div>
  </div>
);

const DashboardPage: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    reportService.getDashboard()
      .then(r => setSummary(r.data))
      .catch(() => toast.error('Failed to load dashboard'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner size="lg" className="mt-20" />;
  if (!summary) return <p className="text-gray-500">No data available.</p>;

  const statusData = [
    { name: 'Draft',     value: summary.draftCount },
    { name: 'Submitted', value: summary.submittedCount },
    { name: 'Approved',  value: summary.approvedCount },
    { name: 'Rejected',  value: summary.rejectedCount },
    { name: 'Paid',      value: summary.paidCount },
  ].filter(d => d.value > 0);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-500 mt-1">Overview of expense activity</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard title="Total Expenses"  value={summary.totalExpenses}
          icon={<FileText className="h-6 w-6 text-blue-600" />}    color="bg-blue-100" />
        <StatCard title="Draft"           value={summary.draftCount}
          icon={<Clock className="h-6 w-6 text-gray-600" />}       color="bg-gray-100" />
        <StatCard title="Pending Review"  value={summary.submittedCount}
          icon={<Clock className="h-6 w-6 text-yellow-600" />}     color="bg-yellow-100" />
        <StatCard title="Approved"        value={summary.approvedCount}
          icon={<CheckCircle className="h-6 w-6 text-green-600" />} color="bg-green-100" />
        <StatCard title="Rejected"        value={summary.rejectedCount}
          icon={<XCircle className="h-6 w-6 text-red-600" />}      color="bg-red-100" />
        <StatCard title="Paid"            value={summary.paidCount}
          icon={<DollarSign className="h-6 w-6 text-purple-600" />} color="bg-purple-100" />
      </div>

      {/* Financial Summary */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <div className="flex items-center gap-3">
            <div className="bg-green-100 p-3 rounded-full">
              <DollarSign className="h-6 w-6 text-green-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">Total Paid (YTD)</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(summary.totalPaidAmount)}
              </p>
            </div>
          </div>
        </div>
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
          <div className="flex items-center gap-3">
            <div className="bg-blue-100 p-3 rounded-full">
              <TrendingUp className="h-6 w-6 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-500">This Month Paid</p>
              <p className="text-2xl font-bold text-gray-900">
                {formatCurrency(summary.currentMonthPaidAmount)}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Monthly Trend Chart */}
        {summary.last6MonthsTrend.length > 0 && (
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Monthly Trend</h3>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={summary.last6MonthsTrend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="monthName" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip formatter={(v: number) => formatCurrency(v)} />
                <Bar dataKey="totalAmount" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Status Distribution */}
        {statusData.length > 0 && (
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Status Distribution</h3>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={statusData} cx="50%" cy="50%" innerRadius={60} outerRadius={90}
                     dataKey="value" label={({ name, percent }) =>
                       `${name} ${(percent * 100).toFixed(0)}%`}>
                  {statusData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}

        {/* Top Expense Types */}
        {summary.topExpenseTypes.length > 0 && (
          <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Top Expense Types</h3>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={summary.topExpenseTypes} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" tick={{ fontSize: 12 }}
                       tickFormatter={(v: number) => `$${(v / 1000).toFixed(0)}k`} />
                <YAxis type="category" dataKey="expenseType" tick={{ fontSize: 12 }} width={100} />
                <Tooltip formatter={(v: number) => formatCurrency(v)} />
                <Bar dataKey="totalAmount" fill="#10b981" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
