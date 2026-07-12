'use client';

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/auth.service';
import type { AuthUser, LoginCredentials, LoginResponse, TenantStatus } from '@/types';

// ─── Context Shape ────────────────────────────────────────────────────────────

interface AuthContextValue {
  user: AuthUser | null;
  tenant: TenantStatus | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<LoginResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

// ─── Provider ─────────────────────────────────────────────────────────────────

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [tenant, setTenant] = useState<TenantStatus | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Hydrate from localStorage on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('kurotek_user');
    const storedTenant = localStorage.getItem('kurotek_tenant');
    const hasToken = authService.isAuthenticated();

    if (hasToken && storedUser) {
      try {
        setUser(JSON.parse(storedUser) as AuthUser);
        if (storedTenant && storedTenant !== 'null') {
          setTenant(JSON.parse(storedTenant) as TenantStatus);
        }
      } catch {
        authService.logout();
      }
    }
    setIsLoading(false);
  }, []);

  const login = useCallback(
    async (credentials: LoginCredentials): Promise<LoginResponse> => {
      const response = await authService.login(credentials);
      setUser(response.user);
      setTenant(response.tenant);
      return response;
    },
    [],
  );

  const logout = useCallback(() => {
    authService.logout();
    setUser(null);
    setTenant(null);
    router.push('/login');
  }, [router]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      tenant,
      isAuthenticated: Boolean(user),
      isLoading,
      login,
      logout,
    }),
    [user, tenant, isLoading, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used inside <AuthProvider>');
  }
  return ctx;
}
