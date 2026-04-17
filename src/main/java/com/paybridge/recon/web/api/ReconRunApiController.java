package com.paybridge.recon.web.api;

import com.paybridge.recon.run.ReconRunRequest;
import com.paybridge.recon.run.ReconRunService;
import com.paybridge.recon.run.ReconRunSummaryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Runs", description = "Manual reconciliation run creation endpoints.")
@RestController
@RequestMapping("/api/recon/runs")
public class ReconRunApiController {

    private final ReconRunService reconRunService;

    public ReconRunApiController(ReconRunService reconRunService) {
        this.reconRunService = reconRunService;
    }

    @Operation(
        summary = "Create a manual reconciliation run",
        responses = {
            @ApiResponse(responseCode = "200", description = "Run created successfully.",
                content = @Content(schema = @Schema(implementation = ReconRunSummaryView.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed.",
                content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
        }
    )
    @PostMapping
    public ReconRunSummaryView createRun(@Valid @RequestBody ReconRunCreateRequest request) {
        return reconRunService.run(new ReconRunRequest(request.batchId(), request.approvedFrom(), request.approvedTo()));
    }
}
