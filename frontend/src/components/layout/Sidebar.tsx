import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, FileText, CheckSquare, BarChart2,
  Bell, Users, LogOut, DollarSign
} from 'lucide-react';
import { useAppDispatch, useAppSelector } from '../../hooks/useAppDispatch';
import { logout } from '../../store/slices/authSlice';
import { UserRole } from '../../types';

interface NavItem {
  to: string;
  label: string;
  icon: React.ReactNode;
  roles: UserRole[];
}

const NAV_ITEMS: NavItem[] = [
  { to: '/dashboard',     label: 'Dashboard',   icon: <LayoutDashboard className="h-5 w-5" />, roles: ['ADMIN','MANAGER','ACCOUNTANT','EMPLOYEE'] },
  { to: '/expenses',      label: 'My Expenses',  icon: <FileText className="h-5 w-5" />,        roles: ['EMPLOYEE'] },
  { to: '/approvals',     label: 'Approvals',    icon: <CheckSquare className="h-5 w-5" />,     roles: ['MANAGER','ADMIN'] },
  { to: '/payments',      label: 'Payments',     icon: <DollarSign className="h-5 w-5" />,      roles: ['ACCOUNTANT','ADMIN'] },
  { to: '/all-expenses',  label: 'All Expenses', icon: <FileText className="h-5 w-5" />,        roles: ['MANAGER','ACCOUNTANT','ADMIN'] },
  { to: '/reports',       label: 'Reports',      icon: <BarChart2 className="h-5 w-5" />,       roles: ['MANAGER','ACCOUNTANT','ADMIN'] },
  { to: '/notifications', label: 'Notifications',icon: <Bell className="h-5 w-5" />,            roles: ['ADMIN','MANAGER','ACCOUNTANT','EMPLOYEE'] },
  { to: '/users',         label: 'Users',        icon: <Users className="h-5 w-5" />,           roles: ['ADMIN'] },
];

const Sidebar: React.FC = () => {
  const dispatch = useAppDispatch();
  const user = useAppSelector(s => s.auth.user);
  const unreadCount = useAppSelector(s => s.notifications.unreadCount);

  const visibleItems = NAV_ITEMS.filter(
    item => user?.role && item.roles.includes(user.role)
  );

  return (
    <aside className="w-64 min-h-screen bg-gray-900 text-white flex flex-col">
      <div className="p-6 border-b border-gray-700">
        <div className="flex items-center gap-2">
          <DollarSign className="h-8 w-8 text-primary-400" />
          <div>
            <p className="font-bold text-lg leading-tight">ExpenseMS</p>
            <p className="text-xs text-gray-400">Management System</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {visibleItems.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-md text-sm transition-colors
               ${isActive
                 ? 'bg-primary-600 text-white'
                 : 'text-gray-300 hover:bg-gray-700 hover:text-white'}`
            }
          >
            {item.icon}
            <span>{item.label}</span>
            {item.to === '/notifications' && unreadCount > 0 && (
              <span className="ml-auto bg-red-500 text-white text-xs rounded-full px-2 py-0.5">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-700">
        <div className="mb-3 px-3 py-2">
          <p className="text-sm font-medium text-white truncate">{user?.fullName}</p>
          <p className="text-xs text-gray-400 truncate">{user?.email}</p>
          <span className="text-xs bg-primary-700 text-primary-200 px-2 py-0.5 rounded mt-1 inline-block">
            {user?.role}
          </span>
        </div>
        <button
          onClick={() => dispatch(logout())}
          className="flex items-center gap-3 w-full px-3 py-2.5 rounded-md text-sm text-gray-300
                     hover:bg-gray-700 hover:text-white transition-colors"
        >
          <LogOut className="h-5 w-5" />
          Sign Out
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
