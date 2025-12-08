import apiClient from './api';

export interface StatsResponse {
    slowApiCount: number;
    brokenApiCount: number;
    rateLimitViolations: number;
    averageLatency: number;
}

export interface TopEndpoint {
    serviceName: string;
    endpoint: string;
    averageLatency: number;
    hitCount: number;
}

export interface TimeSeries {
    timestamp: string;
    errorCount: number;
    totalCount: number;
    errorRate: number;
}

export const getStatsSummary = async (): Promise<StatsResponse> => {
    const response = await apiClient.get<StatsResponse>('/stats/summary');
    return response.data;
};

export const getTopSlowEndpoints = async (limit: number = 10): Promise<TopEndpoint[]> => {
    const response = await apiClient.get<TopEndpoint[]>(`/stats/top-slow-endpoints?limit=${limit}`);
    return response.data;
};

export const getErrorRateTimeSeries = async (hours: number = 24): Promise<TimeSeries[]> => {
    const response = await apiClient.get<TimeSeries[]>(`/stats/error-rate-time-series?hours=${hours}`);
    return response.data;
};
