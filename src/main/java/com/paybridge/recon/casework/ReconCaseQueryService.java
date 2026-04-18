package com.paybridge.recon.casework;

import com.paybridge.recon.integration.paybridge.PayBridgePaymentContextService;
import com.paybridge.recon.integration.paybridge.PayBridgePaymentContextView;
import com.paybridge.recon.run.PayBridgeSnapshotJpaEntity;
import com.paybridge.recon.run.PayBridgeSnapshotJpaRepository;
import com.paybridge.recon.settlement.SettlementRowJpaEntity;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReconCaseQueryService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "openedAt").and(Sort.by(Sort.Direction.DESC, "createdAt"));

    private final ReconCaseJpaRepository reconCaseJpaRepository;
    private final ReconCaseNoteJpaRepository reconCaseNoteJpaRepository;
    private final PayBridgeSnapshotJpaRepository payBridgeSnapshotJpaRepository;
    private final PayBridgePaymentContextService payBridgePaymentContextService;

    public ReconCaseQueryService(
            ReconCaseJpaRepository reconCaseJpaRepository,
            ReconCaseNoteJpaRepository reconCaseNoteJpaRepository,
            PayBridgeSnapshotJpaRepository payBridgeSnapshotJpaRepository,
            PayBridgePaymentContextService payBridgePaymentContextService) {
        this.reconCaseJpaRepository = reconCaseJpaRepository;
        this.reconCaseNoteJpaRepository = reconCaseNoteJpaRepository;
        this.payBridgeSnapshotJpaRepository = payBridgeSnapshotJpaRepository;
        this.payBridgePaymentContextService = payBridgePaymentContextService;
    }

    public ReconCaseListView listCases(ReconCaseFilters filters) {
        Specification<ReconCaseJpaEntity> specification = from(filters);
        List<ReconCaseQueueItemView> items = reconCaseJpaRepository.findAll(specification, DEFAULT_SORT).stream()
            .map(this::toQueueItem)
            .toList();
        return new ReconCaseListView(items, items.size());
    }

    public ReconCaseDetailView getCaseDetail(UUID caseId) {
        ReconCaseJpaEntity reconCase = reconCaseJpaRepository.findById(caseId)
            .orElseThrow(() -> new IllegalArgumentException("Recon case not found: " + caseId));
        SettlementRowDetailView settlementRow = reconCase.getSettlementRow() != null
            ? toSettlementRowDetail(reconCase.getSettlementRow())
            : null;
        PayBridgeSnapshotDetailView snapshot = null;
        PayBridgePaymentContextView payBridgeContext = null;
        if (reconCase.getPaymentId() != null) {
            snapshot = payBridgeSnapshotJpaRepository.findByRun_IdAndPaymentId(reconCase.getRun().getId(), reconCase.getPaymentId())
                .map(this::toSnapshotDetail)
                .orElse(null);
            payBridgeContext = payBridgePaymentContextService.load(reconCase.getPaymentId());
        }
        List<ReconCaseNoteView> notes = reconCaseNoteJpaRepository.findByReconCase_IdOrderByCreatedAtAsc(caseId).stream()
            .map(note -> new ReconCaseNoteView(note.getId(), note.getAuthor(), note.getBody(), note.getCreatedAt()))
            .toList();

        return new ReconCaseDetailView(
            reconCase.getId(),
            reconCase.getRun().getId(),
            reconCase.getCaseType(),
            reconCase.getCaseStatus(),
            reconCase.getProvider(),
            reconCase.getSummary(),
            reconCase.getMatchKey(),
            reconCase.getOpenedAt(),
            reconCase.getResolvedAt(),
            settlementRow,
            snapshot,
            payBridgeContext,
            notes
        );
    }

    private ReconCaseQueueItemView toQueueItem(ReconCaseJpaEntity reconCase) {
        return new ReconCaseQueueItemView(
            reconCase.getId(),
            reconCase.getRun().getId(),
            reconCase.getCaseType(),
            reconCase.getCaseStatus(),
            reconCase.getProvider(),
            reconCase.getSummary(),
            reconCase.getMatchKey(),
            reconCase.getPaymentId(),
            reconCase.getSettlementRow() != null ? reconCase.getSettlementRow().getId() : null,
            reconCase.getOpenedAt(),
            reconCase.getResolvedAt()
        );
    }

    private SettlementRowDetailView toSettlementRowDetail(SettlementRowJpaEntity row) {
        return new SettlementRowDetailView(
            row.getId(),
            row.getRowNumber(),
            row.getProvider(),
            row.getOrderId(),
            row.getProviderPaymentId(),
            row.getProviderTransactionId(),
            row.getAmountMinor(),
            row.getCurrency(),
            row.getSettledAt(),
            row.getRawRowJson()
        );
    }

    private PayBridgeSnapshotDetailView toSnapshotDetail(PayBridgeSnapshotJpaEntity snapshot) {
        return new PayBridgeSnapshotDetailView(
            snapshot.getPaymentId(),
            snapshot.getOrderId(),
            snapshot.getProvider(),
            snapshot.getStatus(),
            snapshot.getAmountMinor(),
            snapshot.getReversibleAmountMinor(),
            snapshot.getCurrency(),
            snapshot.getProviderPaymentId(),
            snapshot.getProviderTransactionId(),
            snapshot.getApprovedAt(),
            snapshot.getUpstreamCreatedAt(),
            snapshot.getUpstreamUpdatedAt()
        );
    }

    private Specification<ReconCaseJpaEntity> from(ReconCaseFilters filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filters.runId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("run").get("id"), filters.runId()));
            }
            if (filters.caseStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("caseStatus"), filters.caseStatus()));
            }
            if (filters.caseType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("caseType"), filters.caseType()));
            }
            if (filters.provider() != null && !filters.provider().isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("provider")), filters.provider().trim().toLowerCase()));
            }
            if (filters.q() != null && !filters.q().isBlank()) {
                String like = "%" + filters.q().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("summary")), like),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("matchKey")), like)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
