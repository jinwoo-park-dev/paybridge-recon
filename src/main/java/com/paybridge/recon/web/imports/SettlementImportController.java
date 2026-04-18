package com.paybridge.recon.web.imports;

import com.paybridge.recon.settlement.SettlementImportException;
import com.paybridge.recon.settlement.SettlementImportPageQueryService;
import com.paybridge.recon.settlement.SettlementImportService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SettlementImportController {

    private final SettlementImportService settlementImportService;
    private final SettlementImportPageQueryService settlementImportPageQueryService;

    public SettlementImportController(
            SettlementImportService settlementImportService,
            SettlementImportPageQueryService settlementImportPageQueryService) {
        this.settlementImportService = settlementImportService;
        this.settlementImportPageQueryService = settlementImportPageQueryService;
    }

    @GetMapping("/imports")
    public String importsPage(Model model) {
        model.addAttribute("recentBatches", settlementImportPageQueryService.recentBatches());
        return "imports/index";
    }

    @PostMapping("/imports")
    public String uploadSettlementCsv(
            @RequestParam("file") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            var summary = settlementImportService.importCsv(file, principal.getName());
            redirectAttributes.addFlashAttribute(
                "successMessage",
                "Imported " + summary.rowCount() + " settlement rows from " + summary.filename() + "."
            );
        } catch (SettlementImportException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/imports";
    }
}
