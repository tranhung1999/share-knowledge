import React from 'react';

interface Props {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizes = { sm: 'h-4 w-4', md: 'h-8 w-8', lg: 'h-12 w-12' };

const LoadingSpinner: React.FC<Props> = ({ size = 'md', className = '' }) => (
  <div className={`flex justify-center items-center ${className}`}>
    <div className={`animate-spin rounded-full border-4 border-gray-200 border-t-primary-600 ${sizes[size]}`} />
  </div>
);

export default LoadingSpinner;
