package com.paybridge.recon.web.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System", description = "Small JSON endpoint used to verify runtime metadata and active profile state.")
@RestController
@RequestMapping("/api/system")
public class SystemInfoApiController {

    private final SystemInfoViewFactory systemInfoViewFactory;

    public SystemInfoApiController(SystemInfoViewFactory systemInfoViewFactory) {
        this.systemInfoViewFactory = systemInfoViewFactory;
    }

    @Operation(
        summary = "Get service metadata",
        description = "Returns basic runtime metadata used by local smoke tests and environment verification.",
        responses = {
            @ApiResponse(responseCode = "200", description = "System metadata returned.",
                content = @Content(schema = @Schema(implementation = SystemInfoResponse.class)))
        }
    )
    @GetMapping("/info")
    public SystemInfoResponse info() {
        return systemInfoViewFactory.create();
    }
}
