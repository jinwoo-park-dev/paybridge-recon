function normalizeText(value) {
    if (!value) {
        return null;
    }
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed : null;
}
export function emptyFilters() {
    return {
        runId: null,
        caseStatus: null,
        caseType: null,
        provider: null,
        q: null
    };
}
export function readUrlState(search) {
    const params = new URLSearchParams(search);
    return {
        runId: normalizeText(params.get('runId')),
        caseStatus: normalizeText(params.get('caseStatus')),
        caseType: normalizeText(params.get('caseType')),
        provider: normalizeText(params.get('provider')),
        q: normalizeText(params.get('q')),
        caseId: normalizeText(params.get('caseId'))
    };
}
export function writeUrlState(state) {
    const params = new URLSearchParams();
    if (state.runId) {
        params.set('runId', state.runId);
    }
    if (state.caseStatus) {
        params.set('caseStatus', state.caseStatus);
    }
    if (state.caseType) {
        params.set('caseType', state.caseType);
    }
    if (state.provider) {
        params.set('provider', state.provider);
    }
    if (state.q) {
        params.set('q', state.q);
    }
    if (state.caseId) {
        params.set('caseId', state.caseId);
    }
    const query = params.toString();
    return query.length > 0 ? `?${query}` : '';
}
