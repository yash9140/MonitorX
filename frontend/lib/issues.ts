import apiClient from './api';

export enum IssueStatus {
    OPEN = 'OPEN',
    RESOLVED = 'RESOLVED',
}

export enum IssueType {
    SLOW_API = 'SLOW_API',
    BROKEN_API = 'BROKEN_API',
    RATE_LIMIT = 'RATE_LIMIT',
}

export interface Issue {
    id: string;
    serviceName: string;
    endpoint: string;
    issueType: IssueType;
    status: IssueStatus;
    hitCount: number;
    firstSeenAt: string;
    lastSeenAt: string;
    resolvedAt?: string;
    resolvedBy?: string;
    version?: number;
}

export interface IssuesFilter {
    status?: string;
    serviceName?: string;
    issueType?: string;
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

export const getIssues = async (filter: IssuesFilter = {}): Promise<PageResponse<Issue>> => {
    const params = new URLSearchParams();

    Object.entries(filter).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            params.append(key, String(value));
        }
    });

    const response = await apiClient.get<PageResponse<Issue>>(`/issues?${params.toString()}`);
    return response.data;
};

export const getIssueById = async (id: string): Promise<Issue> => {
    const response = await apiClient.get<Issue>(`/issues/${id}`);
    return response.data;
};

export const resolveIssue = async (id: string, resolvedBy: string): Promise<Issue> => {
    const response = await apiClient.put<Issue>(`/issues/${id}/resolve`, { resolvedBy });
    return response.data;
};
