package com.paybridge.recon.casework;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReconCaseNoteJpaRepository extends JpaRepository<ReconCaseNoteJpaEntity, UUID> {

    List<ReconCaseNoteJpaEntity> findByReconCase_IdOrderByCreatedAtAsc(UUID caseId);
}
