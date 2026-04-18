package com.paybridge.recon.casework;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReconCaseJpaRepository extends JpaRepository<ReconCaseJpaEntity, UUID>, JpaSpecificationExecutor<ReconCaseJpaEntity> {
}
