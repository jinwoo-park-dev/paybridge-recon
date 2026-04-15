package com.paybridge.recon.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRowJpaRepository extends JpaRepository<SettlementRowJpaEntity, UUID> {

    List<SettlementRowJpaEntity> findByBatch_IdOrderByRowNumberAsc(UUID batchId);
}
