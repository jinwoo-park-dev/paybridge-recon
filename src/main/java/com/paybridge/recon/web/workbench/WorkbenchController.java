package com.paybridge.recon.web.workbench;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkbenchController {

    @GetMapping("/workbench/cases")
    public String workbench() {
        return "workbench/index";
    }
}
