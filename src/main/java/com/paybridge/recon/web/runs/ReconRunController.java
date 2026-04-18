package com.paybridge.recon.web.runs;

import com.paybridge.recon.run.ReconRunQueryService;
import com.paybridge.recon.run.ReconRunRequest;
import com.paybridge.recon.run.ReconRunService;
import com.paybridge.recon.run.ReconRunStatus;
import com.paybridge.recon.settlement.SettlementImportPageQueryService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReconRunController {

    private final SettlementImportPageQueryService settlementImportPageQueryService;
    private final ReconRunQueryService reconRunQueryService;
    private final ReconRunService reconRunService;

    public ReconRunController(
            SettlementImportPageQueryService settlementImportPageQueryService,
            ReconRunQueryService reconRunQueryService,
            ReconRunService reconRunService) {
        this.settlementImportPageQueryService = settlementImportPageQueryService;
        this.reconRunQueryService = reconRunQueryService;
        this.reconRunService = reconRunService;
    }

    @GetMapping("/runs/new")
    public String newRunPage(Model model) {
        model.addAttribute("recentBatches", settlementImportPageQueryService.recentBatches());
        model.addAttribute("recentRuns", reconRunQueryService.recentRuns());
        return "runs/new";
    }

    @PostMapping("/runs")
    public String createRun(
            @RequestParam UUID batchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant approvedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant approvedTo,
            RedirectAttributes redirectAttributes) {
        try {
            var runSummary = reconRunService.run(new ReconRunRequest(batchId, approvedFrom, approvedTo));
            if (runSummary.status() == ReconRunStatus.FAILED) {
                redirectAttributes.addFlashAttribute("errorMessage", "Run failed: " + runSummary.errorSummary());
                return "redirect:/runs/new";
            }
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Created recon run " + runSummary.runId() + " with " + runSummary.caseCount() + " discrepancy cases."
            );
            return "redirect:/workbench/cases?runId=" + runSummary.runId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/runs/new";
        }
    }
}
