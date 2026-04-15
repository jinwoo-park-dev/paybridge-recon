package com.paybridge.recon.settlement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SettlementImportService {

    private final SettlementCsvParser settlementCsvParser;
    private final SettlementImportBatchJpaRepository batchRepository;
    private final SettlementRowJpaRepository rowRepository;
    private final ObjectMapper objectMapper;

    public SettlementImportService(
            SettlementCsvParser settlementCsvParser,
            SettlementImportBatchJpaRepository batchRepository,
            SettlementRowJpaRepository rowRepository,
            ObjectMapper objectMapper) {
        this.settlementCsvParser = settlementCsvParser;
        this.batchRepository = batchRepository;
        this.rowRepository = rowRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SettlementImportSummary importCsv(MultipartFile csvFile, String uploadedBy) {
        if (csvFile == null || csvFile.isEmpty()) {
            throw new SettlementImportException("Please choose a non-empty settlement CSV file.");
        }

        List<SettlementCsvRow> rows = parseRows(csvFile);
        SettlementImportBatchJpaEntity batch = new SettlementImportBatchJpaEntity();
        batch.setFilename(csvFile.getOriginalFilename() != null ? csvFile.getOriginalFilename() : "uploaded-settlement.csv");
        batch.setRowCount(rows.size());
        batch.setUploadedAt(Instant.now());
        batch.setUploadedBy(uploadedBy);
        batch.setStatus(SettlementImportBatchStatus.UPLOADED);
        SettlementImportBatchJpaEntity persistedBatch = batchRepository.save(batch);

        List<SettlementRowJpaEntity> entities = rows.stream()
            .map(row -> toEntity(persistedBatch, row))
            .toList();
        rowRepository.saveAll(entities);

        return new SettlementImportSummary(
            persistedBatch.getId(),
            persistedBatch.getFilename(),
            persistedBatch.getRowCount(),
            persistedBatch.getUploadedBy(),
            persistedBatch.getUploadedAt(),
            persistedBatch.getStatus()
        );
    }

    private List<SettlementCsvRow> parseRows(MultipartFile csvFile) {
        try {
            return settlementCsvParser.parse(csvFile.getInputStream());
        } catch (IOException ex) {
            throw new SettlementImportException("Failed to open uploaded settlement CSV.", ex);
        }
    }

    private SettlementRowJpaEntity toEntity(SettlementImportBatchJpaEntity batch, SettlementCsvRow row) {
        SettlementRowJpaEntity entity = new SettlementRowJpaEntity();
        entity.setBatch(batch);
        entity.setRowNumber(row.rowNumber());
        entity.setProvider(row.provider());
        entity.setOrderId(row.orderId());
        entity.setProviderPaymentId(row.providerPaymentId());
        entity.setProviderTransactionId(row.providerTransactionId());
        entity.setAmountMinor(row.amountMinor());
        entity.setCurrency(row.currency());
        entity.setSettledAt(row.settledAt());
        entity.setRawRowJson(toJson(row.rawRow()));
        return entity;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new SettlementImportException("Failed to serialize imported settlement row.", ex);
        }
    }
}
