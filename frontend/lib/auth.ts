import apiClient from './api';
import Cookies from 'js-cookie';

export interface SignupData {
    username: string;
    email: string;
    password: string;
    role?: string;
}

export interface LoginData {
    email: string;
    password: string;
}

export interface User {
    id: string;
    username: string;
    email: string;
    role: string;
}

export interface AuthResponse {
    token: string;
    user: User;
}

export const signup = async (data: SignupData): Promise<User> => {
    const response = await apiClient.post<User>('/auth/signup', data);
    return response.data;
};

export const login = async (data: LoginData): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', data);

    // Store token and user in cookies
    Cookies.set('monitorx_token', response.data.token, { expires: 1 }); // 1 day
    Cookies.set('monitorx_user', JSON.stringify(response.data.user), { expires: 1 });

    return response.data;
};

export const logout = () => {
    Cookies.remove('monitorx_token');
    Cookies.remove('monitorx_user');
    window.location.href = '/login';
};

export const getCurrentUser = (): User | null => {
    const userStr = Cookies.get('monitorx_user');
    if (!userStr) return null;
    try {
        return JSON.parse(userStr);
    } catch {
        return null;
    }
};

export const isAuthenticated = (): boolean => {
    return !!Cookies.get('monitorx_token');
};
