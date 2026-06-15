package com.tangqilin.competition.controller;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.NotificationCreateDTO;
import com.tangqilin.competition.service.PlatformService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final PlatformService platformService;

    public NotificationController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping
    public Result<?> notifications() {
        return platformService.notifications();
    }

    @PutMapping("/{id}/read")
    public Result<?> markRead(@PathVariable Long id) {
        return platformService.markNotificationRead(id);
    }

    @PostMapping
    public Result<?> createNotification(@RequestBody NotificationCreateDTO notificationCreateDTO) {
        return platformService.createNotification(notificationCreateDTO);
    }
}
