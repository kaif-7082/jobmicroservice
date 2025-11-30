package com.kaif.jobms.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Compensation {
    private Integer minimum;
    private Integer maximum;
    private String currency;
    private String frequency;
}