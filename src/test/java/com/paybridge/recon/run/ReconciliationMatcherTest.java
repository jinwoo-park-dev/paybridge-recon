package com.paybridge.recon.run;

import static org.assertj.core.api.Assertions.assertThat;

import com.paybridge.recon.casework.ReconCaseType;
import com.paybridge.recon.settlement.SettlementRowJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReconciliationMatcherTest {

    private final ReconciliationMatcher matcher = new ReconciliationMatcher();

    @Test
    void createsExpectedCaseTypesForDuplicatesMissingSnapshotsAndAmountMismatch() {
        List<SettlementRowJpaEntity> settlementRows = List.of(
            settlementRow(1, "STRIPE", "ORD-2001", "pi_123", "ch_123", 2099, "USD"),
            settlementRow(2, "STRIPE", "ORD-2002", "pi_dup", "ch_dup", 500, "USD"),
            settlementRow(3, "STRIPE", "ORD-2002", "pi_dup", "ch_dup", 500, "USD"),
            settlementRow(4, "NICEPAY", "ORD-2009", "cp_999", "TID-999", 1000, "KRW")
        );

        List<PayBridgeSnapshotJpaEntity> snapshots = List.of(
            snapshot("11111111-1111-1111-1111-111111111111", "STRIPE", "ORD-2001", "pi_123", "ch_123", 1999, "USD"),
            snapshot("22222222-2222-2222-2222-222222222222", "STRIPE", "ORD-2002", "pi_dup", "ch_dup", 500, "USD"),
            snapshot("33333333-3333-3333-3333-333333333333", "STRIPE", "ORD-7777", "pi_paybridge_only", "ch_paybridge_only", 700, "USD")
        );

        var drafts = matcher.match(settlementRows, snapshots);

        assertThat(drafts).hasSize(4);
        assertThat(drafts).extracting(ReconCaseDraft::caseType)
            .containsExactlyInAnyOrder(
                ReconCaseType.AMOUNT_MISMATCH,
                ReconCaseType.DUPLICATE_SETTLEMENT_ROW,
                ReconCaseType.SETTLEMENT_ONLY,
                ReconCaseType.PAYBRIDGE_ONLY
            );
        assertThat(drafts)
            .filteredOn(draft -> draft.caseType() == ReconCaseType.DUPLICATE_SETTLEMENT_ROW)
            .singleElement()
            .extracting(draft -> draft.settlementRow().getRowNumber())
            .isEqualTo(3);
    }

    @Test
    void fallsBackFromProviderPaymentIdToProviderTransactionIdThenOrderId() {
        List<SettlementRowJpaEntity> settlementRows = List.of(
            settlementRow(1, "STRIPE", "ORD-3001", null, "ch_fallback", 1500, "USD"),
            settlementRow(2, "NICEPAY", "ORD-3002", null, null, 10004, "KRW")
        );

        List<PayBridgeSnapshotJpaEntity> snapshots = List.of(
            snapshot("44444444-4444-4444-4444-444444444444", "STRIPE", "ORD-9999", null, "ch_fallback", 1500, "USD"),
            snapshot("55555555-5555-5555-5555-555555555555", "NICEPAY", "ORD-3002", null, null, 10004, "KRW")
        );

        var drafts = matcher.match(settlementRows, snapshots);

        assertThat(drafts).isEmpty();
    }

    @Test
    void skipsNullSnapshotMatchKeysInsteadOfThrowingAndKeepsSnapshotAsPaybridgeOnly() {
        List<SettlementRowJpaEntity> settlementRows = List.of();

        List<PayBridgeSnapshotJpaEntity> snapshots = List.of(
            snapshot("66666666-6666-6666-6666-666666666666", "STRIPE", null, null, null, 1500, "USD")
        );

        var drafts = matcher.match(settlementRows, snapshots);

        assertThat(drafts).singleElement()
            .extracting(ReconCaseDraft::caseType)
            .isEqualTo(ReconCaseType.PAYBRIDGE_ONLY);
        assertThat(drafts.getFirst().matchKey()).isEqualTo("PAYMENT:66666666-6666-6666-6666-666666666666");
    }

    private SettlementRowJpaEntity settlementRow(
        int rowNumber,
        String provider,
        String orderId,
        String providerPaymentId,
        String providerTransactionId,
        long amountMinor,
        String currency) {
        SettlementRowJpaEntity row = new SettlementRowJpaEntity();
        ReflectionTestUtils.setField(row, "id", UUID.randomUUID());
        row.setRowNumber(rowNumber);
        row.setProvider(provider);
        row.setOrderId(orderId);
        row.setProviderPaymentId(providerPaymentId);
        row.setProviderTransactionId(providerTransactionId);
        row.setAmountMinor(amountMinor);
        row.setCurrency(currency);
        row.setSettledAt(Instant.parse("2025-01-15T12:30:00Z"));
        row.setRawRowJson("{}");
        return row;
    }

    private PayBridgeSnapshotJpaEntity snapshot(
        String paymentId,
        String provider,
        String orderId,
        String providerPaymentId,
        String providerTransactionId,
        long amountMinor,
        String currency) {
        PayBridgeSnapshotJpaEntity snapshot = new PayBridgeSnapshotJpaEntity();
        ReflectionTestUtils.setField(snapshot, "id", UUID.randomUUID());
        snapshot.setPaymentId(UUID.fromString(paymentId));
        snapshot.setProvider(provider);
        snapshot.setOrderId(orderId);
        snapshot.setStatus("APPROVED");
        snapshot.setAmountMinor(amountMinor);
        snapshot.setReversibleAmountMinor(amountMinor);
        snapshot.setCurrency(currency);
        snapshot.setProviderPaymentId(providerPaymentId);
        snapshot.setProviderTransactionId(providerTransactionId);
        snapshot.setApprovedAt(Instant.parse("2025-01-15T12:00:00Z"));
        snapshot.setUpstreamCreatedAt(Instant.parse("2025-01-15T11:59:58Z"));
        snapshot.setUpstreamUpdatedAt(Instant.parse("2025-01-15T12:00:01Z"));
        return snapshot;
    }
}
