export type ReconCaseStatus = 'OPEN' | 'IN_REVIEW' | 'RESOLVED' | 'IGNORED';
export type ReconCaseType = 'SETTLEMENT_ONLY' | 'PAYBRIDGE_ONLY' | 'AMOUNT_MISMATCH' | 'DUPLICATE_SETTLEMENT_ROW';

export interface CaseFilters {
  runId: string | null;
  caseStatus: ReconCaseStatus | null;
  caseType: ReconCaseType | null;
  provider: string | null;
  q: string | null;
}

export interface ReconRunSummary {
  runId: string;
  batchId: string;
  batchFilename: string;
  status: string;
  settlementRowCount: number;
  paybridgeRowCount: number;
  caseCount: number;
  startedAt: string;
  finishedAt: string | null;
  errorSummary: string | null;
}

export interface WorkbenchBootstrap {
  operatorName: string;
  csrfHeaderName: string;
  csrfToken: string;
  recentRuns: ReconRunSummary[];
  caseStatuses: ReconCaseStatus[];
  caseTypes: ReconCaseType[];
}

export interface ReconCaseQueueItem {
  caseId: string;
  runId: string;
  caseType: ReconCaseType;
  caseStatus: ReconCaseStatus;
  provider: string | null;
  summary: string;
  matchKey: string | null;
  paymentId: string | null;
  settlementRowId: string | null;
  openedAt: string;
  resolvedAt: string | null;
}

export interface ReconCaseList {
  cases: ReconCaseQueueItem[];
  totalCount: number;
}

export interface SettlementRowDetail {
  settlementRowId: string;
  rowNumber: number;
  provider: string | null;
  orderId: string | null;
  providerPaymentId: string | null;
  providerTransactionId: string | null;
  amountMinor: number;
  currency: string | null;
  settledAt: string | null;
  rawRowJson: string | null;
}

export interface PayBridgeSnapshotDetail {
  paymentId: string;
  orderId: string | null;
  provider: string | null;
  status: string | null;
  amountMinor: number;
  reversibleAmountMinor: number;
  currency: string | null;
  providerPaymentId: string | null;
  providerTransactionId: string | null;
  approvedAt: string | null;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface PayBridgePaymentReversal {
  reversalId: string;
  reversalType: string;
  status: string;
  amountDisplay: string;
  processedAtDisplay: string | null;
}

export interface PayBridgePaymentDetail {
  paymentId: string;
  orderId: string | null;
  provider: string | null;
  status: string | null;
  amountDisplay: string;
  reversibleAmountDisplay: string;
  currency: string | null;
  providerPaymentId: string | null;
  providerTransactionId: string | null;
  approvedAtDisplay: string | null;
  fullReversalAllowed: boolean;
  partialReversalAllowed: boolean;
  reversals: PayBridgePaymentReversal[];
}

export interface PayBridgeAuditLog {
  id: string;
  action: string;
  outcome: string;
  resourceType: string | null;
  resourceId: string | null;
  provider: string | null;
  actorType: string | null;
  correlationId: string | null;
  message: string | null;
  detailJson: string | null;
  occurredAt: string | null;
}

export interface PayBridgeOutboxEvent {
  id: string;
  aggregateType: string | null;
  aggregateId: string | null;
  eventType: string;
  status: string;
  retryCount: number;
  availableAt: string | null;
  publishedAt: string | null;
  lastError: string | null;
  payloadJson: string | null;
  createdAt: string | null;
}

export interface PayBridgePaymentContext {
  paymentDetail: PayBridgePaymentDetail | null;
  auditLogs: PayBridgeAuditLog[];
  outboxEvents: PayBridgeOutboxEvent[];
  errorSummary: string | null;
}

export interface ReconCaseNote {
  noteId: string;
  author: string;
  body: string;
  createdAt: string;
}

export interface ReconCaseDetail {
  caseId: string;
  runId: string;
  caseType: ReconCaseType;
  caseStatus: ReconCaseStatus;
  provider: string | null;
  summary: string;
  matchKey: string | null;
  openedAt: string;
  resolvedAt: string | null;
  settlementRow: SettlementRowDetail | null;
  payBridgeSnapshot: PayBridgeSnapshotDetail | null;
  payBridgeContext: PayBridgePaymentContext | null;
  notes: ReconCaseNote[];
}
