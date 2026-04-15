package com.paybridge.recon.settlement;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SettlementImportPageQueryService {

    private final SettlementImportBatchJpaRepository batchRepository;

    public SettlementImportPageQueryService(SettlementImportBatchJpaRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public List<SettlementImportSummary> recentBatches() {
        return batchRepository.findTop10ByOrderByUploadedAtDesc().stream()
            .map(batch -> new SettlementImportSummary(
                batch.getId(),
                batch.getFilename(),
                batch.getRowCount(),
                batch.getUploadedBy(),
                batch.getUploadedAt(),
                batch.getStatus()
            ))
            .toList();
    }
}
