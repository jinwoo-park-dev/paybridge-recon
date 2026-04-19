import React from 'react';
import { formatInstant } from '../utils/format.js';
export function FilterPanel({ operatorName, recentRuns, caseStatuses, caseTypes, draftFilters, totalCount, openCount, onDraftChange, onApply, onClear }) {
    function updateField(field, value) {
        onDraftChange({
            ...draftFilters,
            [field]: value
        });
    }
    return (React.createElement("section", { className: "card" },
        React.createElement("div", { className: "section-header" },
            React.createElement("div", null,
                React.createElement("h2", null, "Queue filters"),
                React.createElement("p", { className: "muted" },
                    "Signed in as ",
                    React.createElement("strong", null, operatorName),
                    ". Filters stay in the URL so a run and a specific discrepancy can be revisited directly."))),
        React.createElement("div", { className: "filter-summary" },
            React.createElement("span", { className: "badge" },
                totalCount,
                " filtered cases"),
            React.createElement("span", { className: "badge" },
                openCount,
                " open"),
            React.createElement("span", { className: "badge" },
                recentRuns.length,
                " recent runs")),
        React.createElement("form", { className: "stack", onSubmit: (event) => {
                event.preventDefault();
                onApply();
            } },
            React.createElement("div", { className: "filter-grid" },
                React.createElement("label", null,
                    React.createElement("span", null, "Run"),
                    React.createElement("select", { value: draftFilters.runId ?? '', onChange: (event) => updateField('runId', event.target.value || null) },
                        React.createElement("option", { value: "" }, "All runs"),
                        recentRuns.map((run) => (React.createElement("option", { key: run.runId, value: run.runId },
                            run.batchFilename,
                            " \u2014 ",
                            formatInstant(run.startedAt)))))),
                React.createElement("label", null,
                    React.createElement("span", null, "Status"),
                    React.createElement("select", { value: draftFilters.caseStatus ?? '', onChange: (event) => updateField('caseStatus', (event.target.value || null)) },
                        React.createElement("option", { value: "" }, "All statuses"),
                        caseStatuses.map((status) => (React.createElement("option", { key: status, value: status }, status))))),
                React.createElement("label", null,
                    React.createElement("span", null, "Type"),
                    React.createElement("select", { value: draftFilters.caseType ?? '', onChange: (event) => updateField('caseType', (event.target.value || null)) },
                        React.createElement("option", { value: "" }, "All types"),
                        caseTypes.map((type) => (React.createElement("option", { key: type, value: type }, type))))),
                React.createElement("label", null,
                    React.createElement("span", null, "Provider"),
                    React.createElement("input", { type: "text", value: draftFilters.provider ?? '', placeholder: "STRIPE or NICEPAY", onChange: (event) => updateField('provider', event.target.value || null) }))),
            React.createElement("label", null,
                React.createElement("span", null, "Search summary or match key"),
                React.createElement("input", { type: "text", value: draftFilters.q ?? '', placeholder: "orderId, providerPaymentId, summary text", onChange: (event) => updateField('q', event.target.value || null) })),
            React.createElement("div", { className: "actions-row" },
                React.createElement("button", { type: "submit" }, "Apply filters"),
                React.createElement("button", { type: "button", className: "secondary-button", onClick: onClear }, "Clear")))));
}
