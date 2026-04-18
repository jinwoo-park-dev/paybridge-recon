package com.paybridge.recon.casework;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReconCaseCommandService {

    private final ReconCaseJpaRepository reconCaseJpaRepository;
    private final ReconCaseNoteJpaRepository reconCaseNoteJpaRepository;

    public ReconCaseCommandService(
            ReconCaseJpaRepository reconCaseJpaRepository,
            ReconCaseNoteJpaRepository reconCaseNoteJpaRepository) {
        this.reconCaseJpaRepository = reconCaseJpaRepository;
        this.reconCaseNoteJpaRepository = reconCaseNoteJpaRepository;
    }

    @Transactional
    public ReconCaseStatusUpdateView updateStatus(UUID caseId, ReconCaseStatus targetStatus) {
        ReconCaseJpaEntity reconCase = getCase(caseId);
        reconCase.setCaseStatus(targetStatus);
        if (targetStatus == ReconCaseStatus.RESOLVED || targetStatus == ReconCaseStatus.IGNORED) {
            reconCase.setResolvedAt(Instant.now());
        } else {
            reconCase.setResolvedAt(null);
        }
        ReconCaseJpaEntity saved = reconCaseJpaRepository.save(reconCase);
        return new ReconCaseStatusUpdateView(saved.getId(), saved.getCaseStatus(), saved.getResolvedAt());
    }

    @Transactional
    public ReconCaseNoteView addNote(UUID caseId, String author, String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Case note body must not be blank.");
        }
        ReconCaseJpaEntity reconCase = getCase(caseId);
        ReconCaseNoteJpaEntity note = new ReconCaseNoteJpaEntity();
        note.setReconCase(reconCase);
        note.setAuthor(author);
        note.setBody(body.trim());
        ReconCaseNoteJpaEntity saved = reconCaseNoteJpaRepository.save(note);
        return new ReconCaseNoteView(saved.getId(), saved.getAuthor(), saved.getBody(), saved.getCreatedAt());
    }

    private ReconCaseJpaEntity getCase(UUID caseId) {
        return reconCaseJpaRepository.findById(caseId)
            .orElseThrow(() -> new IllegalArgumentException("Recon case not found: " + caseId));
    }
}
