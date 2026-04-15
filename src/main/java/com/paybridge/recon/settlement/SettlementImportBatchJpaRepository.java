package com.paybridge.recon.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementImportBatchJpaRepository extends JpaRepository<SettlementImportBatchJpaEntity, UUID> {

    List<SettlementImportBatchJpaEntity> findTop10ByOrderByUploadedAtDesc();
}
