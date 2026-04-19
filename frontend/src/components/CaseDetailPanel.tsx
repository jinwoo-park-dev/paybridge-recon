import React, { useEffect, useMemo, useState } from 'react';
import type { ReconCaseDetail, ReconCaseStatus } from '../types/recon.js';
import { formatInstant, formatMinorAmount } from '../utils/format.js';
import { EnumBadge } from './EnumBadge.js';

interface FlashMessage {
  tone: 'success' | 'error';
  text: string;
}

interface CaseDetailPanelProps {
  selectedCaseId: string | null;
  caseStatuses: ReconCaseStatus[];
  detail: ReconCaseDetail | null;
  loading: boolean;
  error: string | null;
  statusBusy: boolean;
  noteBusy: boolean;
  statusFlash: FlashMessage | null;
  noteFlash: FlashMessage | null;
  onRetry: () => void;
  onSaveStatus: (status: ReconCaseStatus) => void;
  onAddNote: (body: string) => void;
}

export function CaseDetailPanel({
  selectedCaseId,
  caseStatuses,
  detail,
  loading,
  error,
  statusBusy,
  noteBusy,
  statusFlash,
  noteFlash,
  onRetry,
  onSaveStatus,
  onAddNote
}: CaseDetailPanelProps) {
  const [nextStatus, setNextStatus] = useState<ReconCaseStatus>('OPEN');
  const [noteBody, setNoteBody] = useState('');

  useEffect(() => {
    if (detail?.caseStatus) {
      setNextStatus(detail.caseStatus);
    }
  }, [detail?.caseStatus]);

  useEffect(() => {
    setNoteBody('');
  }, [detail?.caseId]);

  const amountDelta = useMemo(() => {
    if (!detail?.settlementRow || !detail.payBridgeSnapshot) {
      return null;
    }
    return detail.settlementRow.amountMinor - detail.payBridgeSnapshot.amountMinor;
  }, [detail]);

  if (!selectedCaseId) {
    return (
      <section className="card empty-panel">
        <div className="empty-state">Select a case from the queue to load the compare view, linked PayBridge context, and operator note workflow.</div>
      </section>
    );
  }

  if (loading) {
    return (
      <section className="card empty-panel">
        <div className="empty-state">Loading the selected case detail…</div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="card stack">
        <div className="alert error">{error}</div>
        <div>
          <button type="button" className="secondary-button" onClick={onRetry}>Retry case detail</button>
        </div>
      </section>
    );
  }

  if (!detail) {
    return (
      <section className="card empty-panel">
        <div className="empty-state">The selected case could not be loaded.</div>
      </section>
    );
  }

  const settlementAmount = detail.settlementRow
    ? formatMinorAmount(detail.settlementRow.amountMinor, detail.settlementRow.currency)
    : '—';
  const snapshotAmount = detail.payBridgeSnapshot
    ? formatMinorAmount(detail.payBridgeSnapshot.amountMinor, detail.payBridgeSnapshot.currency)
    : '—';
  const deltaDisplay = amountDelta === null
    ? '—'
    : formatMinorAmount(Math.abs(amountDelta), detail.settlementRow?.currency ?? detail.payBridgeSnapshot?.currency);
  const deltaLabel = amountDelta === null
    ? 'No comparable amounts'
    : amountDelta === 0
      ? 'Amounts match'
      : amountDelta > 0
        ? `Settlement is higher by ${deltaDisplay}`
        : `PayBridge is higher by ${deltaDisplay}`;

  return (
    <section className="card detail-shell">
      <div className="detail-header">
        <div>
          <div className="inline-badges">
            <EnumBadge kind="type" value={detail.caseType} />
            <EnumBadge kind="status" value={detail.caseStatus} />
            <EnumBadge kind="provider" value={detail.provider} />
          </div>
          <h2 style={{ marginTop: '0.75rem' }}>{detail.summary}</h2>
          <p className="muted">Opened {formatInstant(detail.openedAt)} · Match key <span className="mono">{detail.matchKey || 'none'}</span></p>
        </div>
        <div className="subcard">
          <div className="key-value">
            <div><dt>Case ID</dt><dd className="mono">{detail.caseId}</dd></div>
            <div><dt>Run ID</dt><dd className="mono">{detail.runId}</dd></div>
            <div><dt>Resolved</dt><dd>{formatInstant(detail.resolvedAt)}</dd></div>
          </div>
        </div>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="label">Settlement amount</div>
          <div className="metric">{settlementAmount}</div>
        </div>
        <div className="stat-card">
          <div className="label">PayBridge amount</div>
          <div className="metric">{snapshotAmount}</div>
        </div>
        <div className="stat-card">
          <div className="label">Comparison</div>
          <div className="metric" style={{ fontSize: '1rem' }}>{deltaLabel}</div>
        </div>
      </div>

      <div className="mutation-row">
        <form className="subcard stack" onSubmit={(event) => {
          event.preventDefault();
          onSaveStatus(nextStatus);
        }}>
          <div className="section-header compact">
            <div>
              <h3>Update status</h3>
              <p className="muted">Keep the case workflow intentionally small: operator review, resolution, or ignore.</p>
            </div>
          </div>
          {statusFlash ? <div className={`alert ${statusFlash.tone === 'success' ? 'success' : 'error'}`}>{statusFlash.text}</div> : null}
          <label>
            <span>Case status</span>
            <select value={nextStatus} onChange={(event) => setNextStatus(event.target.value as ReconCaseStatus)}>
              {caseStatuses.map((status) => (
                <option key={status} value={status}>{status}</option>
              ))}
            </select>
          </label>
          <div>
            <button type="submit" disabled={statusBusy}>{statusBusy ? 'Saving…' : 'Save status'}</button>
          </div>
        </form>

        <form className="subcard stack note-form" onSubmit={(event) => {
          event.preventDefault();
          onAddNote(noteBody);
        }}>
          <div className="section-header compact">
            <div>
              <h3>Add operator note</h3>
              <p className="muted">Capture what was checked, whether PayBridge context matched, and what follow-up is still open.</p>
            </div>
          </div>
          {noteFlash ? <div className={`alert ${noteFlash.tone === 'success' ? 'success' : 'error'}`}>{noteFlash.text}</div> : null}
          <label>
            <span>Note body</span>
            <textarea
              value={noteBody}
              placeholder="Summarize what you checked and what the next operator should know."
              onChange={(event) => setNoteBody(event.target.value)}
            />
          </label>
          <div>
            <button type="submit" disabled={noteBusy || noteBody.trim().length === 0}>{noteBusy ? 'Saving…' : 'Add note'}</button>
          </div>
        </form>
      </div>

      <div className="compare-grid">
        <section className="subcard stack">
          <div className="section-header compact">
            <div>
              <h3>Settlement row</h3>
              <p className="muted">The imported external settlement source for this discrepancy.</p>
            </div>
          </div>
          {!detail.settlementRow ? <div className="empty-state">This case is not linked to a settlement row.</div> : (
            <>
              <dl className="key-value inline-grid">
                <div><dt>Row</dt><dd>{detail.settlementRow.rowNumber}</dd></div>
                <div><dt>Provider</dt><dd>{detail.settlementRow.provider || '—'}</dd></div>
                <div><dt>Order ID</dt><dd>{detail.settlementRow.orderId || '—'}</dd></div>
                <div><dt>Provider payment</dt><dd className="mono">{detail.settlementRow.providerPaymentId || '—'}</dd></div>
                <div><dt>Provider transaction</dt><dd className="mono">{detail.settlementRow.providerTransactionId || '—'}</dd></div>
                <div><dt>Settled at</dt><dd>{formatInstant(detail.settlementRow.settledAt)}</dd></div>
                <div><dt>Amount</dt><dd>{settlementAmount}</dd></div>
                <div><dt>Row ID</dt><dd className="mono">{detail.settlementRow.settlementRowId}</dd></div>
              </dl>
              {detail.settlementRow.rawRowJson ? (
                <details>
                  <summary>Raw row JSON</summary>
                  <pre className="code-block">{detail.settlementRow.rawRowJson}</pre>
                </details>
              ) : null}
            </>
          )}
        </section>

        <section className="subcard stack">
          <div className="section-header compact">
            <div>
              <h3>PayBridge snapshot</h3>
              <p className="muted">The machine-readable export row captured during the manual reconciliation run.</p>
            </div>
          </div>
          {!detail.payBridgeSnapshot ? <div className="empty-state">This case is not linked to a PayBridge snapshot.</div> : (
            <dl className="key-value inline-grid">
              <div><dt>Payment ID</dt><dd className="mono">{detail.payBridgeSnapshot.paymentId}</dd></div>
              <div><dt>Status</dt><dd>{detail.payBridgeSnapshot.status || '—'}</dd></div>
              <div><dt>Order ID</dt><dd>{detail.payBridgeSnapshot.orderId || '—'}</dd></div>
              <div><dt>Provider</dt><dd>{detail.payBridgeSnapshot.provider || '—'}</dd></div>
              <div><dt>Provider payment</dt><dd className="mono">{detail.payBridgeSnapshot.providerPaymentId || '—'}</dd></div>
              <div><dt>Provider transaction</dt><dd className="mono">{detail.payBridgeSnapshot.providerTransactionId || '—'}</dd></div>
              <div><dt>Approved at</dt><dd>{formatInstant(detail.payBridgeSnapshot.approvedAt)}</dd></div>
              <div><dt>Created at</dt><dd>{formatInstant(detail.payBridgeSnapshot.createdAt)}</dd></div>
              <div><dt>Updated at</dt><dd>{formatInstant(detail.payBridgeSnapshot.updatedAt)}</dd></div>
              <div><dt>Amount</dt><dd>{snapshotAmount}</dd></div>
              <div><dt>Reversible amount</dt><dd>{formatMinorAmount(detail.payBridgeSnapshot.reversibleAmountMinor, detail.payBridgeSnapshot.currency)}</dd></div>
            </dl>
          )}
        </section>
      </div>

      <section className="subcard stack">
        <div className="section-header compact">
          <div>
            <h3>PayBridge operator context</h3>
            <p className="muted">The browser never calls PayBridge directly. The recon backend aggregates detail, audit logs, and outbox events into this single case response.</p>
          </div>
        </div>

        {!detail.payBridgeContext ? <div className="empty-state">No PayBridge context is attached to this case.</div> : (
          <>
            {detail.payBridgeContext.errorSummary ? <div className="callout error">{detail.payBridgeContext.errorSummary}</div> : null}

            {detail.payBridgeContext.paymentDetail ? (
              <div className="stack">
                <dl className="key-value inline-grid">
                  <div><dt>Payment status</dt><dd>{detail.payBridgeContext.paymentDetail.status || '—'}</dd></div>
                  <div><dt>Display amount</dt><dd>{detail.payBridgeContext.paymentDetail.amountDisplay}</dd></div>
                  <div><dt>Reversible amount</dt><dd>{detail.payBridgeContext.paymentDetail.reversibleAmountDisplay}</dd></div>
                  <div><dt>Approved at</dt><dd>{detail.payBridgeContext.paymentDetail.approvedAtDisplay || '—'}</dd></div>
                  <div><dt>Full reversal allowed</dt><dd>{detail.payBridgeContext.paymentDetail.fullReversalAllowed ? 'Yes' : 'No'}</dd></div>
                  <div><dt>Partial reversal allowed</dt><dd>{detail.payBridgeContext.paymentDetail.partialReversalAllowed ? 'Yes' : 'No'}</dd></div>
                </dl>

                {detail.payBridgeContext.paymentDetail.reversals.length > 0 ? (
                  <div className="table-scroll">
                    <table>
                      <thead>
                        <tr>
                          <th>Reversal type</th>
                          <th>Status</th>
                          <th>Amount</th>
                          <th>Processed</th>
                        </tr>
                      </thead>
                      <tbody>
                        {detail.payBridgeContext.paymentDetail.reversals.map((reversal) => (
                          <tr key={reversal.reversalId}>
                            <td>{reversal.reversalType}</td>
                            <td>{reversal.status}</td>
                            <td>{reversal.amountDisplay}</td>
                            <td>{reversal.processedAtDisplay || '—'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : null}
              </div>
            ) : <div className="empty-state">No upstream payment detail was returned.</div>}

            <div className="stack">
              <div>
                <h4>Audit logs</h4>
                {detail.payBridgeContext.auditLogs.length === 0 ? <div className="empty-state">No audit logs returned.</div> : (
                  <div className="table-scroll">
                    <table>
                      <thead>
                        <tr>
                          <th>Occurred</th>
                          <th>Action</th>
                          <th>Outcome</th>
                          <th>Message</th>
                        </tr>
                      </thead>
                      <tbody>
                        {detail.payBridgeContext.auditLogs.map((log) => (
                          <tr key={log.id}>
                            <td>{formatInstant(log.occurredAt)}</td>
                            <td>{log.action}</td>
                            <td>{log.outcome}</td>
                            <td>
                              <div>{log.message || '—'}</div>
                              {log.correlationId ? <div className="muted mono">{log.correlationId}</div> : null}
                              {log.detailJson ? (
                                <details>
                                  <summary>detail json</summary>
                                  <pre className="code-block">{log.detailJson}</pre>
                                </details>
                              ) : null}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              <div>
                <h4>Outbox events</h4>
                {detail.payBridgeContext.outboxEvents.length === 0 ? <div className="empty-state">No outbox events returned.</div> : (
                  <div className="table-scroll">
                    <table>
                      <thead>
                        <tr>
                          <th>Created</th>
                          <th>Event type</th>
                          <th>Status</th>
                          <th>Retries</th>
                          <th>Context</th>
                        </tr>
                      </thead>
                      <tbody>
                        {detail.payBridgeContext.outboxEvents.map((event) => (
                          <tr key={event.id}>
                            <td>{formatInstant(event.createdAt)}</td>
                            <td>{event.eventType}</td>
                            <td>{event.status}</td>
                            <td>{event.retryCount}</td>
                            <td>
                              <div className="muted mono">aggregate {event.aggregateType || '—'} / {event.aggregateId || '—'}</div>
                              {event.lastError ? <div className="callout warning">{event.lastError}</div> : null}
                              {event.payloadJson ? (
                                <details>
                                  <summary>payload json</summary>
                                  <pre className="code-block">{event.payloadJson}</pre>
                                </details>
                              ) : null}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </>
        )}
      </section>

      <section className="subcard stack">
        <div className="section-header compact">
          <div>
            <h3>Operator notes</h3>
            <p className="muted">Notes stay intentionally small: author, timestamp, and plain text body.</p>
          </div>
          <span className="badge">{detail.notes.length} notes</span>
        </div>
        {detail.notes.length === 0 ? <div className="empty-state">No notes have been recorded for this case yet.</div> : (
          <div className="timeline">
            {detail.notes.map((note) => (
              <article key={note.noteId} className="timeline-note">
                <div className="section-header compact">
                  <strong>{note.author}</strong>
                  <span className="muted">{formatInstant(note.createdAt)}</span>
                </div>
                <p className="timeline-note-body">{note.body}</p>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}
