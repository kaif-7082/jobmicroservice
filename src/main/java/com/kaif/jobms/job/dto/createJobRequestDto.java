package com.kaif.jobms.job.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class createJobRequestDto {

    @NotEmpty(message = "Title cannot be empty")
    private String title;

    @NotEmpty(message = "Description cannot be empty")
    private String description;

    private Integer minSalary;
    private Integer maxSalary;

    @NotEmpty(message = "Location cannot be empty")
    private String location;

    @NotNull(message = "companyId cannot be null")
    private Long companyId;
}