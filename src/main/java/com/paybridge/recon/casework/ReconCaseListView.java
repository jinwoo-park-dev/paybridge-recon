package com.paybridge.recon.casework;

import java.util.List;

public record ReconCaseListView(
        List<ReconCaseQueueItemView> cases,
        long totalCount
) {
}
