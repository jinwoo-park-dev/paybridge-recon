package com.paybridge.recon.casework;

import com.paybridge.recon.run.ReconRunJpaEntity;
import com.paybridge.recon.settlement.SettlementRowJpaEntity;
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
import java.util.UUID;

@Entity
@Table(name = "recon_cases")
public class ReconCaseJpaEntity extends AbstractJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private ReconRunJpaEntity run;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", nullable = false, length = 64)
    private ReconCaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_status", nullable = false, length = 32)
    private ReconCaseStatus caseStatus;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "payment_id")
    private UUID paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_row_id")
    private SettlementRowJpaEntity settlementRow;

    @Column(name = "summary", nullable = false, length = 500)
    private String summary;

    @Column(name = "match_key", nullable = false, length = 255)
    private String matchKey;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public ReconRunJpaEntity getRun() {
        return run;
    }

    public void setRun(ReconRunJpaEntity run) {
        this.run = run;
    }

    public ReconCaseType getCaseType() {
        return caseType;
    }

    public void setCaseType(ReconCaseType caseType) {
        this.caseType = caseType;
    }

    public ReconCaseStatus getCaseStatus() {
        return caseStatus;
    }

    public void setCaseStatus(ReconCaseStatus caseStatus) {
        this.caseStatus = caseStatus;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public SettlementRowJpaEntity getSettlementRow() {
        return settlementRow;
    }

    public void setSettlementRow(SettlementRowJpaEntity settlementRow) {
        this.settlementRow = settlementRow;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getMatchKey() {
        return matchKey;
    }

    public void setMatchKey(String matchKey) {
        this.matchKey = matchKey;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
