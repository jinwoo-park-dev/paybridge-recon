package com.paybridge.recon.casework;

import com.paybridge.recon.integration.paybridge.PayBridgePaymentContextView;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReconCaseDetailView(
        UUID caseId,
        UUID runId,
        ReconCaseType caseType,
        ReconCaseStatus caseStatus,
        String provider,
        String summary,
        String matchKey,
        Instant openedAt,
        Instant resolvedAt,
        SettlementRowDetailView settlementRow,
        PayBridgeSnapshotDetailView payBridgeSnapshot,
        PayBridgePaymentContextView payBridgeContext,
        List<ReconCaseNoteView> notes
) {
}
