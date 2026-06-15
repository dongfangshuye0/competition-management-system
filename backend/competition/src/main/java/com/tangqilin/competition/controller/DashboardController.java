package com.tangqilin.competition.controller;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.service.PlatformService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final PlatformService platformService;

    public DashboardController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/api/dashboard")
    public Result<?> dashboardSummary() {
        return platformService.dashboardSummary();
    }
}
