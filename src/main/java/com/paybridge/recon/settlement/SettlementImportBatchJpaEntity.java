package com.paybridge.recon.settlement;

import com.paybridge.recon.support.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "settlement_import_batches")
public class SettlementImportBatchJpaEntity extends AbstractJpaEntity {

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private SettlementImportBatchStatus status;

    @OneToMany(mappedBy = "batch")
    private List<SettlementRowJpaEntity> rows = new ArrayList<>();

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public SettlementImportBatchStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementImportBatchStatus status) {
        this.status = status;
    }

    public List<SettlementRowJpaEntity> getRows() {
        return rows;
    }
}
