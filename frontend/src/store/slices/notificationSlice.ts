import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { Notification, PageResponse } from '../../types';
import notificationService from '../../services/notificationService';

interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  totalElements: number;
  isLoading: boolean;
}

const initialState: NotificationState = {
  notifications: [],
  unreadCount: 0,
  totalElements: 0,
  isLoading: false,
};

export const fetchNotifications = createAsyncThunk(
  'notifications/fetch',
  async (params: { page?: number; size?: number } = {}) => {
    const response = await notificationService.getNotifications(params.page, params.size);
    return response.data;
  }
);

export const fetchUnreadCount = createAsyncThunk(
  'notifications/unreadCount',
  async () => {
    const response = await notificationService.getUnreadCount();
    return response.data.unreadCount;
  }
);

export const markNotificationRead = createAsyncThunk(
  'notifications/markRead',
  async (id: number) => {
    await notificationService.markAsRead(id);
    return id;
  }
);

export const markAllNotificationsRead = createAsyncThunk(
  'notifications/markAllRead',
  async () => {
    await notificationService.markAllAsRead();
  }
);

const notificationSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.pending, (state) => { state.isLoading = true; })
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.isLoading = false;
        state.notifications = (action.payload as PageResponse<Notification>).content;
        state.totalElements = (action.payload as PageResponse<Notification>).totalElements;
      })
      .addCase(fetchNotifications.rejected, (state) => { state.isLoading = false; })
      .addCase(fetchUnreadCount.fulfilled, (state, action) => {
        state.unreadCount = action.payload as number;
      })
      .addCase(markNotificationRead.fulfilled, (state, action) => {
        const n = state.notifications.find(n => n.id === action.payload);
        if (n) { n.isRead = true; state.unreadCount = Math.max(0, state.unreadCount - 1); }
      })
      .addCase(markAllNotificationsRead.fulfilled, (state) => {
        state.notifications.forEach(n => { n.isRead = true; });
        state.unreadCount = 0;
      });
  },
});

export default notificationSlice.reducer;
