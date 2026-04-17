package com.paybridge.recon.run;

import com.paybridge.recon.casework.ReconCaseType;
import com.paybridge.recon.settlement.SettlementRowJpaEntity;
import java.util.UUID;

public record ReconCaseDraft(
        ReconCaseType caseType,
        String provider,
        UUID paymentId,
        SettlementRowJpaEntity settlementRow,
        String summary,
        String matchKey
) {
}
