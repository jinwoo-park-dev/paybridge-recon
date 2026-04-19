package com.paybridge.recon.web.workbench;

import com.paybridge.recon.casework.ReconCaseStatus;
import com.paybridge.recon.casework.ReconCaseType;
import com.paybridge.recon.run.ReconRunQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Workbench", description = "Same-origin bootstrap endpoint used by the React case workbench.")
@RestController
@RequestMapping("/api/recon/workbench")
public class WorkbenchApiController {

    private final ReconRunQueryService reconRunQueryService;

    public WorkbenchApiController(ReconRunQueryService reconRunQueryService) {
        this.reconRunQueryService = reconRunQueryService;
    }

    @Operation(summary = "Get workbench bootstrap metadata")
    @GetMapping("/bootstrap")
    public WorkbenchBootstrapView bootstrap(Principal principal, CsrfToken csrfToken) {
        return new WorkbenchBootstrapView(
            principal != null ? principal.getName() : "operator",
            csrfToken.getHeaderName(),
            csrfToken.getToken(),
            reconRunQueryService.recentRuns(),
            List.of(ReconCaseStatus.values()),
            List.of(ReconCaseType.values())
        );
    }
}
