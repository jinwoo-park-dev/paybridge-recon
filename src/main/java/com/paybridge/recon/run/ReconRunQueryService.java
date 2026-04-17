package com.paybridge.recon.run;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReconRunQueryService {

    private final ReconRunJpaRepository reconRunJpaRepository;

    public ReconRunQueryService(ReconRunJpaRepository reconRunJpaRepository) {
        this.reconRunJpaRepository = reconRunJpaRepository;
    }

    public List<ReconRunSummaryView> recentRuns() {
        return reconRunJpaRepository.findTop10ByOrderByStartedAtDesc().stream()
            .map(this::toView)
            .toList();
    }

    public ReconRunSummaryView getRun(UUID runId) {
        return reconRunJpaRepository.findById(runId)
            .map(this::toView)
            .orElseThrow(() -> new IllegalArgumentException("Recon run not found: " + runId));
    }

    private ReconRunSummaryView toView(ReconRunJpaEntity run) {
        return new ReconRunSummaryView(
            run.getId(),
            run.getBatch().getId(),
            run.getBatch().getFilename(),
            run.getStatus(),
            run.getSettlementRowCount(),
            run.getPaybridgeRowCount(),
            run.getCaseCount(),
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getErrorSummary()
        );
    }
}
