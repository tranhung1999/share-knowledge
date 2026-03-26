import apiClient from './api';
import { ApiResponse, User } from '../types';

interface LoginPayload { email: string; password: string }
interface AuthResponse { accessToken: string; tokenType: string; expiresIn: number; user: User }

const authService = {
  login: (credentials: LoginPayload) =>
    apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials)
      .then(r => r.data),
};

export default authService;
