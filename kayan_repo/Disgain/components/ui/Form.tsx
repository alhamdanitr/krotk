'use client';

import React from 'react';

interface FormProps extends React.FormHTMLAttributes<HTMLFormElement> {
  children: React.ReactNode;
}

interface FormFieldProps {
  label?: string;
  error?: string;
  required?: boolean;
  children: React.ReactNode;
}

interface FormGroupProps {
  children: React.ReactNode;
}

export function Form({ children, className = '', ...props }: FormProps) {
  return (
    <form className={`w-full space-y-6 ${className}`} {...props}>
      {children}
    </form>
  );
}

export function FormField({
  label,
  error,
  required = false,
  children,
}: FormFieldProps) {
  return (
    <div className="w-full space-y-2">
      {label && (
        <label className="block text-sm font-semibold text-foreground">
          {label}
          {required && <span className="text-error-red ml-1">*</span>}
        </label>
      )}
      {children}
      {error && <p className="text-xs text-error-red font-medium">{error}</p>}
    </div>
  );
}

export function FormGroup({ children }: FormGroupProps) {
  return <div className="space-y-3">{children}</div>;
}
