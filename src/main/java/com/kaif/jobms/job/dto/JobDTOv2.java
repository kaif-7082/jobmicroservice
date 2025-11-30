package com.kaif.jobms.job.dto;

import com.kaif.jobms.job.external.Company;
import lombok.Data;

@Data
public class JobDTOv2 {
    private Long id;
    private String title;
    private String description;
    private String location;
    private Company company;
    private Compensation compensation;
}