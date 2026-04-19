import React, { useEffect, useMemo, useState } from 'react';
import { formatInstant } from '../utils/format.js';
import type { ReconCaseQueueItem } from '../types/recon.js';
import { EnumBadge } from './EnumBadge.js';

const PAGE_SIZE_OPTIONS = [5, 10, 20, 50] as const;
const DEFAULT_PAGE_SIZE = PAGE_SIZE_OPTIONS[0];

interface CaseQueueProps {
  resetKey: string;
  cases: ReconCaseQueueItem[];
  totalCount: number;
  selectedCaseId: string | null;
  loading: boolean;
  error: string | null;
  onRetry: () => void;
  onSelect: (caseId: string) => void;
}

export function CaseQueue({
  resetKey,
  cases,
  totalCount,
  selectedCaseId,
  loading,
  error,
  onRetry,
  onSelect
}: CaseQueueProps) {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(DEFAULT_PAGE_SIZE);

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

  return (
    <section className="card">
      <div className="section-header compact">
        <div>
          <h2>Case queue</h2>
          <p className="muted">
            The backend still owns all matching and case aggregation. This React shell stays focused on triage and operator readability.
          </p>
        </div>
        <span className="badge">{totalCount} total</span>
      </div>

      {loading ? <div className="empty-state">Loading the discrepancy queue…</div> : null}

      {error ? (
        <div className="stack">
          <div className="alert error">{error}</div>
          <div>
            <button type="button" className="secondary-button" onClick={onRetry}>Retry queue request</button>
          </div>
        </div>
      ) : null}

      {!loading && !error && cases.length === 0 ? <div className="empty-state">No cases match the current filters.</div> : null}

      {!loading && !error && cases.length > 0 ? (
        <>
          <div className="queue-list">
            {visibleCases.map((item) => {
              const selected = selectedCaseId === item.caseId;
              return (
                <button
                  key={item.caseId}
                  type="button"
                  className={`queue-item${selected ? ' is-selected' : ''}`}
                  onClick={() => onSelect(item.caseId)}
                >
                  <div className="queue-item-header">
                    <div className="inline-badges">
                      <EnumBadge kind="type" value={item.caseType} />
                      <EnumBadge kind="status" value={item.caseStatus} />
                      <EnumBadge kind="provider" value={item.provider} />
                    </div>
                    <span className="muted">Opened {formatInstant(item.openedAt)}</span>
                  </div>
                  <div className="queue-item-meta">
                    <p className="queue-item-summary">{item.summary}</p>
                    <div className="inline-meta muted">
                      <span className="mono">{item.matchKey || 'no-match-key'}</span>
                      {item.paymentId ? <span className="mono">payment {item.paymentId}</span> : null}
                      {item.resolvedAt ? <span>Resolved {formatInstant(item.resolvedAt)}</span> : null}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>

          <div className="queue-pagination">
            <div className="queue-pagination__meta">
              <label className="queue-pagination__page-size">
                <span>Rows per page</span>
                <select
                  value={rowsPerPage}
                  onChange={(event) => {
                    const nextRowsPerPage = Number(event.target.value);
                    setRowsPerPage(nextRowsPerPage);
                    setPage(0);
                  }}
                >
                  {PAGE_SIZE_OPTIONS.map((option) => (
                    <option key={option} value={option}>{option}</option>
                  ))}
                </select>
              </label>
              <p className="queue-pagination__summary">
                Showing <strong>{visibleStart}</strong>–<strong>{visibleEnd}</strong> of <strong>{totalCount}</strong> filtered cases
              </p>
            </div>

            {cases.length > rowsPerPage ? (
              <div className="queue-pagination__actions">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setPage((value) => Math.max(0, value - 1))}
                  disabled={safePage === 0}
                >
                  Previous page
                </button>
                <span className="badge">Page {safePage + 1} of {pageCount}</span>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setPage((value) => Math.min(pageCount - 1, value + 1))}
                  disabled={safePage >= pageCount - 1}
                >
                  Next page
                </button>
              </div>
            ) : null}
          </div>
        </>
      ) : null}
    </section>
  );
}
