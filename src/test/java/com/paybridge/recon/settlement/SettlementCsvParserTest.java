package com.paybridge.recon.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SettlementCsvParserTest {

    private final SettlementCsvParser parser = new SettlementCsvParser();

    @Test
    void parsesNormalizedSettlementRows() {
        String csv = "provider,order_id,provider_payment_id,provider_transaction_id,amount_minor,currency,settled_at\n"
            + "STRIPE,ORD-2001,pi_123,ch_123,1999,USD,2025-01-15T12:30:00Z\n"
            + "NICEPAY,ORD-2002,cp_456,TID-1001,10004,KRW,2025-01-15T12:31:00Z\n";

        var rows = parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        assertThat(rows).hasSize(2);
        assertThat(rows.getFirst().provider()).isEqualTo("STRIPE");
        assertThat(rows.getFirst().rowNumber()).isEqualTo(2);
        assertThat(rows.getFirst().amountMinor()).isEqualTo(1999L);
        assertThat(rows.getFirst().settledAt()).isEqualTo(Instant.parse("2025-01-15T12:30:00Z"));
        assertThat(rows.get(1).currency()).isEqualTo("KRW");
    }

    @Test
    void rejectsUnexpectedHeaderOrder() {
        String csv = "provider,order_id,provider_transaction_id,provider_payment_id,amount_minor,currency,settled_at\n"
            + "STRIPE,ORD-2001,ch_123,pi_123,1999,USD,2025-01-15T12:30:00Z\n";

        assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))))
            .isInstanceOf(SettlementImportException.class)
            .hasMessageContaining("Unexpected settlement CSV header order");
    }
}
