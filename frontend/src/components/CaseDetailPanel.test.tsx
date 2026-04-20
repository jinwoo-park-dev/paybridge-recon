import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CaseDetailPanel } from './CaseDetailPanel.js';
import type { ReconCaseDetail } from '../types/recon.js';

const detail: ReconCaseDetail = {
  caseId: 'case-1',
  runId: 'run-1',
  caseType: 'AMOUNT_MISMATCH',
  caseStatus: 'OPEN',
  provider: 'STRIPE',
  summary: 'Settlement amount does not match PayBridge snapshot.',
  matchKey: 'STRIPE::providerPaymentId::pi_123',
  openedAt: '2025-01-15T12:30:00Z',
  resolvedAt: null,
  settlementRow: {
    settlementRowId: 'row-1',
    rowNumber: 2,
    provider: 'STRIPE',
    orderId: 'ORD-2001',
    providerPaymentId: 'pi_123',
    providerTransactionId: 'ch_123',
    amountMinor: 2099,
    currency: 'USD',
    settledAt: '2025-01-15T12:30:00Z',
    rawRowJson: '{"provider":"STRIPE"}'
  },
  payBridgeSnapshot: {
    paymentId: '11111111-1111-1111-1111-111111111111',
    orderId: 'ORD-2001',
    provider: 'STRIPE',
    status: 'APPROVED',
    amountMinor: 1999,
    reversibleAmountMinor: 1999,
    currency: 'USD',
    providerPaymentId: 'pi_123',
    providerTransactionId: 'ch_123',
    approvedAt: '2025-01-15T12:00:00Z',
    createdAt: '2025-01-15T11:59:58Z',
    updatedAt: '2025-01-15T12:00:01Z'
  },
  payBridgeContext: {
    paymentDetail: {
      paymentId: '11111111-1111-1111-1111-111111111111',
      orderId: 'ORD-2001',
      provider: 'STRIPE',
      status: 'APPROVED',
      amountDisplay: '$19.99',
      reversibleAmountDisplay: '$19.99',
      currency: 'USD',
      providerPaymentId: 'pi_123',
      providerTransactionId: 'ch_123',
      approvedAtDisplay: '2025-01-15 12:00:00 UTC',
      fullReversalAllowed: true,
      partialReversalAllowed: true,
      reversals: []
    },
    auditLogs: [],
    outboxEvents: [],
    errorSummary: null
  },
  notes: []
};

describe('CaseDetailPanel', () => {
  it('renders an empty prompt when no case is selected', () => {
    render(
      <CaseDetailPanel
        selectedCaseId={null}
        caseStatuses={['OPEN', 'IN_REVIEW', 'RESOLVED', 'IGNORED']}
        detail={null}
        loading={false}
        error={null}
        statusBusy={false}
        noteBusy={false}
        statusFlash={null}
        noteFlash={null}
        onRetry={() => {}}
        onSaveStatus={() => {}}
        onAddNote={() => {}}
      />
    );

    expect(screen.getByText(/Select a case from the queue/i)).toBeInTheDocument();
  });

  it('renders the comparison summary and calls save handlers', () => {
    const onSaveStatus = vi.fn();
    const onAddNote = vi.fn();

    render(
      <CaseDetailPanel
        selectedCaseId="case-1"
        caseStatuses={['OPEN', 'IN_REVIEW', 'RESOLVED', 'IGNORED']}
        detail={detail}
        loading={false}
        error={null}
        statusBusy={false}
        noteBusy={false}
        statusFlash={null}
        noteFlash={null}
        onRetry={() => {}}
        onSaveStatus={onSaveStatus}
        onAddNote={onAddNote}
      />
    );

    expect(screen.getByText(/Settlement is higher by/)).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Case status'), { target: { value: 'IN_REVIEW' } });
    fireEvent.click(screen.getByRole('button', { name: 'Save status' }));
    expect(onSaveStatus).toHaveBeenCalledWith('IN_REVIEW');

    fireEvent.change(screen.getByLabelText('Note body'), { target: { value: 'Checked the settlement batch.' } });
    fireEvent.click(screen.getByRole('button', { name: 'Add note' }));
    expect(onAddNote).toHaveBeenCalledWith('Checked the settlement batch.');
  });
});
