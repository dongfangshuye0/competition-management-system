package com.tangqilin.competition.controller;

import com.tangqilin.competition.common.result.Result;
import com.tangqilin.competition.entity.dto.CompetitionDTO;
import com.tangqilin.competition.service.PlatformService;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompetitionController {

    private final PlatformService platformService;

    public CompetitionController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/api/public/competitions")
    public Result<?> publicCompetitions() {
        return platformService.publicCompetitions();
    }

    @GetMapping("/api/public/competitions/{id}")
    public Result<?> publicCompetitionDetail(@PathVariable Long id) {
        return platformService.publicCompetitionDetail(id);
    }

    @GetMapping("/api/public/competitions/{id}/ranking")
    public Result<?> publicCompetitionRanking(@PathVariable Long id) {
        return platformService.competitionRanking(id);
    }

    @GetMapping("/api/competitions/recommended")
    public Result<?> competitionRecommendations() {
        return platformService.competitionRecommendations();
    }

    @GetMapping("/api/competitions/{id}/ranking")
    public Result<?> competitionRanking(@PathVariable Long id) {
        return platformService.competitionRanking(id);
    }

    @PostMapping("/api/competitions")
    public Result<?> createCompetition(@RequestBody CompetitionDTO competitionDTO) {
        return platformService.saveCompetition(null, competitionDTO);
    }

    @PutMapping("/api/competitions/{id}")
    public Result<?> updateCompetition(@PathVariable Long id, @RequestBody CompetitionDTO competitionDTO) {
        return platformService.saveCompetition(id, competitionDTO);
    }

    @DeleteMapping("/api/competitions/{id}")
    public Result<?> deleteCompetition(@PathVariable Long id) {
        return platformService.deleteCompetition(id);
    }

    @PostMapping("/api/competitions/{id}/publish-ranking")
    public Result<?> publishRanking(@PathVariable Long id) {
        return platformService.publishRanking(id);
    }
}
