import { format, parseISO } from 'date-fns';

export const formatCurrency = (amount: number): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);

export const formatDate = (dateStr: string): string => {
  try {
    return format(parseISO(dateStr), 'MMM dd, yyyy');
  } catch {
    return dateStr;
  }
};

export const formatDateTime = (dateStr: string): string => {
  try {
    return format(parseISO(dateStr), 'MMM dd, yyyy HH:mm');
  } catch {
    return dateStr;
  }
};

export const formatFileSize = (bytes: number): string => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

export const STATUS_COLORS: Record<string, string> = {
  DRAFT:     'bg-gray-100 text-gray-800',
  SUBMITTED: 'bg-blue-100 text-blue-800',
  APPROVED:  'bg-green-100 text-green-800',
  REJECTED:  'bg-red-100 text-red-800',
  PAID:      'bg-purple-100 text-purple-800',
};

export const EXPENSE_TYPE_LABELS: Record<string, string> = {
  TRAVEL:         'Travel',
  ACCOMMODATION:  'Accommodation',
  MEALS:          'Meals',
  OFFICE_SUPPLIES:'Office Supplies',
  TRAINING:       'Training',
  ENTERTAINMENT:  'Entertainment',
  MEDICAL:        'Medical',
  OTHER:          'Other',
};
