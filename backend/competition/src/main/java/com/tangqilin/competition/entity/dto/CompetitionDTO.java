package com.tangqilin.competition.entity.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompetitionDTO {
    private String name;
    private String description;
    private String organizer;
    private String level;
    private LocalDateTime registrationStartTime;
    private LocalDateTime registrationEndTime;
    private LocalDateTime submissionEndTime;
}
