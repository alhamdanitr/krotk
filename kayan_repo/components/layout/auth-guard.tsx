'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/hooks/use-auth';
import { PageLoader } from '@/components/ui/loading';

// ─── Auth Guard ────────────────────────────────────────────────────────────────

interface AuthGuardProps {
  children: React.ReactNode;
  allowedRoles?: string[];
}

/**
 * Wraps protected pages.
 * - Redirects unauthenticated users to /login.
 * - Optionally restricts by role.
 */
export function AuthGuard({ children, allowedRoles }: AuthGuardProps) {
  const { isAuthenticated, isLoading, user } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (isLoading) return;
    if (!isAuthenticated) {
      router.replace('/login');
      return;
    }
    if (allowedRoles && user && !allowedRoles.includes(user.role)) {
      router.replace('/dashboard'); // Redirect to safe page
    }
  }, [isAuthenticated, isLoading, user, allowedRoles, router]);

  if (isLoading) return <PageLoader />;
  if (!isAuthenticated) return null;
  if (allowedRoles && user && !allowedRoles.includes(user.role)) return null;

  return <>{children}</>;
}

// ─── Guest Guard (redirect authenticated users away from login) ───────────────

interface GuestGuardProps {
  children: React.ReactNode;
}

export function GuestGuard({ children }: GuestGuardProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.replace('/dashboard');
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading) return <PageLoader />;
  if (isAuthenticated) return null;

  return <>{children}</>;
}
