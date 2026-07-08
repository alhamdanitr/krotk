'use client';

import React from 'react';

interface AccountCardProps {
  accountName: string;
  accountNumber: string;
  balance: string;
  holderName: string;
  cardType?: 'visa' | 'mastercard' | 'generic';
  onClick?: () => void;
  isActive?: boolean;
}

export function AccountCard({
  accountName,
  accountNumber,
  balance,
  holderName,
  cardType = 'generic',
  onClick,
  isActive = false,
}: AccountCardProps) {
  return (
    <button
      onClick={onClick}
      className={`relative w-full bg-gradient-to-br from-primary to-primary/90 text-primary-foreground rounded-2xl p-6 shadow-lg transition-all ${
        isActive ? 'ring-2 ring-offset-2 ring-primary' : ''
      }`}
    >
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-10 rounded-2xl overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-white to-transparent" />
      </div>

      {/* Content */}
      <div className="relative z-10">
        <div className="flex justify-between items-start mb-8">
          <div>
            <p className="text-sm font-medium opacity-90">{accountName}</p>
            <p className="text-2xl font-bold">{balance}</p>
          </div>
          <div className="w-12 h-8 bg-white/20 rounded-lg flex items-center justify-center">
            {cardType === 'visa' && <span className="text-xs font-bold">V</span>}
            {cardType === 'mastercard' && (
              <div className="flex gap-0.5">
                <div className="w-2.5 h-2.5 bg-red-400 rounded-full" />
                <div className="w-2.5 h-2.5 bg-yellow-400 rounded-full" />
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-between items-end">
          <div>
            <p className="text-xs opacity-75 mb-1">رقم الحساب</p>
            <p className="text-lg font-mono">{accountNumber}</p>
          </div>
        </div>

        <div className="mt-6 pt-4 border-t border-white/20">
          <p className="text-xs opacity-75">{holderName}</p>
        </div>
      </div>
    </button>
  );
}
