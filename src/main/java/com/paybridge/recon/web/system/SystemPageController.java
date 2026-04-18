package com.paybridge.recon.web.system;

import com.paybridge.recon.integration.paybridge.PayBridgeConnectivityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SystemPageController {

    private final SystemInfoViewFactory systemInfoViewFactory;
    private final PayBridgeConnectivityService payBridgeConnectivityService;

    public SystemPageController(
            SystemInfoViewFactory systemInfoViewFactory,
            PayBridgeConnectivityService payBridgeConnectivityService) {
        this.systemInfoViewFactory = systemInfoViewFactory;
        this.payBridgeConnectivityService = payBridgeConnectivityService;
    }

    @GetMapping("/system")
    public String systemPage(Model model) {
        model.addAttribute("systemInfo", systemInfoViewFactory.create());
        model.addAttribute("payBridgeConnection", payBridgeConnectivityService.checkConnection());
        return "system/index";
    }
}
