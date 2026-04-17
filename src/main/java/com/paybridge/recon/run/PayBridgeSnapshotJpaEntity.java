package com.paybridge.recon.run;

import com.paybridge.recon.support.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paybridge_snapshots")
public class PayBridgeSnapshotJpaEntity extends AbstractJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private ReconRunJpaEntity run;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", length = 128)
    private String orderId;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "reversible_amount_minor", nullable = false)
    private long reversibleAmountMinor;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "provider_payment_id", length = 128)
    private String providerPaymentId;

    @Column(name = "provider_transaction_id", length = 128)
    private String providerTransactionId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "upstream_created_at")
    private Instant upstreamCreatedAt;

    @Column(name = "upstream_updated_at")
    private Instant upstreamUpdatedAt;

    public ReconRunJpaEntity getRun() {
        return run;
    }

    public void setRun(ReconRunJpaEntity run) {
        this.run = run;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public long getReversibleAmountMinor() {
        return reversibleAmountMinor;
    }

    public void setReversibleAmountMinor(long reversibleAmountMinor) {
        this.reversibleAmountMinor = reversibleAmountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public void setProviderPaymentId(String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
    }

    public String getProviderTransactionId() {
        return providerTransactionId;
    }

    public void setProviderTransactionId(String providerTransactionId) {
        this.providerTransactionId = providerTransactionId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getUpstreamCreatedAt() {
        return upstreamCreatedAt;
    }

    public void setUpstreamCreatedAt(Instant upstreamCreatedAt) {
        this.upstreamCreatedAt = upstreamCreatedAt;
    }

    public Instant getUpstreamUpdatedAt() {
        return upstreamUpdatedAt;
    }

    public void setUpstreamUpdatedAt(Instant upstreamUpdatedAt) {
        this.upstreamUpdatedAt = upstreamUpdatedAt;
    }
}
