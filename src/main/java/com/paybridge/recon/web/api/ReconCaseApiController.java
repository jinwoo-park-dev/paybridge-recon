package com.paybridge.recon.web.api;

import com.paybridge.recon.casework.ReconCaseCommandService;
import com.paybridge.recon.casework.ReconCaseDetailView;
import com.paybridge.recon.casework.ReconCaseFilters;
import com.paybridge.recon.casework.ReconCaseListView;
import com.paybridge.recon.casework.ReconCaseNoteView;
import com.paybridge.recon.casework.ReconCaseQueryService;
import com.paybridge.recon.casework.ReconCaseStatus;
import com.paybridge.recon.casework.ReconCaseStatusUpdateView;
import com.paybridge.recon.casework.ReconCaseType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Workbench", description = "Case queue, case detail, and local case write endpoints for the reconciliation workbench.")
@RestController
@RequestMapping("/api/recon/cases")
public class ReconCaseApiController {

    private final ReconCaseQueryService reconCaseQueryService;
    private final ReconCaseCommandService reconCaseCommandService;

    public ReconCaseApiController(
            ReconCaseQueryService reconCaseQueryService,
            ReconCaseCommandService reconCaseCommandService) {
        this.reconCaseQueryService = reconCaseQueryService;
        this.reconCaseCommandService = reconCaseCommandService;
    }

    @Operation(summary = "List discrepancy cases")
    @GetMapping
    public ReconCaseListView cases(
            @RequestParam(required = false) UUID runId,
            @RequestParam(required = false) ReconCaseStatus caseStatus,
            @RequestParam(required = false) ReconCaseType caseType,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) String q) {
        return reconCaseQueryService.listCases(new ReconCaseFilters(runId, caseStatus, caseType, provider, q));
    }

    @Operation(summary = "Get a discrepancy case detail")
    @GetMapping("/{caseId}")
    public ReconCaseDetailView detail(@PathVariable UUID caseId) {
        return reconCaseQueryService.getCaseDetail(caseId);
    }

    @Operation(
        summary = "Update a case status",
        responses = {
            @ApiResponse(responseCode = "200", description = "Case status updated.",
                content = @Content(schema = @Schema(implementation = ReconCaseStatusUpdateView.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed.",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        }
    )
    @PatchMapping("/{caseId}/status")
    public ReconCaseStatusUpdateView updateStatus(
            @PathVariable UUID caseId,
            @Valid @RequestBody ReconCaseStatusUpdateRequest request) {
        return reconCaseCommandService.updateStatus(caseId, request.status());
    }

    @Operation(
        summary = "Add a case note",
        responses = {
            @ApiResponse(responseCode = "200", description = "Case note created.",
                content = @Content(schema = @Schema(implementation = ReconCaseNoteView.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed.",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        }
    )
    @PostMapping("/{caseId}/notes")
    public ReconCaseNoteView createNote(
            @PathVariable UUID caseId,
            @Valid @RequestBody ReconCaseNoteCreateRequest request,
            Principal principal) {
        return reconCaseCommandService.addNote(caseId, principal.getName(), request.body());
    }
}
