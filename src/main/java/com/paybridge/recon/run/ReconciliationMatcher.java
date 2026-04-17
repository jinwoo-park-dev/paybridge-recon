package com.paybridge.recon.run;

import com.paybridge.recon.casework.ReconCaseType;
import com.paybridge.recon.settlement.SettlementRowJpaEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationMatcher {

    public List<ReconCaseDraft> match(
        List<SettlementRowJpaEntity> settlementRows,
        List<PayBridgeSnapshotJpaEntity> snapshots) {
        List<ReconCaseDraft> drafts = new ArrayList<>();
        Set<java.util.UUID> duplicateSettlementRowIds = new HashSet<>();

        Map<String, List<SettlementRowJpaEntity>> settlementByPrimaryKey = settlementRows.stream()
            .map(row -> Map.entry(primaryMatchKey(row), row))
            .filter(entry -> entry.getKey().isPresent())
            .collect(Collectors.groupingBy(entry -> entry.getKey().get(), Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (Map.Entry<String, List<SettlementRowJpaEntity>> entry : settlementByPrimaryKey.entrySet()) {
            List<SettlementRowJpaEntity> groupedRows = entry.getValue();
            if (groupedRows.size() <= 1) {
                continue;
            }
            for (int index = 1; index < groupedRows.size(); index++) {
                SettlementRowJpaEntity row = groupedRows.get(index);
                duplicateSettlementRowIds.add(row.getId());
                drafts.add(new ReconCaseDraft(
                    ReconCaseType.DUPLICATE_SETTLEMENT_ROW,
                    row.getProvider(),
                    null,
                    row,
                    "Settlement row duplicates match key " + entry.getKey() + ".",
                    entry.getKey()
                ));
            }
        }

        Map<String, PayBridgeSnapshotJpaEntity> byProviderPaymentId = uniqueIndex(snapshots, this::providerPaymentIdKey);
        Map<String, PayBridgeSnapshotJpaEntity> byProviderTransactionId = uniqueIndex(snapshots, this::providerTransactionIdKey);
        Map<String, PayBridgeSnapshotJpaEntity> byOrderId = uniqueIndex(snapshots, this::orderIdKey);
        Set<java.util.UUID> matchedSnapshotIds = new HashSet<>();

        for (SettlementRowJpaEntity row : settlementRows) {
            if (duplicateSettlementRowIds.contains(row.getId())) {
                continue;
            }
            MatchResult matchResult = findMatch(row, byProviderPaymentId, byProviderTransactionId, byOrderId);
            if (matchResult == null) {
                drafts.add(new ReconCaseDraft(
                    ReconCaseType.SETTLEMENT_ONLY,
                    row.getProvider(),
                    null,
                    row,
                    "Settlement row has no matching PayBridge payment for key " + displayMatchKey(row) + ".",
                    displayMatchKey(row)
                ));
                continue;
            }

            matchedSnapshotIds.add(matchResult.snapshot().getId());
            if (row.getAmountMinor() != matchResult.snapshot().getAmountMinor() || !row.getCurrency().equalsIgnoreCase(matchResult.snapshot().getCurrency())) {
                drafts.add(new ReconCaseDraft(
                    ReconCaseType.AMOUNT_MISMATCH,
                    row.getProvider(),
                    matchResult.snapshot().getPaymentId(),
                    row,
                    "Settlement amount/currency does not match PayBridge snapshot for key " + matchResult.matchKey() + ". "
                        + "Settlement=" + row.getAmountMinor() + " " + row.getCurrency()
                        + ", PayBridge=" + matchResult.snapshot().getAmountMinor() + " " + matchResult.snapshot().getCurrency() + ".",
                    matchResult.matchKey()
                ));
            }
        }

        for (PayBridgeSnapshotJpaEntity snapshot : snapshots) {
            if (matchedSnapshotIds.contains(snapshot.getId())) {
                continue;
            }
            drafts.add(new ReconCaseDraft(
                ReconCaseType.PAYBRIDGE_ONLY,
                snapshot.getProvider(),
                snapshot.getPaymentId(),
                null,
                "PayBridge payment has no matching settlement row for key " + displayMatchKey(snapshot) + ".",
                displayMatchKey(snapshot)
            ));
        }

        return drafts;
    }

    private MatchResult findMatch(
        SettlementRowJpaEntity row,
        Map<String, PayBridgeSnapshotJpaEntity> byProviderPaymentId,
        Map<String, PayBridgeSnapshotJpaEntity> byProviderTransactionId,
        Map<String, PayBridgeSnapshotJpaEntity> byOrderId) {
        String providerPaymentIdKey = providerPaymentIdKey(row);
        if (providerPaymentIdKey != null && byProviderPaymentId.containsKey(providerPaymentIdKey)) {
            return new MatchResult(byProviderPaymentId.get(providerPaymentIdKey), providerPaymentIdKey);
        }

        String providerTransactionIdKey = providerTransactionIdKey(row);
        if (providerTransactionIdKey != null && byProviderTransactionId.containsKey(providerTransactionIdKey)) {
            return new MatchResult(byProviderTransactionId.get(providerTransactionIdKey), providerTransactionIdKey);
        }

        String orderIdKey = orderIdKey(row);
        if (orderIdKey != null && byOrderId.containsKey(orderIdKey)) {
            return new MatchResult(byOrderId.get(orderIdKey), orderIdKey);
        }
        return null;
    }

    private Map<String, PayBridgeSnapshotJpaEntity> uniqueIndex(
        List<PayBridgeSnapshotJpaEntity> snapshots,
        Function<PayBridgeSnapshotJpaEntity, String> keyExtractor) {
        Map<String, List<PayBridgeSnapshotJpaEntity>> grouped = new HashMap<>();
        for (PayBridgeSnapshotJpaEntity snapshot : snapshots) {
            String key = keyExtractor.apply(snapshot);
            if (key == null) {
                continue;
            }
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(snapshot);
        }

        Map<String, PayBridgeSnapshotJpaEntity> unique = new HashMap<>();
        for (Map.Entry<String, List<PayBridgeSnapshotJpaEntity>> entry : grouped.entrySet()) {
            if (entry.getValue().size() == 1) {
                unique.put(entry.getKey(), entry.getValue().getFirst());
            }
        }
        return unique;
    }

    private Optional<String> primaryMatchKey(SettlementRowJpaEntity row) {
        return Optional.ofNullable(providerPaymentIdKey(row))
            .or(() -> Optional.ofNullable(providerTransactionIdKey(row)))
            .or(() -> Optional.ofNullable(orderIdKey(row)));
    }

    private String displayMatchKey(SettlementRowJpaEntity row) {
        return primaryMatchKey(row).orElse("ROW:" + row.getProvider() + ":" + row.getRowNumber());
    }

    private String displayMatchKey(PayBridgeSnapshotJpaEntity snapshot) {
        return firstNonNull(providerPaymentIdKey(snapshot), providerTransactionIdKey(snapshot), orderIdKey(snapshot), "PAYMENT:" + snapshot.getPaymentId());
    }

    private String providerPaymentIdKey(SettlementRowJpaEntity row) {
        return key(row.getProvider(), "providerPaymentId", row.getProviderPaymentId());
    }

    private String providerPaymentIdKey(PayBridgeSnapshotJpaEntity snapshot) {
        return key(snapshot.getProvider(), "providerPaymentId", snapshot.getProviderPaymentId());
    }

    private String providerTransactionIdKey(SettlementRowJpaEntity row) {
        return key(row.getProvider(), "providerTransactionId", row.getProviderTransactionId());
    }

    private String providerTransactionIdKey(PayBridgeSnapshotJpaEntity snapshot) {
        return key(snapshot.getProvider(), "providerTransactionId", snapshot.getProviderTransactionId());
    }

    private String orderIdKey(SettlementRowJpaEntity row) {
        return key(row.getProvider(), "orderId", row.getOrderId());
    }

    private String orderIdKey(PayBridgeSnapshotJpaEntity snapshot) {
        return key(snapshot.getProvider(), "orderId", snapshot.getOrderId());
    }

    private String key(String provider, String keyType, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return provider + "::" + keyType + "::" + value.trim();
    }

    private String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private record MatchResult(PayBridgeSnapshotJpaEntity snapshot, String matchKey) {
    }
}
