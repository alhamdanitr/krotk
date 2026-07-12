import apiClient, { tokenStorage } from '@/lib/api-client';
import type {
  LoginCredentials,
  LoginResponse,
  RegisterTenantPayload,
  ActivateSerialPayload,
} from '@/types';

// ─── Auth Service ─────────────────────────────────────────────────────────────

export const authService = {
  /**
   * POST /api/auth/login
   * Authenticates user and stores tokens in localStorage.
   */
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const { data } = await apiClient.post<LoginResponse>('/auth/login', credentials);
    tokenStorage.set(data.accessToken, data.refreshToken);
    // Persist user for layout
    localStorage.setItem('kurotek_user', JSON.stringify(data.user));
    localStorage.setItem('kurotek_tenant', JSON.stringify(data.tenant));
    return data;
  },

  /**
   * POST /api/auth/wizard/register
   * Registers a new tenant with 7-day trial.
   */
  async register(payload: RegisterTenantPayload): Promise<{ message: string; tenantId: string }> {
    const { data } = await apiClient.post('/auth/wizard/register', payload);
    return data;
  },

  /**
   * POST /api/auth/activate
   * Activates subscription using serial key (requires JWT).
   */
  async activate(payload: ActivateSerialPayload): Promise<{ message: string; expiryDate: string }> {
    const { data } = await apiClient.post('/auth/activate', payload);
    return data;
  },

  /**
   * Logout — clear all tokens from storage.
   */
  logout(): void {
    tokenStorage.clear();
  },

  /**
   * Check if user is authenticated (token present).
   */
  isAuthenticated(): boolean {
    return Boolean(tokenStorage.getAccess());
  },
};
