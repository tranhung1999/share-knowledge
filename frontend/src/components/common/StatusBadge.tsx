import React from 'react';
import { ExpenseStatus } from '../../types';
import { STATUS_COLORS } from '../../utils/formatters';

interface Props {
  status: ExpenseStatus;
}

const StatusBadge: React.FC<Props> = ({ status }) => (
  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[status]}`}>
    {status}
  </span>
);

export default StatusBadge;
