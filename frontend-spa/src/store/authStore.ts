import { create } from 'zustand';
import apiClient from '../api/apiClient';

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  setToken: (token: string | null) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  isAuthenticated: !!localStorage.getItem('token'),
  isLoading: false,
  error: null,
  setToken: (token) => {
    set({ token, isAuthenticated: !!token, error: null });
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  },
  login: async (username, password) => {
    set({ isLoading: true, error: null });
    try {
      const response = await apiClient.post('/api/v1/auth/login', { username, password });
      const { token } = response.data;
      set({ token, isAuthenticated: true, isLoading: false });
      localStorage.setItem('token', token);
    } catch (error: unknown) {
      let errorMessage = 'An unexpected error occurred.';
      if (typeof error === 'object' && error !== null && 'response' in error) {
        const response = (error as { response?: { data?: { message?: string } } }).response;
        if (response?.data?.message) {
          errorMessage = response.data.message;
        }
      }
      set({ error: errorMessage, isLoading: false });
    }
  },
  logout: () => {
    set({ token: null, isAuthenticated: false });
    localStorage.removeItem('token');
  },
}));
