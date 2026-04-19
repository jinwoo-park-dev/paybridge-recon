import React from 'react';
import type { CaseFilters, ReconCaseStatus, ReconCaseType, ReconRunSummary } from '../types/recon.js';
import { formatInstant } from '../utils/format.js';

interface FilterPanelProps {
  operatorName: string;
  recentRuns: ReconRunSummary[];
  caseStatuses: ReconCaseStatus[];
  caseTypes: ReconCaseType[];
  draftFilters: CaseFilters;
  totalCount: number;
  openCount: number;
  onDraftChange: (next: CaseFilters) => void;
  onApply: () => void;
  onClear: () => void;
}

export function FilterPanel({
  operatorName,
  recentRuns,
  caseStatuses,
  caseTypes,
  draftFilters,
  totalCount,
  openCount,
  onDraftChange,
  onApply,
  onClear
}: FilterPanelProps) {
  function updateField<K extends keyof CaseFilters>(field: K, value: CaseFilters[K]) {
    onDraftChange({
      ...draftFilters,
      [field]: value
    });
  }

  return (
    <section className="card">
      <div className="section-header">
        <div>
          <h2>Queue filters</h2>
          <p className="muted">Signed in as <strong>{operatorName}</strong>. Filters stay in the URL so a run and a specific discrepancy can be revisited directly.</p>
        </div>
      </div>

      <div className="filter-summary">
        <span className="badge">{totalCount} filtered cases</span>
        <span className="badge">{openCount} open</span>
        <span className="badge">{recentRuns.length} recent runs</span>
      </div>

      <form className="stack" onSubmit={(event) => {
        event.preventDefault();
        onApply();
      }}>
        <div className="filter-grid">
          <label>
            <span>Run</span>
            <select value={draftFilters.runId ?? ''} onChange={(event) => updateField('runId', event.target.value || null)}>
              <option value="">All runs</option>
              {recentRuns.map((run) => (
                <option key={run.runId} value={run.runId}>
                  {run.batchFilename} — {formatInstant(run.startedAt)}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>Status</span>
            <select value={draftFilters.caseStatus ?? ''} onChange={(event) => updateField('caseStatus', (event.target.value || null) as ReconCaseStatus | null)}>
              <option value="">All statuses</option>
              {caseStatuses.map((status) => (
                <option key={status} value={status}>{status}</option>
              ))}
            </select>
          </label>

          <label>
            <span>Type</span>
            <select value={draftFilters.caseType ?? ''} onChange={(event) => updateField('caseType', (event.target.value || null) as ReconCaseType | null)}>
              <option value="">All types</option>
              {caseTypes.map((type) => (
                <option key={type} value={type}>{type}</option>
              ))}
            </select>
          </label>

          <label>
            <span>Provider</span>
            <input
              type="text"
              value={draftFilters.provider ?? ''}
              placeholder="STRIPE or NICEPAY"
              onChange={(event) => updateField('provider', event.target.value || null)}
            />
          </label>
        </div>

        <label>
          <span>Search summary or match key</span>
          <input
            type="text"
            value={draftFilters.q ?? ''}
            placeholder="orderId, providerPaymentId, summary text"
            onChange={(event) => updateField('q', event.target.value || null)}
          />
        </label>

        <div className="actions-row">
          <button type="submit">Apply filters</button>
          <button type="button" className="secondary-button" onClick={onClear}>Clear</button>
        </div>
      </form>
    </section>
  );
}
