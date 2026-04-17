package com.paybridge.recon.run;

import com.paybridge.recon.casework.ReconCaseJpaEntity;
import com.paybridge.recon.casework.ReconCaseJpaRepository;
import com.paybridge.recon.casework.ReconCaseStatus;
import com.paybridge.recon.integration.paybridge.PayBridgeExportPageView;
import com.paybridge.recon.integration.paybridge.PayBridgeExportRequest;
import com.paybridge.recon.integration.paybridge.PayBridgeExportRowView;
import com.paybridge.recon.integration.paybridge.PayBridgeOpsClient;
import com.paybridge.recon.settlement.SettlementImportBatchJpaEntity;
import com.paybridge.recon.settlement.SettlementImportBatchJpaRepository;
import com.paybridge.recon.settlement.SettlementRowJpaEntity;
import com.paybridge.recon.settlement.SettlementRowJpaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
public class ReconRunService {

    private static final int EXPORT_PAGE_SIZE = 200;

    private final SettlementImportBatchJpaRepository batchRepository;
    private final SettlementRowJpaRepository settlementRowRepository;
    private final ReconRunJpaRepository reconRunJpaRepository;
    private final PayBridgeSnapshotJpaRepository payBridgeSnapshotJpaRepository;
    private final ReconCaseJpaRepository reconCaseJpaRepository;
    private final PayBridgeOpsClient payBridgeOpsClient;
    private final ReconciliationMatcher reconciliationMatcher;

    public ReconRunService(
            SettlementImportBatchJpaRepository batchRepository,
            SettlementRowJpaRepository settlementRowRepository,
            ReconRunJpaRepository reconRunJpaRepository,
            PayBridgeSnapshotJpaRepository payBridgeSnapshotJpaRepository,
            ReconCaseJpaRepository reconCaseJpaRepository,
            PayBridgeOpsClient payBridgeOpsClient,
            ReconciliationMatcher reconciliationMatcher) {
        this.batchRepository = batchRepository;
        this.settlementRowRepository = settlementRowRepository;
        this.reconRunJpaRepository = reconRunJpaRepository;
        this.payBridgeSnapshotJpaRepository = payBridgeSnapshotJpaRepository;
        this.reconCaseJpaRepository = reconCaseJpaRepository;
        this.payBridgeOpsClient = payBridgeOpsClient;
        this.reconciliationMatcher = reconciliationMatcher;
    }

    @Transactional
    public ReconRunSummaryView run(ReconRunRequest request) {
        SettlementImportBatchJpaEntity batch = batchRepository.findById(request.batchId())
            .orElseThrow(() -> new IllegalArgumentException("Settlement batch not found: " + request.batchId()));
        List<SettlementRowJpaEntity> settlementRows = settlementRowRepository.findByBatch_IdOrderByRowNumberAsc(batch.getId());

        ReconRunJpaEntity run = new ReconRunJpaEntity();
        run.setBatch(batch);
        run.setApprovedFrom(request.approvedFrom());
        run.setApprovedTo(request.approvedTo());
        run.setStatus(ReconRunStatus.RUNNING);
        run.setSettlementRowCount(settlementRows.size());
        run.setPaybridgeRowCount(0);
        run.setCaseCount(0);
        run.setStartedAt(Instant.now());
        ReconRunJpaEntity persistedRun = reconRunJpaRepository.save(run);

        try {
            List<PayBridgeExportRowView> exportRows = fetchAllExportRows(request);
            List<PayBridgeSnapshotJpaEntity> snapshots = exportRows.stream()
                .map(row -> toSnapshotEntity(persistedRun, row))
                .toList();
            payBridgeSnapshotJpaRepository.saveAll(snapshots);

            List<ReconCaseJpaEntity> cases = reconciliationMatcher.match(settlementRows, snapshots).stream()
                .map(draft -> toCaseEntity(persistedRun, draft))
                .toList();
            reconCaseJpaRepository.saveAll(cases);

            persistedRun.setPaybridgeRowCount(snapshots.size());
            persistedRun.setCaseCount(cases.size());
            persistedRun.setStatus(ReconRunStatus.COMPLETED);
            persistedRun.setFinishedAt(Instant.now());
            persistedRun.setErrorSummary(null);
            return toSummaryView(reconRunJpaRepository.save(persistedRun));
        } catch (RestClientException ex) {
            persistedRun.setStatus(ReconRunStatus.FAILED);
            persistedRun.setFinishedAt(Instant.now());
            persistedRun.setErrorSummary(ex.getMessage());
            return toSummaryView(reconRunJpaRepository.save(persistedRun));
        }
    }

    private List<PayBridgeExportRowView> fetchAllExportRows(ReconRunRequest request) {
        List<PayBridgeExportRowView> rows = new ArrayList<>();
        int page = 0;
        boolean hasNext = false;
        do {
            PayBridgeExportPageView exportPage = payBridgeOpsClient.fetchTransactionsExport(
                new PayBridgeExportRequest(request.approvedFrom(), request.approvedTo(), null, null, page, EXPORT_PAGE_SIZE)
            ).getBody();
            if (exportPage == null || exportPage.content() == null) {
                throw new RestClientException("PayBridge export response missing content for page " + page + ".");
            }
            rows.addAll(exportPage.content());
            hasNext = exportPage.hasNext();
            page++;
        } while (hasNext);
        return rows;
    }

    private PayBridgeSnapshotJpaEntity toSnapshotEntity(ReconRunJpaEntity run, PayBridgeExportRowView row) {
        PayBridgeSnapshotJpaEntity entity = new PayBridgeSnapshotJpaEntity();
        entity.setRun(run);
        entity.setPaymentId(row.paymentId());
        entity.setOrderId(row.orderId());
        entity.setProvider(row.provider());
        entity.setStatus(row.status());
        entity.setAmountMinor(row.amountMinor());
        entity.setReversibleAmountMinor(row.reversibleAmountMinor());
        entity.setCurrency(row.currency());
        entity.setProviderPaymentId(row.providerPaymentId());
        entity.setProviderTransactionId(row.providerTransactionId());
        entity.setApprovedAt(row.approvedAt());
        entity.setUpstreamCreatedAt(row.createdAt());
        entity.setUpstreamUpdatedAt(row.updatedAt());
        return entity;
    }

    private ReconCaseJpaEntity toCaseEntity(ReconRunJpaEntity run, ReconCaseDraft draft) {
        ReconCaseJpaEntity entity = new ReconCaseJpaEntity();
        entity.setRun(run);
        entity.setCaseType(draft.caseType());
        entity.setCaseStatus(ReconCaseStatus.OPEN);
        entity.setProvider(draft.provider());
        entity.setPaymentId(draft.paymentId());
        entity.setSettlementRow(draft.settlementRow());
        entity.setSummary(draft.summary());
        entity.setMatchKey(draft.matchKey());
        entity.setOpenedAt(Instant.now());
        return entity;
    }

    private ReconRunSummaryView toSummaryView(ReconRunJpaEntity run) {
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
