package com.howprom.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResultViewDto {
    private String  problemTitle;
    private String  evaluationType;
    private Integer score;
    private String  status;
    private Integer totalUserTokens;
    private Integer turnCount;
}
