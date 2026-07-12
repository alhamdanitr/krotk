'use client';

import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Loader2, Zap, Lock, User } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { GuestGuard } from '@/components/layout/auth-guard';
import { useAuth } from '@/hooks/use-auth';
import { parseApiError } from '@/lib/utils';
import { cn } from '@/lib/utils';

// ─── Schema ───────────────────────────────────────────────────────────────────

const loginSchema = z.object({
  username: z
    .string()
    .min(3, 'اسم المستخدم يجب أن يكون 3 أحرف على الأقل')
    .max(100, 'اسم المستخدم طويل جداً'),
  password: z
    .string()
    .min(4, 'كلمة المرور يجب أن تكون 4 أحرف على الأقل'),
});

type LoginFormData = z.infer<typeof loginSchema>;

// ─── Form Field ───────────────────────────────────────────────────────────────

interface FormFieldProps {
  id: string;
  label: string;
  error?: string;
  children: React.ReactNode;
}

function FormField({ id, label, error, children }: FormFieldProps) {
  return (
    <div className="space-y-1.5">
      <label htmlFor={id} className="block text-sm font-medium text-foreground">
        {label}
      </label>
      {children}
      {error && (
        <p role="alert" className="text-xs text-destructive">
          {error}
        </p>
      )}
    </div>
  );
}

// ─── Login Form ───────────────────────────────────────────────────────────────

function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const { login } = useAuth();
  const router = useRouter();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: '', password: '' },
  });

  const onSubmit = async (values: LoginFormData) => {
    setApiError(null);
    try {
      await login(values);
      router.push('/dashboard');
    } catch (error) {
      setApiError(parseApiError(error));
    }
  };

  return (
    <form
      id="login-form"
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="space-y-5"
    >
      {/* Username */}
      <FormField id="username" label="اسم المستخدم" error={errors.username?.message}>
        <div className="relative">
          <User className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
          <input
            {...register('username')}
            id="username"
            type="text"
            autoComplete="username"
            placeholder="أدخل اسم المستخدم"
            disabled={isSubmitting}
            className={cn(
              'w-full rounded-lg border bg-input py-2.5 pr-10 pl-4 text-sm text-foreground placeholder:text-muted-foreground',
              'focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all',
              'disabled:opacity-50 disabled:cursor-not-allowed',
              errors.username && 'border-destructive focus:ring-destructive/50',
            )}
          />
        </div>
      </FormField>

      {/* Password */}
      <FormField id="password" label="كلمة المرور" error={errors.password?.message}>
        <div className="relative">
          <Lock className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground pointer-events-none" />
          <input
            {...register('password')}
            id="password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            placeholder="أدخل كلمة المرور"
            disabled={isSubmitting}
            className={cn(
              'w-full rounded-lg border bg-input py-2.5 pr-10 pl-10 text-sm text-foreground placeholder:text-muted-foreground',
              'focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary transition-all',
              'disabled:opacity-50 disabled:cursor-not-allowed',
              errors.password && 'border-destructive focus:ring-destructive/50',
            )}
          />
          <button
            type="button"
            tabIndex={-1}
            onClick={() => setShowPassword((v) => !v)}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
            aria-label={showPassword ? 'إخفاء كلمة المرور' : 'إظهار كلمة المرور'}
          >
            {showPassword ? (
              <EyeOff className="h-4 w-4" />
            ) : (
              <Eye className="h-4 w-4" />
            )}
          </button>
        </div>
      </FormField>

      {/* API Error */}
      {apiError && (
        <div
          role="alert"
          className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
        >
          {apiError}
        </div>
      )}

      {/* Submit */}
      <button
        type="submit"
        id="login-submit"
        disabled={isSubmitting}
        className={cn(
          'flex w-full items-center justify-center gap-2 rounded-lg bg-primary py-3 text-sm font-semibold text-primary-foreground',
          'hover:bg-primary/90 active:scale-[0.98] transition-all duration-150',
          'disabled:opacity-60 disabled:cursor-not-allowed disabled:active:scale-100',
          'shadow-lg shadow-primary/25',
        )}
      >
        {isSubmitting ? (
          <>
            <Loader2 className="h-4 w-4 animate-spin" />
            <span>جارٍ تسجيل الدخول…</span>
          </>
        ) : (
          'تسجيل الدخول'
        )}
      </button>
    </form>
  );
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function LoginPage() {
  return (
    <GuestGuard>
      <div className="flex min-h-screen items-center justify-center bg-background p-4">
        {/* Background decoration */}
        <div className="pointer-events-none fixed inset-0 overflow-hidden">
          <div className="absolute -top-40 -right-40 h-96 w-96 rounded-full bg-primary/5 blur-3xl" />
          <div className="absolute -bottom-40 -left-40 h-96 w-96 rounded-full bg-blue-500/5 blur-3xl" />
        </div>

        <div className="relative w-full max-w-[400px] space-y-8">
          {/* Header */}
          <div className="text-center space-y-3">
            {/* Logo */}
            <div className="flex justify-center">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-primary shadow-xl shadow-primary/30">
                <Zap className="h-8 w-8 text-white" />
              </div>
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-foreground">
                مرحباً بك في Kurotek
              </h1>
              <p className="mt-1 text-sm text-muted-foreground">
                سجّل دخولك للوصول إلى لوحة التحكم
              </p>
            </div>
          </div>

          {/* Card */}
          <div className="rounded-2xl border border-border bg-card p-8 shadow-xl shadow-black/5 dark:shadow-black/30">
            <LoginForm />
          </div>

          {/* Footer */}
          <p className="text-center text-xs text-muted-foreground">
            منصة Kurotek لإدارة كروت الشحن وشبكات التوزيع
          </p>
        </div>
      </div>
    </GuestGuard>
  );
}
