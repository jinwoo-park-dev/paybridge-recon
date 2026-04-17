package com.paybridge.recon.run;

import com.paybridge.recon.settlement.SettlementImportBatchJpaEntity;
import com.paybridge.recon.support.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "recon_runs")
public class ReconRunJpaEntity extends AbstractJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private SettlementImportBatchJpaEntity batch;

    @Column(name = "approved_from")
    private Instant approvedFrom;

    @Column(name = "approved_to")
    private Instant approvedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ReconRunStatus status;

    @Column(name = "paybridge_row_count", nullable = false)
    private int paybridgeRowCount;

    @Column(name = "settlement_row_count", nullable = false)
    private int settlementRowCount;

    @Column(name = "case_count", nullable = false)
    private int caseCount;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "error_summary", length = 1000)
    private String errorSummary;

    public SettlementImportBatchJpaEntity getBatch() {
        return batch;
    }

    public void setBatch(SettlementImportBatchJpaEntity batch) {
        this.batch = batch;
    }

    public Instant getApprovedFrom() {
        return approvedFrom;
    }

    public void setApprovedFrom(Instant approvedFrom) {
        this.approvedFrom = approvedFrom;
    }

    public Instant getApprovedTo() {
        return approvedTo;
    }

    public void setApprovedTo(Instant approvedTo) {
        this.approvedTo = approvedTo;
    }

    public ReconRunStatus getStatus() {
        return status;
    }

    public void setStatus(ReconRunStatus status) {
        this.status = status;
    }

    public int getPaybridgeRowCount() {
        return paybridgeRowCount;
    }

    public void setPaybridgeRowCount(int paybridgeRowCount) {
        this.paybridgeRowCount = paybridgeRowCount;
    }

    public int getSettlementRowCount() {
        return settlementRowCount;
    }

    public void setSettlementRowCount(int settlementRowCount) {
        this.settlementRowCount = settlementRowCount;
    }

    public int getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(int caseCount) {
        this.caseCount = caseCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getErrorSummary() {
        return errorSummary;
    }

    public void setErrorSummary(String errorSummary) {
        this.errorSummary = errorSummary;
    }
}
