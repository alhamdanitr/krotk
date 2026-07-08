'use client';

import React from 'react';

interface ServiceCardProps {
  icon: React.ReactNode;
  title: string;
  subtitle?: string;
  onClick?: () => void;
  className?: string;
  badge?: string;
}

export function ServiceCard({
  icon,
  title,
  subtitle,
  onClick,
  className = '',
  badge,
}: ServiceCardProps) {
  return (
    <button
      onClick={onClick}
      className={`relative bg-card rounded-xl p-4 flex flex-col items-center justify-center gap-2 transition-all hover:shadow-md active:scale-95 border border-border/50 ${className}`}
    >
      <div className="text-3xl text-primary">{icon}</div>
      <h3 className="text-sm font-semibold text-foreground text-center leading-tight">
        {title}
      </h3>
      {subtitle && (
        <p className="text-xs text-muted-foreground text-center">{subtitle}</p>
      )}
      {badge && (
        <span className="absolute -top-2 -right-2 bg-primary text-primary-foreground text-xs px-2 py-1 rounded-full font-bold">
          {badge}
        </span>
      )}
    </button>
  );
}
