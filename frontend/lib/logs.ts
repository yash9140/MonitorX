import apiClient from './api';

export interface ApiLog {
    id: string;
    serviceName: string;
    method: string;
    endpoint: string;
    timestamp: string;
    latency: number;
    statusCode: number;
    requestSize: number;
    responseSize: number;
}

export interface LogsFilter {
    serviceName?: string;
    endpoint?: string;
    minStatusCode?: number;
    maxStatusCode?: number;
    slowOnly?: boolean;
    brokenOnly?: boolean;
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

export const getLogs = async (filter: LogsFilter = {}): Promise<PageResponse<ApiLog>> => {
    const params = new URLSearchParams();

    Object.entries(filter).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            params.append(key, String(value));
        }
    });

    const response = await apiClient.get<PageResponse<ApiLog>>(`/logs?${params.toString()}`);
    return response.data;
};
