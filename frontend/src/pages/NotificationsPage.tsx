import React, { useEffect } from 'react';
import { Bell, CheckCheck } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '../hooks/useAppDispatch';
import {
  fetchNotifications, markNotificationRead, markAllNotificationsRead
} from '../store/slices/notificationSlice';
import LoadingSpinner from '../components/common/LoadingSpinner';
import { formatDateTime } from '../utils/formatters';
import { useNavigate } from 'react-router-dom';

const NotificationsPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { notifications, unreadCount, isLoading } = useAppSelector(s => s.notifications);

  useEffect(() => { dispatch(fetchNotifications()); }, [dispatch]);

  const handleClick = (id: number, expenseId: number | null, isRead: boolean) => {
    if (!isRead) dispatch(markNotificationRead(id));
    if (expenseId) navigate(`/expenses/${expenseId}`);
  };

  const TYPE_COLORS: Record<string, string> = {
    EXPENSE_SUBMITTED: 'bg-blue-100 text-blue-800',
    EXPENSE_APPROVED:  'bg-green-100 text-green-800',
    EXPENSE_REJECTED:  'bg-red-100 text-red-800',
    EXPENSE_PAID:      'bg-purple-100 text-purple-800',
    GENERAL:           'bg-gray-100 text-gray-800',
  };

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Notifications</h1>
          {unreadCount > 0 && (
            <p className="text-primary-600 text-sm">{unreadCount} unread</p>
          )}
        </div>
        {unreadCount > 0 && (
          <button
            onClick={() => dispatch(markAllNotificationsRead())}
            className="flex items-center gap-2 text-sm text-primary-600 hover:text-primary-700"
          >
            <CheckCheck className="h-4 w-4" /> Mark all as read
          </button>
        )}
      </div>

      {isLoading ? (
        <LoadingSpinner className="py-12" />
      ) : notifications.length === 0 ? (
        <div className="bg-white rounded-xl p-12 text-center shadow-sm border border-gray-100">
          <Bell className="h-12 w-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">No notifications yet</p>
        </div>
      ) : (
        <div className="space-y-2">
          {notifications.map(n => (
            <div
              key={n.id}
              onClick={() => handleClick(n.id, n.expenseId, n.isRead)}
              className={`bg-white rounded-xl p-4 shadow-sm border transition-colors cursor-pointer
                ${!n.isRead ? 'border-primary-200 bg-primary-50' : 'border-gray-100 hover:border-gray-200'}`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${TYPE_COLORS[n.type]}`}>
                      {n.type.replace(/_/g, ' ')}
                    </span>
                    {!n.isRead && (
                      <span className="h-2 w-2 rounded-full bg-primary-500 flex-shrink-0" />
                    )}
                  </div>
                  <p className="text-sm font-semibold text-gray-900">{n.title}</p>
                  <p className="text-sm text-gray-600 mt-0.5">{n.message}</p>
                </div>
                <p className="text-xs text-gray-400 whitespace-nowrap flex-shrink-0">
                  {formatDateTime(n.createdAt)}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default NotificationsPage;
