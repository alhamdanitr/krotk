'use client';

import React, { useState } from 'react';
import { ChevronDown } from 'lucide-react';

interface SettingsMenuItemProps {
  icon: React.ReactNode;
  title: string;
  expandable?: boolean;
  onClick?: () => void;
  children?: React.ReactNode;
  isOpen?: boolean;
}

export function SettingsMenuItem({
  icon,
  title,
  expandable = false,
  onClick,
  children,
  isOpen: controlledIsOpen,
}: SettingsMenuItemProps) {
  const [isOpen, setIsOpen] = useState(false);
  const open = controlledIsOpen ?? isOpen;

  const handleClick = () => {
    if (expandable) {
      setIsOpen(!open);
    } else {
      onClick?.();
    }
  };

  return (
    <>
      <button
        onClick={handleClick}
        className="w-full bg-card rounded-xl p-4 flex items-center justify-between gap-3 border border-border/50 transition-all hover:bg-muted active:scale-95"
      >
        <div className="flex items-center gap-3 flex-1">
          <div className="w-10 h-10 bg-muted rounded-lg flex items-center justify-center text-primary text-xl">
            {icon}
          </div>
          <span className="text-sm font-semibold text-foreground text-right">
            {title}
          </span>
        </div>
        {expandable && (
          <ChevronDown
            size={20}
            className={`text-muted-foreground transition-transform ${
              open ? 'rotate-180' : ''
            }`}
          />
        )}
      </button>
      {expandable && open && children && (
        <div className="mt-2 space-y-2 pl-4">{children}</div>
      )}
    </>
  );
}
