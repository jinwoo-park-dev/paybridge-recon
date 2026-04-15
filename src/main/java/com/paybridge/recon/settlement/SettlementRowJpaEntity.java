package com.paybridge.recon.settlement;

import com.paybridge.recon.support.persistence.AbstractJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "settlement_rows")
public class SettlementRowJpaEntity extends AbstractJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private SettlementImportBatchJpaEntity batch;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    @Column(name = "provider", nullable = false, length = 32)
    private String provider;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "provider_payment_id")
    private String providerPaymentId;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "settled_at", nullable = false)
    private Instant settledAt;

    @Column(name = "raw_row_json", nullable = false, length = 4000)
    private String rawRowJson;

    public SettlementImportBatchJpaEntity getBatch() {
        return batch;
    }

    public void setBatch(SettlementImportBatchJpaEntity batch) {
        this.batch = batch;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(Instant settledAt) {
        this.settledAt = settledAt;
    }

    public String getRawRowJson() {
        return rawRowJson;
    }

    public void setRawRowJson(String rawRowJson) {
        this.rawRowJson = rawRowJson;
    }
}
