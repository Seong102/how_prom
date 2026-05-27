package com.howprom.admin.service;

import com.howprom.admin.dto.AdminDashboardDTO;
import com.howprom.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private static final String[] EXTENDED_COLORS = {
            "#4A90E2", "#76A1EF", "#79C3C4", "#2ECC71",
            "#E28743", "#E74C3C", "#9B59B6", "#1ABC9C",
            "#F1C40F", "#34495E", "#E67E22", "#3498DB"
    };

    public AdminDashboardDTO getDashboardStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        Long totalSubmissions = adminDashboardRepository.countTotalSubmissions();
        Long todaySubmissions = adminDashboardRepository.countTodaySubmissions(startOfToday);
        Long todayJoinCount = adminDashboardRepository.countTodayJoinCount(startOfToday);
        Long gradingCount = adminDashboardRepository.countGradingCount();
        Long totalUserCount = adminDashboardRepository.countTotalUserCount();
        Double passRate = adminDashboardRepository.calculateTotalPassRate();
        Long activeUsers = adminDashboardRepository.countActiveUsers();
        Long publicProblems = adminDashboardRepository.countPublicProblems();
        Long totalProblems = adminDashboardRepository.countTotalProblems();
        Double avgGradingTime = adminDashboardRepository.calculateAvgGradingTime();
        Long monthTokenUsage = adminDashboardRepository.calculateMonthTokenUsage(oneMonthAgo);

        String formattedPassRate = passRate != null ? String.format("%.1f%%", passRate) : "0.0%";

        // 문제별 통계
        List<Object[]> rawProblems = adminDashboardRepository.findProblemTableStatsRaw();
        List<AdminDashboardDTO.ProblemTableDTO> problemList = new ArrayList<>();

        for (Object[] row : rawProblems) {
            long totalCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            double avgScore = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            long passed = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            long failed = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            long error = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            boolean hasSub = totalCount > 0;

            problemList.add(AdminDashboardDTO.ProblemTableDTO.builder()
                    .id(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .title(row[1] != null ? (String) row[1] : "-")
                    .evaluationType(row[2] != null ? row[2].toString() : "")
                    .totalCount(totalCount + "건")
                    .avgScore(hasSub ? String.format("%.1f점", avgScore) : "-")
                    .passedCount(hasSub ? passed + "건" : "-")
                    .failedCount(hasSub ? failed + "건" : "-")
                    .errorCount(hasSub ? (error > 0 ? error + "건" : "-") : "-")
                    .hasSubmissions(hasSub)
                    .hasErrors(error > 0)
                    .build());
        }

        // 2번 수정: 토큰 차트는 별도 쿼리로 분리
        List<AdminDashboardDTO.EfficiencyChartDTO> efficiencyList = getEfficiencyTokensList();

        // 요구사항 분석
     // 요구사항 분석 로직 수정
        List<AdminDashboardDTO.RequirementAnalysisDTO> requirementList = new ArrayList<>();
        List<Object[]> rawReqs = adminDashboardRepository.findRequirementStatsRaw();

        String hardestProblemTitle = "-";
        String hardestProblemPassRate = "0.0";
        String easiestProblemTitle = "-";
        String easiestProblemPassRate = "0.0";

        if (rawReqs != null && !rawReqs.isEmpty()) {
            // 1. 통계 데이터를 정렬 (실패율 낮은 순 -> 높은 순)
            rawReqs.sort((a, b) -> {
                double failA = a[5] != null ? ((Number) a[5]).doubleValue() : 0.0;
                double failB = b[5] != null ? ((Number) b[5]).doubleValue() : 0.0;
                return Double.compare(failA, failB);
            });

            // 2. 가장 쉬운 문제 (정렬된 데이터 중 첫 번째)
            Object[] easiest = rawReqs.get(0);
            double minFail = easiest[5] != null ? ((Number) easiest[5]).doubleValue() : 0.0;
            easiestProblemTitle = easiest[1] != null ? (String) easiest[1] : "-";
            easiestProblemPassRate = String.format("%.1f", Math.max(0, 100.0 - minFail));

            // 3. 가장 어려운 문제 (정렬된 데이터 중 마지막)
            Object[] hardest = rawReqs.get(rawReqs.size() - 1);
            double maxFail = hardest[5] != null ? ((Number) hardest[5]).doubleValue() : 0.0;
            hardestProblemTitle = hardest[1] != null ? (String) hardest[1] : "-";
            hardestProblemPassRate = String.format("%.1f", Math.max(0, 100.0 - maxFail));

            // 4. 리스트 생성
            int colorIdx = 0;
            for (Object[] row : rawReqs) {
                String probId = row[0] != null ? "#" + row[0].toString() : "#0";
                String title = row[1] != null ? (String) row[1] : "알 수 없는 문제";
                String description = row[2] != null ? (String) row[2] : "요구사항 명세가 없습니다.";
                Integer weight = row[3] != null ? ((Number) row[3]).intValue() : 0;
                double avgAchieve = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                double failRate = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;

                requirementList.add(AdminDashboardDTO.RequirementAnalysisDTO.builder()
                        .probId(probId).title(title).description(description)
                        .weight(weight)
                        .avgAchieve(String.format("%.1f%%", avgAchieve))
                        .failRate(String.format("%.1f%%", failRate))
                        .color(EXTENDED_COLORS[colorIdx++ % EXTENDED_COLORS.length])
                        .build());
            }
        }

        List<AdminDashboardDTO.RecentSubmissionDTO> recentSubmissionList = getRecentSubmissions();
        List<AdminDashboardDTO.TopScoreDTO> topScoreList = getTopScores();

        return AdminDashboardDTO.builder()
                .totalSubmissions(totalSubmissions != null ? totalSubmissions : 0L)
                .todaySubmissions(todaySubmissions != null ? todaySubmissions : 0L)
                .todayJoinCount(todayJoinCount != null ? todayJoinCount : 0L)
                .gradingCount(gradingCount != null ? gradingCount : 0L)
                .totalUserCount(totalUserCount != null ? totalUserCount : 0L)
                .totalPassRate(formattedPassRate)
                .activeUserCount(activeUsers != null ? activeUsers : 0L)
                .publicProblemCount(publicProblems != null ? publicProblems : 0L)
                .totalProblemCount(totalProblems != null ? totalProblems : 0L)
                .avgGradingTime(avgGradingTime != null ? avgGradingTime : 0.0)
                .monthTokenUsage(monthTokenUsage != null ? monthTokenUsage : 0L)
                .hardestProblemTitle(hardestProblemTitle)
                .hardestProblemPassRate(hardestProblemPassRate)
                .easiestProblemTitle(easiestProblemTitle)
                .easiestProblemPassRate(easiestProblemPassRate)
                .problemList(problemList)
                .efficiencyList(efficiencyList)
                .requirementList(requirementList)
                .recentSubmissionList(recentSubmissionList)
                .topScoreList(topScoreList)
                .build();
    }

    // 2번 수정: rawProblems 재활용 제거, 전용 쿼리 사용
    private List<AdminDashboardDTO.EfficiencyChartDTO> getEfficiencyTokensList() {
        List<Object[]> rawStats = adminDashboardRepository.findAllTokenStatsRaw();
        List<AdminDashboardDTO.EfficiencyChartDTO> result = new ArrayList<>();
        if (rawStats == null || rawStats.isEmpty()) return result;

        // row[0]:id, row[1]:title, row[2]:evaluationType, row[3]:avgTokens
        double maxTokens = rawStats.stream()
                .mapToDouble(row -> row[3] != null ? ((Number) row[3]).doubleValue() : 0.0)
                .max().orElse(0.0);

        int colorIdx = 0;
        for (Object[] row : rawStats) {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            String title = row[1] != null ? (String) row[1] : "-";
            String evalType = row[2] != null ? row[2].toString() : "STANDARD";
            Double avgUserTokens = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            String barWidth = maxTokens > 0 ? (int)((avgUserTokens / maxTokens) * 100) + "%" : "0%";
            String color = EXTENDED_COLORS[colorIdx % EXTENDED_COLORS.length];
            colorIdx++;

            result.add(AdminDashboardDTO.EfficiencyChartDTO.builder()
                    .id(id)
                    .title(title)
                    .avgUserTokens(String.format("%,d", avgUserTokens.intValue()))
                    .barWidth(barWidth)
                    .color(color)
                    .evaluationType(evalType)
                    .build());
        }
        return result;
    }

    private List<AdminDashboardDTO.RecentSubmissionDTO> getRecentSubmissions() {
        // row[0]:problemId, row[1]:nickname, row[2]:title, row[3]:score, row[4]:status, row[5]:submittedAt
        List<Object[]> raw = adminDashboardRepository.findRecentSubmissions(PageRequest.of(0, 10));
        List<AdminDashboardDTO.RecentSubmissionDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            Long problemId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String nickname = row[1] != null ? (String) row[1] : "-";
            String problemTitle = row[2] != null ? (String) row[2] : "-";
            Integer score = row[3] != null ? ((Number) row[3]).intValue() : 0;
            String status = row[4] != null ? row[4].toString() : "-";
            String submittedAt = toFormattedString(row[5]);

            result.add(AdminDashboardDTO.RecentSubmissionDTO.builder()
                    .problemId(problemId)
                    .nickname(nickname)
                    .problemTitle(problemTitle)
                    .score(score)
                    .status(status)
                    .submittedAt(submittedAt)
                    .build());
        }
        return result;
    }

    private List<AdminDashboardDTO.TopScoreDTO> getTopScores() {
        // row[0]:problemId, row[1]:title, row[2]:nickname, row[3]:score, row[4]:totalUserTokens, row[5]:submittedAt
        List<Object[]> raw = adminDashboardRepository.findTopScorePerProblem();
        List<AdminDashboardDTO.TopScoreDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            Long problemId = row[0] != null ? ((Number) row[0]).longValue() : 0L;
            String problemTitle = row[1] != null ? (String) row[1] : "-";
            String nickname = row[2] != null ? (String) row[2] : "-";
            Integer score = row[3] != null ? ((Number) row[3]).intValue() : 0;
            Integer totalUserTokens = row[4] != null ? ((Number) row[4]).intValue() : 0;
            String submittedAt = toFormattedString(row[5]);

            result.add(AdminDashboardDTO.TopScoreDTO.builder()
                    .problemId(problemId)
                    .problemTitle(problemTitle)
                    .nickname(nickname)
                    .score(score)
                    .totalUserTokens(totalUserTokens)
                    .submittedAt(submittedAt)
                    .build());
        }
        return result;
    }

    private String toFormattedString(Object dateObj) {
        if (dateObj == null) return "-";
        if (dateObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateObj).format(FORMATTER);
        }
        if (dateObj instanceof Timestamp) {
            return ((Timestamp) dateObj).toLocalDateTime().format(FORMATTER);
        }
        return dateObj.toString();
    }
}