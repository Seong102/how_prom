package com.howprom.admin.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
public class AdminDashboardDTO {

    private Long totalSubmissions;
    private String totalPassRate;
    private Long activeUserCount;
    private Long publicProblemCount;
    private Long totalProblemCount;

    private List<ProblemTableDTO> problemList;
    private List<EfficiencyChartDTO> efficiencyList;
    private List<RequirementAnalysisDTO> requirementList;
    private List<RecentSubmissionDTO> recentSubmissionList;  // 4번
    private List<TopScoreDTO> topScoreList;                  // 5번

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProblemTableDTO {
        private Long id;
        private String title;
        private String evaluationType;   // 2번
        private String totalCount;
        private String avgScore;
        private String passedCount;
        private String failedCount;
        private String errorCount;
        private boolean hasSubmissions;
        private boolean hasErrors;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EfficiencyChartDTO {
        private Long id;
        private String title;
        private String avgUserTokens;
        private String barWidth;
        private String color;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RequirementAnalysisDTO {
        private String probId;
        private String title;
        private String description;
        private Integer weight;
        private String avgAchieve;
        private String failRate;
        private String color;
    }

    //최근 제출 현황
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RecentSubmissionDTO {
        private String nickname;
        private String problemTitle;
        private Integer score;
        private String status;
        private String submittedAt;
    }

    //문제별 최고 점수 랭킹
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TopScoreDTO {
        private Long problemId;
        private String problemTitle;
        private String nickname;
        private Integer score;
        private Integer totalUserTokens;
        private String submittedAt;
    }
}