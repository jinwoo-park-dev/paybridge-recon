package com.paybridge.recon.run;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayBridgeSnapshotJpaRepository extends JpaRepository<PayBridgeSnapshotJpaEntity, UUID> {

    List<PayBridgeSnapshotJpaEntity> findByRun_IdOrderByApprovedAtDesc(UUID runId);

    Optional<PayBridgeSnapshotJpaEntity> findByRun_IdAndPaymentId(UUID runId, UUID paymentId);
}
