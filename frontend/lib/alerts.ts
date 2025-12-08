import apiClient from './api';

export enum AlertType {
    SLOW_API = 'SLOW_API',
    BROKEN_API = 'BROKEN_API',
    RATE_LIMIT = 'RATE_LIMIT',
}

export interface Alert {
    id: string;
    serviceName: string;
    endpoint: string;
    alertType: AlertType;
    reason: string;
    timestamp: string;
}

export interface AlertsFilter {
    alertType?: string;
    serviceName?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
}

export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export const getAlerts = async (filter: AlertsFilter = {}): Promise<PageResponse<Alert>> => {
    const params = new URLSearchParams();

    Object.entries(filter).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            params.append(key, String(value));
        }
    });

    const response = await apiClient.get<PageResponse<Alert>>(`/alerts?${params.toString()}`);
    return response.data;
};
