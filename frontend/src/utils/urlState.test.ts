import { describe, expect, it } from 'vitest';
import { emptyFilters, readUrlState, writeUrlState } from './urlState.js';

describe('urlState helpers', () => {
  it('round-trips filters and selected case id through query strings', () => {
    const search = writeUrlState({
      runId: 'run-1',
      caseStatus: 'OPEN',
      caseType: 'AMOUNT_MISMATCH',
      provider: 'STRIPE',
      q: 'ORD-2001',
      caseId: 'case-9'
    });

    expect(readUrlState(search)).toEqual({
      runId: 'run-1',
      caseStatus: 'OPEN',
      caseType: 'AMOUNT_MISMATCH',
      provider: 'STRIPE',
      q: 'ORD-2001',
      caseId: 'case-9'
    });
  });

  it('creates an empty filter object', () => {
    expect(emptyFilters()).toEqual({
      runId: null,
      caseStatus: null,
      caseType: null,
      provider: null,
      q: null
    });
  });
});
