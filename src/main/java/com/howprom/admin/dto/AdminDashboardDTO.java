package com.howprom.admin.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDTO {
    private Long totalSubmissions;
    private Long todaySubmissions;
    private Long todayJoinCount;    
    private Long gradingCount;      
    private Long totalUserCount;    
    private String totalPassRate;
    private Long activeUserCount;
    private Long publicProblemCount;
    private Long totalProblemCount;
    
    // 💡 추가 필드: 서비스 레이어 컴파일 에러 해결용
    private Double avgGradingTime;
    private Long monthTokenUsage;
    
    // 요주의 문항 (가장 어려운 문제)
    private String hardestProblemTitle; 
    private String hardestProblemPassRate; 

    // 변별력 검토 문항 (가장 쉬운 문제)
    private String easiestProblemTitle;    
    private String easiestProblemPassRate; 

    private List<ProblemTableDTO> problemList;
    private List<EfficiencyChartDTO> efficiencyList;
    private List<RequirementAnalysisDTO> requirementList;
    private List<RecentSubmissionDTO> recentSubmissionList;
    private List<TopScoreDTO> topScoreList;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemTableDTO {
        private Long id;
        private String title;
        private String evaluationType;
        private String totalCount;
        private String avgScore;
        private String passedCount;
        private String failedCount;
        private String errorCount;
        private boolean hasSubmissions;
        private boolean hasErrors;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EfficiencyChartDTO {
        private Long id;
        private String title;
        private String avgUserTokens;
        private String barWidth;
        private String color;
        private String evaluationType;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequirementAnalysisDTO {
        private String probId;
        private String title;
        private String description;
        private Integer weight;
        private String avgAchieve;
        private String failRate;
        private String color;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentSubmissionDTO {
        private Long problemId; 
        private String nickname;
        private String problemTitle;
        private Integer score;
        private String status;
        private String submittedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopScoreDTO {
        private Long problemId;
        private String problemTitle;
        private String nickname;
        private Integer score;
        private Integer totalUserTokens;
        private String submittedAt;
    }
}