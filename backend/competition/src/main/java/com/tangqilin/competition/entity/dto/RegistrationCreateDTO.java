package com.tangqilin.competition.entity.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RegistrationCreateDTO {
    private Long competitionId;
    private Long teacherId;
    private String teamName;
    private String teamMembers;
    private MultipartFile applicationForm;
}
