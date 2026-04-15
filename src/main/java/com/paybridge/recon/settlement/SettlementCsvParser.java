package com.paybridge.recon.settlement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

@Component
public class SettlementCsvParser {

    static final List<String> EXPECTED_HEADERS = List.of(
        "provider",
        "order_id",
        "provider_payment_id",
        "provider_transaction_id",
        "amount_minor",
        "currency",
        "settled_at"
    );

    public List<SettlementCsvRow> parse(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setIgnoreEmptyLines(true)
                 .setTrim(true)
                 .get()
                 .parse(reader)) {

            validateHeaders(parser.getHeaderNames());
            List<SettlementCsvRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                int rowNumber = Math.toIntExact(record.getRecordNumber()) + 1;
                Map<String, String> rawRow = new LinkedHashMap<>();
                for (String header : EXPECTED_HEADERS) {
                    rawRow.put(header, record.get(header));
                }
                rows.add(new SettlementCsvRow(
                    rowNumber,
                    required(record, "provider").toUpperCase(Locale.ROOT),
                    trimToNull(record.get("order_id")),
                    trimToNull(record.get("provider_payment_id")),
                    trimToNull(record.get("provider_transaction_id")),
                    parseAmount(record.get("amount_minor")),
                    required(record, "currency").toUpperCase(Locale.ROOT),
                    parseInstant(record.get("settled_at")),
                    rawRow
                ));
            }
            return rows;
        } catch (IOException ex) {
            throw new SettlementImportException("Failed to read settlement CSV.", ex);
        }
    }

    private void validateHeaders(List<String> actualHeaders) {
        if (!EXPECTED_HEADERS.equals(actualHeaders)) {
            throw new SettlementImportException(
                "Unexpected settlement CSV header order. Expected " + EXPECTED_HEADERS + " but got " + actualHeaders
            );
        }
    }

    private String required(CSVRecord record, String field) {
        String value = trimToNull(record.get(field));
        if (value == null) {
            throw new SettlementImportException("Missing required value for field '" + field + "' at CSV row " + (record.getRecordNumber() + 1));
        }
        return value;
    }

    private long parseAmount(String rawAmount) {
        try {
            return Long.parseLong(requiredValue(rawAmount, "amount_minor"));
        } catch (NumberFormatException ex) {
            throw new SettlementImportException("amount_minor must be a whole-number minor-unit amount.", ex);
        }
    }

    private Instant parseInstant(String rawInstant) {
        try {
            return Instant.parse(requiredValue(rawInstant, "settled_at"));
        } catch (DateTimeParseException ex) {
            throw new SettlementImportException("settled_at must be an ISO-8601 instant.", ex);
        }
    }

    private String requiredValue(String rawValue, String field) {
        String value = trimToNull(rawValue);
        if (value == null) {
            throw new SettlementImportException("Missing required value for field '" + field + "'.");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
