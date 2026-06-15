package com.tangqilin.competition.controller;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.AiQuestionDTO;
import com.tangqilin.competition.service.PlatformService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final PlatformService platformService;

    public AiController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @PostMapping("/qa")
    public Result<?> answerQuestion(@RequestBody AiQuestionDTO aiQuestionDTO) {
        return platformService.answerQuestion(aiQuestionDTO);
    }
}
