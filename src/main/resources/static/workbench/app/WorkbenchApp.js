import React, { useEffect, useMemo, useState } from 'react';
import { ApiError, createCaseNote, fetchCaseDetail, fetchCaseList, fetchWorkbenchBootstrap, updateCaseStatus } from '../api/reconApi.js';
import { CaseDetailPanel } from '../components/CaseDetailPanel.js';
import { CaseQueue } from '../components/CaseQueue.js';
import { FilterPanel } from '../components/FilterPanel.js';
import { emptyFilters, readUrlState, writeUrlState } from '../utils/urlState.js';
function useAsyncResource(loader, deps) {
    const [reloadToken, setReloadToken] = useState(0);
    const [state, setState] = useState({
        status: 'loading',
        data: null,
        error: null
    });
    useEffect(() => {
        let cancelled = false;
        setState((previous) => ({
            status: 'loading',
            data: previous.data,
            error: null
        }));
        loader()
            .then((value) => {
            if (cancelled) {
                return;
            }
            setState({
                status: 'success',
                data: value,
                error: null
            });
        })
            .catch((error) => {
            if (cancelled) {
                return;
            }
            const message = error instanceof Error ? error.message : 'Unexpected request failure.';
            setState({
                status: 'error',
                data: null,
                error: message
            });
        });
        return () => {
            cancelled = true;
        };
    }, [...deps, reloadToken]);
    return {
        ...state,
        reload: () => setReloadToken((value) => value + 1)
    };
}
function filtersFromUrl() {
    const urlState = readUrlState(window.location.search);
    return {
        filters: {
            runId: urlState.runId,
            caseStatus: urlState.caseStatus,
            caseType: urlState.caseType,
            provider: urlState.provider,
            q: urlState.q
        },
        caseId: urlState.caseId
    };
}
function errorMessage(error, fallback) {
    if (error instanceof ApiError || error instanceof Error) {
        return error.message;
    }
    return fallback;
}
export function WorkbenchApp() {
    const initial = useMemo(() => filtersFromUrl(), []);
    const [filters, setFilters] = useState(initial.filters);
    const [draftFilters, setDraftFilters] = useState(initial.filters);
    const [selectedCaseId, setSelectedCaseId] = useState(initial.caseId);
    const [statusBusy, setStatusBusy] = useState(false);
    const [noteBusy, setNoteBusy] = useState(false);
    const [statusFlash, setStatusFlash] = useState(null);
    const [noteFlash, setNoteFlash] = useState(null);
    const bootstrap = useAsyncResource(() => fetchWorkbenchBootstrap(), []);
    const caseListDependency = JSON.stringify(filters);
    const caseList = useAsyncResource(() => fetchCaseList(filters), [caseListDependency]);
    const caseDetail = useAsyncResource(() => selectedCaseId ? fetchCaseDetail(selectedCaseId) : Promise.resolve(null), [selectedCaseId ?? 'none']);
    useEffect(() => {
        const query = writeUrlState({
            ...filters,
            caseId: selectedCaseId
        });
        const nextUrl = `${window.location.pathname}${query}`;
        if (`${window.location.pathname}${window.location.search}` !== nextUrl) {
            window.history.replaceState(null, '', nextUrl);
        }
    }, [filters, selectedCaseId]);
    useEffect(() => {
        const onPopState = () => {
            const next = filtersFromUrl();
            setFilters(next.filters);
            setDraftFilters(next.filters);
            setSelectedCaseId(next.caseId);
            setStatusFlash(null);
            setNoteFlash(null);
        };
        window.addEventListener('popstate', onPopState);
        return () => window.removeEventListener('popstate', onPopState);
    }, []);
    const openCount = useMemo(() => {
        return caseList.data?.cases.filter((item) => item.caseStatus === 'OPEN').length ?? 0;
    }, [caseList.data]);
    async function handleSaveStatus(status) {
        if (!selectedCaseId || bootstrap.status !== 'success' || !bootstrap.data) {
            return;
        }
        setStatusBusy(true);
        setStatusFlash(null);
        try {
            await updateCaseStatus(selectedCaseId, status, {
                headerName: bootstrap.data.csrfHeaderName,
                token: bootstrap.data.csrfToken
            });
            setStatusFlash({ tone: 'success', text: `Updated case status to ${status}.` });
            caseList.reload();
            caseDetail.reload();
        }
        catch (error) {
            setStatusFlash({ tone: 'error', text: errorMessage(error, 'Could not update case status.') });
        }
        finally {
            setStatusBusy(false);
        }
    }
    async function handleAddNote(body) {
        if (!selectedCaseId || bootstrap.status !== 'success' || !bootstrap.data) {
            return;
        }
        setNoteBusy(true);
        setNoteFlash(null);
        try {
            await createCaseNote(selectedCaseId, body, {
                headerName: bootstrap.data.csrfHeaderName,
                token: bootstrap.data.csrfToken
            });
            setNoteFlash({ tone: 'success', text: 'Added a new operator note.' });
            caseList.reload();
            caseDetail.reload();
        }
        catch (error) {
            setNoteFlash({ tone: 'error', text: errorMessage(error, 'Could not save the operator note.') });
        }
        finally {
            setNoteBusy(false);
        }
    }
    function applyFilters() {
        setFilters(draftFilters);
        setSelectedCaseId(null);
        setStatusFlash(null);
        setNoteFlash(null);
    }
    function clearFilters() {
        const cleared = emptyFilters();
        setDraftFilters(cleared);
        setFilters(cleared);
        setSelectedCaseId(null);
        setStatusFlash(null);
        setNoteFlash(null);
    }
    const bootstrapData = bootstrap.status === 'success' && bootstrap.data ? bootstrap.data : null;
    return (React.createElement("div", { className: "workbench-shell" },
        bootstrap.status === 'error' ? (React.createElement("section", { className: "card stack" },
            React.createElement("div", { className: "alert error" }, bootstrap.error),
            React.createElement("div", null,
                React.createElement("button", { type: "button", className: "secondary-button", onClick: bootstrap.reload }, "Retry bootstrap")))) : null,
        bootstrap.status === 'loading' && !bootstrap.data ? (React.createElement("section", { className: "card" },
            React.createElement("div", { className: "empty-state" }, "Loading operator bootstrap metadata\u2026"))) : null,
        bootstrapData ? (React.createElement("div", { className: "workbench-frame" },
            React.createElement("div", { className: "workbench-sidebar" },
                React.createElement(FilterPanel, { operatorName: bootstrapData.operatorName, recentRuns: bootstrapData.recentRuns, caseStatuses: bootstrapData.caseStatuses, caseTypes: bootstrapData.caseTypes, draftFilters: draftFilters, totalCount: caseList.data?.totalCount ?? 0, openCount: openCount, onDraftChange: setDraftFilters, onApply: applyFilters, onClear: clearFilters }),
                React.createElement(CaseQueue, { resetKey: caseListDependency, cases: caseList.data?.cases ?? [], totalCount: caseList.data?.totalCount ?? 0, selectedCaseId: selectedCaseId, loading: caseList.status === 'loading', error: caseList.error, onRetry: caseList.reload, onSelect: (caseId) => {
                        setSelectedCaseId(caseId);
                        setStatusFlash(null);
                        setNoteFlash(null);
                    } })),
            React.createElement("div", { className: "workbench-main" },
                React.createElement(CaseDetailPanel, { selectedCaseId: selectedCaseId, caseStatuses: bootstrapData.caseStatuses, detail: caseDetail.data, loading: caseDetail.status === 'loading', error: caseDetail.error, statusBusy: statusBusy, noteBusy: noteBusy, statusFlash: statusFlash, noteFlash: noteFlash, onRetry: caseDetail.reload, onSaveStatus: handleSaveStatus, onAddNote: handleAddNote })))) : null));
}
