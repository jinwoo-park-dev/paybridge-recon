package com.paybridge.recon.casework;

import com.paybridge.recon.support.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recon_case_notes")
public class ReconCaseNoteJpaEntity extends AbstractJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private ReconCaseJpaEntity reconCase;

    @Column(name = "author", nullable = false, length = 100)
    private String author;

    @Column(name = "body", nullable = false, length = 2000)
    private String body;

    public ReconCaseJpaEntity getReconCase() {
        return reconCase;
    }

    public void setReconCase(ReconCaseJpaEntity reconCase) {
        this.reconCase = reconCase;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
