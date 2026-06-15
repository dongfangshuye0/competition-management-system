package com.tangqilin.competition.controller;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.ScoreDTO;
import com.tangqilin.competition.entity.dto.WorkAssignReviewerDTO;
import com.tangqilin.competition.entity.dto.WorkSubmitDTO;
import com.tangqilin.competition.service.PlatformService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final PlatformService platformService;

    public WorkController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @PostMapping
    public Result<?> submitWork(@ModelAttribute WorkSubmitDTO workSubmitDTO) {
        return platformService.submitWork(workSubmitDTO);
    }

    @GetMapping("/my")
    public Result<?> myWorks() {
        return platformService.myWorks();
    }

    @GetMapping("/teacher")
    public Result<?> teacherWorks() {
        return platformService.teacherWorks();
    }

    @GetMapping
    public Result<?> allWorks() {
        return platformService.allWorks();
    }

    @PutMapping("/{id}/assign-reviewer")
    public Result<?> assignReviewer(@PathVariable Long id, @RequestBody WorkAssignReviewerDTO workAssignReviewerDTO) {
        return platformService.assignReviewer(id, workAssignReviewerDTO);
    }

    @PostMapping("/{id}/score")
    public Result<?> scoreWork(@PathVariable Long id, @RequestBody ScoreDTO scoreDTO) {
        return platformService.scoreWork(id, scoreDTO);
    }
}
