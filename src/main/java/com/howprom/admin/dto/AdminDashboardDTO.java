package com.howprom.admin.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
public class AdminDashboardDTO {

    // 1. 상단 요약 카드용 데이터
    private Long totalSubmissions;      
    private String totalPassRate;       // 💡 "85.3%" 형태로 백엔드가 직접 제공
    private Long activeUserCount;       
    private Long publicProblemCount;    

    // 2. 하단 리스트 데이터
    private List<ProblemTableDTO> problemList;
    private List<EfficiencyChartDTO> efficiencyList;
    private List<RequirementAnalysisDTO> requirementList;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProblemTableDTO {
        private Long id;
        private String title;
        private String totalCount;      // 💡 "15건"
        private String avgScore;        // 💡 "82.5점" 또는 "-"
        private String passedCount;     // 💡 "10건" 또는 "-"
        private String failedCount;     // 💡 "3건" 또는 "-"
        private String errorCount;      // 💡 "2건" 또는 "-"
        private boolean hasSubmissions; // 💡 데이터가 없는 문제 구별용 플래그
        private boolean hasErrors;      // 💡 에러 건수가 있는지 구별용 플래그
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EfficiencyChartDTO {
        private Long id;
        private String title;
        private String avgUserTokens;   // 💡 "1,240"
        private String barWidth;
        private String color;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RequirementAnalysisDTO {
        private String probId;
        private String title;
        private String description;
        private String avgAchieve;
        private String failRate;
        private String color;
    }
}