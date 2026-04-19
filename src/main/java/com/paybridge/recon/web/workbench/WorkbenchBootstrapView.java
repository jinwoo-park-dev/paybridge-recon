package com.paybridge.recon.web.workbench;

import com.paybridge.recon.casework.ReconCaseStatus;
import com.paybridge.recon.casework.ReconCaseType;
import com.paybridge.recon.run.ReconRunSummaryView;
import java.util.List;

public record WorkbenchBootstrapView(
        String operatorName,
        String csrfHeaderName,
        String csrfToken,
        List<ReconRunSummaryView> recentRuns,
        List<ReconCaseStatus> caseStatuses,
        List<ReconCaseType> caseTypes
) {
}
