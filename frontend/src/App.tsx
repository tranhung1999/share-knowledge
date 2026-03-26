import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import { Toaster } from 'react-hot-toast';
import { store } from './store';
import { useAppSelector } from './hooks/useAppDispatch';
import MainLayout from './components/layout/MainLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ExpensesPage from './pages/ExpensesPage';
import NewExpensePage from './pages/NewExpensePage';
import ExpenseDetailPage from './pages/ExpenseDetailPage';
import AllExpensesPage from './pages/AllExpensesPage';
import ReportsPage from './pages/ReportsPage';
import NotificationsPage from './pages/NotificationsPage';
import { UserRole } from './types';

const ProtectedRoute: React.FC<{
  children: React.ReactNode;
  allowedRoles?: UserRole[];
}> = ({ children, allowedRoles }) => {
  const { isAuthenticated, user } = useAppSelector(s => s.auth);

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (allowedRoles && user && !allowedRoles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }
  return <>{children}</>;
};

const AppRoutes: React.FC = () => {
  const { isAuthenticated } = useAppSelector(s => s.auth);

  return (
    <Routes>
      <Route path="/login" element={
        isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />
      } />

      <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/expenses" element={
          <ProtectedRoute allowedRoles={['EMPLOYEE']}>
            <ExpensesPage />
          </ProtectedRoute>
        } />
        <Route path="/expenses/new" element={
          <ProtectedRoute allowedRoles={['EMPLOYEE']}>
            <NewExpensePage />
          </ProtectedRoute>
        } />
        <Route path="/expenses/:id" element={<ExpenseDetailPage />} />
        <Route path="/all-expenses" element={
          <ProtectedRoute allowedRoles={['MANAGER', 'ACCOUNTANT', 'ADMIN']}>
            <AllExpensesPage />
          </ProtectedRoute>
        } />
        <Route path="/approvals" element={
          <ProtectedRoute allowedRoles={['MANAGER', 'ADMIN']}>
            <AllExpensesPage />
          </ProtectedRoute>
        } />
        <Route path="/payments" element={
          <ProtectedRoute allowedRoles={['ACCOUNTANT', 'ADMIN']}>
            <AllExpensesPage />
          </ProtectedRoute>
        } />
        <Route path="/reports" element={
          <ProtectedRoute allowedRoles={['MANAGER', 'ACCOUNTANT', 'ADMIN']}>
            <ReportsPage />
          </ProtectedRoute>
        } />
        <Route path="/notifications" element={<NotificationsPage />} />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};

const App: React.FC = () => (
  <Provider store={store}>
    <BrowserRouter>
      <AppRoutes />
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: { borderRadius: '10px', fontFamily: 'system-ui, sans-serif' },
        }}
      />
    </BrowserRouter>
  </Provider>
);

export default App;
