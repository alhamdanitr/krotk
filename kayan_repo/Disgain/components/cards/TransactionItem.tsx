'use client';

import React from 'react';

interface TransactionItemProps {
  icon: React.ReactNode;
  title: string;
  date: string;
  amount: string;
  amountType?: 'positive' | 'negative';
  onClick?: () => void;
  status?: 'completed' | 'pending' | 'failed';
}

export function TransactionItem({
  icon,
  title,
  date,
  amount,
  amountType = 'negative',
  onClick,
  status = 'completed',
}: TransactionItemProps) {
  const amountColor =
    amountType === 'positive' ? 'text-success-green' : 'text-primary';
  const amountPrefix = amountType === 'positive' ? '+' : '-';

  return (
    <button
      onClick={onClick}
      className="w-full bg-card rounded-xl p-4 flex items-center justify-between gap-4 border border-border/50 transition-all hover:shadow-md active:scale-95"
    >
      <div className="flex items-center gap-3 flex-1">
        <div className="w-12 h-12 bg-muted rounded-full flex items-center justify-center text-primary">
          {icon}
        </div>
        <div className="flex-1 text-right">
          <p className="text-sm font-semibold text-foreground">{title}</p>
          <p className="text-xs text-muted-foreground">{date}</p>
        </div>
      </div>

      <div className="text-right">
        <p className={`text-sm font-bold ${amountColor}`}>
          {amountPrefix}
          {amount} ريال يمني
        </p>
        {status === 'pending' && (
          <span className="text-xs text-warning-orange">قيد الانتظار</span>
        )}
        {status === 'failed' && (
          <span className="text-xs text-error-red">فشل</span>
        )}
      </div>
    </button>
  );
}
