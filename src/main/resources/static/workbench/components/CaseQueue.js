import React, { useEffect, useMemo, useState } from 'react';
import { formatInstant } from '../utils/format.js';
import { EnumBadge } from './EnumBadge.js';
const PAGE_SIZE_OPTIONS = [5, 10, 20, 50];
const DEFAULT_PAGE_SIZE = PAGE_SIZE_OPTIONS[0];
export function CaseQueue({ resetKey, cases, totalCount, selectedCaseId, loading, error, onRetry, onSelect }) {
    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(DEFAULT_PAGE_SIZE);
    const pageCount = Math.max(1, Math.ceil(cases.length / rowsPerPage));
    const safePage = Math.min(page, pageCount - 1);
    const visibleStart = cases.length === 0 ? 0 : safePage * rowsPerPage + 1;
    const visibleEnd = Math.min((safePage + 1) * rowsPerPage, cases.length);
    const visibleCases = useMemo(() => {
        const start = safePage * rowsPerPage;
        return cases.slice(start, start + rowsPerPage);
    }, [cases, rowsPerPage, safePage]);
    useEffect(() => {
        setPage(0);
    }, [resetKey]);
    useEffect(() => {
        if (page > pageCount - 1) {
            setPage(Math.max(0, pageCount - 1));
        }
    }, [page, pageCount]);
    useEffect(() => {
        if (!selectedCaseId) {
            return;
        }
        const selectedIndex = cases.findIndex((item) => item.caseId === selectedCaseId);
        if (selectedIndex < 0) {
            return;
        }
        const selectedPage = Math.floor(selectedIndex / rowsPerPage);
        if (selectedPage !== safePage) {
            setPage(selectedPage);
        }
    }, [cases, rowsPerPage, safePage, selectedCaseId]);
    return (React.createElement("section", { className: "card" },
        React.createElement("div", { className: "section-header compact" },
            React.createElement("div", null,
                React.createElement("h2", null, "Case queue"),
                React.createElement("p", { className: "muted" }, "The backend still owns all matching and case aggregation. This React shell stays focused on triage and operator readability.")),
            React.createElement("span", { className: "badge" },
                totalCount,
                " total")),
        loading ? React.createElement("div", { className: "empty-state" }, "Loading the discrepancy queue\u2026") : null,
        error ? (React.createElement("div", { className: "stack" },
            React.createElement("div", { className: "alert error" }, error),
            React.createElement("div", null,
                React.createElement("button", { type: "button", className: "secondary-button", onClick: onRetry }, "Retry queue request")))) : null,
        !loading && !error && cases.length === 0 ? React.createElement("div", { className: "empty-state" }, "No cases match the current filters.") : null,
        !loading && !error && cases.length > 0 ? (React.createElement(React.Fragment, null,
            React.createElement("div", { className: "queue-list" }, visibleCases.map((item) => {
                const selected = selectedCaseId === item.caseId;
                return (React.createElement("button", { key: item.caseId, type: "button", className: `queue-item${selected ? ' is-selected' : ''}`, onClick: () => onSelect(item.caseId) },
                    React.createElement("div", { className: "queue-item-header" },
                        React.createElement("div", { className: "inline-badges" },
                            React.createElement(EnumBadge, { kind: "type", value: item.caseType }),
                            React.createElement(EnumBadge, { kind: "status", value: item.caseStatus }),
                            React.createElement(EnumBadge, { kind: "provider", value: item.provider })),
                        React.createElement("span", { className: "muted" },
                            "Opened ",
                            formatInstant(item.openedAt))),
                    React.createElement("div", { className: "queue-item-meta" },
                        React.createElement("p", { className: "queue-item-summary" }, item.summary),
                        React.createElement("div", { className: "inline-meta muted" },
                            React.createElement("span", { className: "mono" }, item.matchKey || 'no-match-key'),
                            item.paymentId ? React.createElement("span", { className: "mono" },
                                "payment ",
                                item.paymentId) : null,
                            item.resolvedAt ? React.createElement("span", null,
                                "Resolved ",
                                formatInstant(item.resolvedAt)) : null))));
            })),
            React.createElement("div", { className: "queue-pagination" },
                React.createElement("div", { className: "queue-pagination__meta" },
                    React.createElement("label", { className: "queue-pagination__page-size" },
                        React.createElement("span", null, "Rows per page"),
                        React.createElement("select", { value: rowsPerPage, onChange: (event) => {
                                const nextRowsPerPage = Number(event.target.value);
                                setRowsPerPage(nextRowsPerPage);
                                setPage(0);
                            } }, PAGE_SIZE_OPTIONS.map((option) => (React.createElement("option", { key: option, value: option }, option))))),
                    React.createElement("p", { className: "queue-pagination__summary" },
                        "Showing ",
                        React.createElement("strong", null, visibleStart),
                        "\u2013",
                        React.createElement("strong", null, visibleEnd),
                        " of ",
                        React.createElement("strong", null, totalCount),
                        " filtered cases")),
                cases.length > rowsPerPage ? (React.createElement("div", { className: "queue-pagination__actions" },
                    React.createElement("button", { type: "button", className: "secondary-button", onClick: () => setPage((value) => Math.max(0, value - 1)), disabled: safePage === 0 }, "Previous page"),
                    React.createElement("span", { className: "badge" },
                        "Page ",
                        safePage + 1,
                        " of ",
                        pageCount),
                    React.createElement("button", { type: "button", className: "secondary-button", onClick: () => setPage((value) => Math.min(pageCount - 1, value + 1)), disabled: safePage >= pageCount - 1 }, "Next page"))) : null))) : null));
}
