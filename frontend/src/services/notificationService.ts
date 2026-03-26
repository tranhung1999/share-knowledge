import apiClient from './api';
import { ApiResponse, PageResponse, Notification } from '../types';

const notificationService = {
  getNotifications: (page = 0, size = 20) =>
    apiClient.get<ApiResponse<PageResponse<Notification>>>('/notifications', {
      params: { page, size }
    }).then(r => r.data),

  getUnreadCount: () =>
    apiClient.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count')
      .then(r => r.data),

  markAsRead: (id: number) =>
    apiClient.patch<ApiResponse<void>>(`/notifications/${id}/read`).then(r => r.data),

  markAllAsRead: () =>
    apiClient.patch<ApiResponse<void>>('/notifications/read-all').then(r => r.data),
};

export default notificationService;
