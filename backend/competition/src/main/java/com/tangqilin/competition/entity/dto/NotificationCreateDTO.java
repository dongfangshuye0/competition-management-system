package com.tangqilin.competition.entity.dto;

import lombok.Data;

@Data
public class NotificationCreateDTO {
    private String title;
    private String content;
    private Long userId;
    private Boolean broadcast;
}
