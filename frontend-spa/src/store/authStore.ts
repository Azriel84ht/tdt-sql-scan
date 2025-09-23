import { create } from 'zustand';
import apiClient from '../api/apiClient';

import { toast } from 'react-hot-toast';

interface AuthState {
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  setToken: (token: string | null) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  isAuthenticated: !!localStorage.getItem('token'),
  isLoading: false,
  setToken: (token) => {
    set({ token, isAuthenticated: !!token });
    if (token) {
      localStorage.setItem('token', token);
    } else {
      localStorage.removeItem('token');
    }
  },
  login: async (username, password) => {
    set({ isLoading: true });
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
      toast.error(errorMessage);
      set({ isLoading: false });
    }
  },
  logout: () => {
    set({ token: null, isAuthenticated: false });
    localStorage.removeItem('token');
  },
}));
