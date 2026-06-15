package com.tangqilin.competition.controller;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.RegistrationCreateDTO;
import com.tangqilin.competition.entity.dto.RegistrationReviewDTO;
import com.tangqilin.competition.service.PlatformService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final PlatformService platformService;

    public RegistrationController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @PostMapping
    public Result<?> createRegistration(@ModelAttribute RegistrationCreateDTO registrationCreateDTO) {
        return platformService.createRegistration(registrationCreateDTO);
    }

    @GetMapping("/my")
    public Result<?> myRegistrations() {
        return platformService.myRegistrations();
    }

    @GetMapping("/teacher")
    public Result<?> teacherRegistrations() {
        return platformService.teacherRegistrations();
    }

    @GetMapping
    public Result<?> allRegistrations() {
        return platformService.allRegistrations();
    }

    @PutMapping("/{id}/review")
    public Result<?> reviewRegistration(@PathVariable Long id, @RequestBody RegistrationReviewDTO registrationReviewDTO) {
        return platformService.reviewRegistration(id, registrationReviewDTO);
    }
}
