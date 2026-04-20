import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { CaseQueue } from './CaseQueue.js';
import type { ReconCaseQueueItem } from '../types/recon.js';

function buildCase(index: number): ReconCaseQueueItem {
  return {
    caseId: `case-${index}`,
    runId: 'run-1',
    caseType: 'AMOUNT_MISMATCH',
    caseStatus: 'OPEN',
    provider: 'STRIPE',
    summary: `Queue summary ${index}`,
    matchKey: `match-key-${index}`,
    paymentId: `payment-${index}`,
    settlementRowId: `row-${index}`,
    openedAt: '2025-01-15T12:30:00Z',
    resolvedAt: null
  };
}

describe('CaseQueue', () => {
  it('defaults to five rows per page and moves through queue pages', () => {
    const onSelect = vi.fn();
    const cases = Array.from({ length: 9 }, (_, index) => buildCase(index + 1));

    render(
      <CaseQueue
        resetKey="all"
        cases={cases}
        totalCount={cases.length}
        selectedCaseId={null}
        loading={false}
        error={null}
        onRetry={() => {}}
        onSelect={onSelect}
      />
    );

    expect(screen.getByText('Queue summary 1')).toBeInTheDocument();
    expect(screen.getByText('Queue summary 5')).toBeInTheDocument();
    expect(screen.queryByText('Queue summary 6')).not.toBeInTheDocument();
    expect(screen.getByText('Showing 1–5 of 9 filtered cases')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Next page' }));
    expect(screen.getByText('Queue summary 6')).toBeInTheDocument();
    expect(screen.queryByText('Queue summary 1')).not.toBeInTheDocument();
  });

  it('lets the operator change rows per page', () => {
    const cases = Array.from({ length: 12 }, (_, index) => buildCase(index + 1));

    render(
      <CaseQueue
        resetKey="all"
        cases={cases}
        totalCount={cases.length}
        selectedCaseId={null}
        loading={false}
        error={null}
        onRetry={() => {}}
        onSelect={() => {}}
      />
    );

    fireEvent.change(screen.getByRole('combobox'), { target: { value: '10' } });

    expect(screen.getByText('Queue summary 10')).toBeInTheDocument();
    expect(screen.queryByText('Queue summary 11')).not.toBeInTheDocument();
    expect(screen.getByText('Showing 1–10 of 12 filtered cases')).toBeInTheDocument();
  });

  it('automatically jumps to the page containing the selected case', async () => {
    const cases = Array.from({ length: 12 }, (_, index) => buildCase(index + 1));

    render(
      <CaseQueue
        resetKey="all"
        cases={cases}
        totalCount={cases.length}
        selectedCaseId="case-12"
        loading={false}
        error={null}
        onRetry={() => {}}
        onSelect={() => {}}
      />
    );

    expect(await screen.findByText('Queue summary 12')).toBeInTheDocument();
    expect(screen.getByText('Page 3 of 3')).toBeInTheDocument();
  });
});
