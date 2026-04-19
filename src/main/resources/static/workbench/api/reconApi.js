export class ApiError extends Error {
    status;
    constructor(message, status = 500) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
    }
}
function buildQuery(filters) {
    const params = new URLSearchParams();
    if (filters.runId) {
        params.set('runId', filters.runId);
    }
    if (filters.caseStatus) {
        params.set('caseStatus', filters.caseStatus);
    }
    if (filters.caseType) {
        params.set('caseType', filters.caseType);
    }
    if (filters.provider) {
        params.set('provider', filters.provider);
    }
    if (filters.q) {
        params.set('q', filters.q);
    }
    const query = params.toString();
    return query.length > 0 ? `?${query}` : '';
}
async function parseJson(response) {
    const contentType = response.headers.get('content-type') ?? '';
    if (!response.ok) {
        if (contentType.includes('application/json')) {
            const payload = await response.json().catch(() => null);
            throw new ApiError(payload?.message ?? `Request failed with status ${response.status}.`, response.status);
        }
        const text = await response.text();
        throw new ApiError(text || `Request failed with status ${response.status}.`, response.status);
    }
    if (!contentType.includes('application/json')) {
        throw new ApiError('Expected JSON but received a different response. The operator session may have expired.', response.status);
    }
    return response.json();
}
function jsonHeaders(csrf) {
    const headers = {
        'Accept': 'application/json'
    };
    if (csrf) {
        headers[csrf.headerName] = csrf.token;
    }
    return headers;
}
export async function fetchWorkbenchBootstrap() {
    const response = await fetch('/api/recon/workbench/bootstrap', {
        credentials: 'same-origin',
        headers: jsonHeaders()
    });
    const payload = await parseJson(response);
    if (!Array.isArray(payload.recentRuns) || !Array.isArray(payload.caseStatuses) || !Array.isArray(payload.caseTypes)) {
        throw new ApiError('Invalid workbench bootstrap response.');
    }
    return payload;
}
export async function fetchCaseList(filters) {
    const response = await fetch(`/api/recon/cases${buildQuery(filters)}`, {
        credentials: 'same-origin',
        headers: jsonHeaders()
    });
    const payload = await parseJson(response);
    if (!Array.isArray(payload.cases)) {
        throw new ApiError('Invalid case queue response.');
    }
    return payload;
}
export async function fetchCaseDetail(caseId) {
    const response = await fetch(`/api/recon/cases/${caseId}`, {
        credentials: 'same-origin',
        headers: jsonHeaders()
    });
    return parseJson(response);
}
export async function updateCaseStatus(caseId, status, csrf) {
    const response = await fetch(`/api/recon/cases/${caseId}/status`, {
        method: 'PATCH',
        credentials: 'same-origin',
        headers: {
            ...jsonHeaders(csrf),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status })
    });
    await parseJson(response);
}
export async function createCaseNote(caseId, body, csrf) {
    const response = await fetch(`/api/recon/cases/${caseId}/notes`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
            ...jsonHeaders(csrf),
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ body })
    });
    await parseJson(response);
}
