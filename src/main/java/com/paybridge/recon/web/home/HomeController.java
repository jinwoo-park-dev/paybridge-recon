package com.paybridge.recon.web.home;

import com.paybridge.recon.integration.paybridge.PayBridgeConnectivityService;
import com.paybridge.recon.run.ReconRunQueryService;
import com.paybridge.recon.settlement.SettlementImportPageQueryService;
import com.paybridge.recon.web.system.SystemInfoViewFactory;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final int OVERVIEW_ROW_LIMIT = 3;

    private final PayBridgeConnectivityService payBridgeConnectivityService;
    private final SettlementImportPageQueryService settlementImportPageQueryService;
    private final ReconRunQueryService reconRunQueryService;
    private final SystemInfoViewFactory systemInfoViewFactory;

    public HomeController(
            PayBridgeConnectivityService payBridgeConnectivityService,
            SettlementImportPageQueryService settlementImportPageQueryService,
            ReconRunQueryService reconRunQueryService,
            SystemInfoViewFactory systemInfoViewFactory) {
        this.payBridgeConnectivityService = payBridgeConnectivityService;
        this.settlementImportPageQueryService = settlementImportPageQueryService;
        this.reconRunQueryService = reconRunQueryService;
        this.systemInfoViewFactory = systemInfoViewFactory;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("systemInfo", systemInfoViewFactory.create());
        model.addAttribute("payBridgeConnection", payBridgeConnectivityService.checkConnection());
        model.addAttribute(
            "recentBatches",
            limit(settlementImportPageQueryService.recentBatches(), OVERVIEW_ROW_LIMIT)
        );
        model.addAttribute(
            "recentRuns",
            limit(reconRunQueryService.recentRuns(), OVERVIEW_ROW_LIMIT)
        );
        return "home/index";
    }

    private static <T> List<T> limit(List<T> items, int maxSize) {
        if (items.size() <= maxSize) {
            return items;
        }
        return List.copyOf(items.subList(0, maxSize));
    }
}
