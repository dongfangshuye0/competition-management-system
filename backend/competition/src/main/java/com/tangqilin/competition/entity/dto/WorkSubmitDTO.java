package com.tangqilin.competition.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class WorkSubmitDTO {
    private Long registrationId;
    private String title;
    private String description;
    private MultipartFile file;
}
