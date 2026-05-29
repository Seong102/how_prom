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
import java.util.Set;
import java.util.HashSet;

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

        List<Object[]> rawProblems = adminDashboardRepository.findProblemTableStatsRaw();
        List<AdminDashboardDTO.ProblemTableDTO> problemList = new ArrayList<>();
        
        // 🌟 [요구사항 3] 제출 이력이 존재하는 문제의 ID를 담을 Set 선언
        Set<Long> solvedProblemIds = new HashSet<>();

        String hardestProblemTitle = "-";
        String hardestProblemPassRate = "-";
        String easiestProblemTitle = "-";
        String easiestProblemPassRate = "-";

        double maxPassRate = -1.0;
        double minPassRate = 101.0;

        for (Object[] row : rawProblems) {
            long totalCount = row[3] != null ? ((Number) row[3]).longValue() : 0L;
            double avgScore = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            long passed = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            long failed = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            long error = row[7] != null ? ((Number) row[7]).longValue() : 0L;
            boolean hasSub = totalCount > 0;

            // 제출 건수가 1건 이상 존재하는 문제 ID 저장
            if (hasSub && row[0] != null) {
                solvedProblemIds.add(((Number) row[0]).longValue());
            }

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

            if (hasSub && passed > 0) {
                String title = row[1] != null ? (String) row[1] : "-";
                double currentPassRate = (passed * 100.0) / totalCount;

                if (currentPassRate > maxPassRate) {
                    maxPassRate = currentPassRate;
                    easiestProblemTitle = title;
                    easiestProblemPassRate = String.format("%.1f", currentPassRate);
                }
                if (currentPassRate < minPassRate) {
                    minPassRate = currentPassRate;
                    hardestProblemTitle = title;
                    hardestProblemPassRate = String.format("%.1f", currentPassRate);
                }
            }
        }

        List<AdminDashboardDTO.EfficiencyChartDTO> efficiencyList = getEfficiencyTokensList();

        List<AdminDashboardDTO.RequirementAnalysisDTO> requirementList = new ArrayList<>();
        List<Object[]> rawReqs = adminDashboardRepository.findRequirementStatsRaw();

        if (rawReqs != null && !rawReqs.isEmpty()) {
            int colorIdx = 0;
            for (Object[] row : rawReqs) {
                Long probIdLong = row[1] != null ? ((Number) row[1]).longValue() : null;
                
                // 🌟 [요구사항 3] 아무도 푼 적이 없는 문제(제출 건수 0)의 요구사항은 화면에 뿌리지 않고 제외(Skip)
                if (probIdLong == null || !solvedProblemIds.contains(probIdLong)) {
                    continue;
                }

                String probId = "#" + probIdLong;
                String title = row[2] != null ? (String) row[2] : "알 수 없는 문제";
                String description = row[3] != null ? (String) row[3] : "요구사항 명세가 없습니다.";
                Integer weight = row[4] != null ? ((Number) row[4]).intValue() : 0;
                double avgAchieve = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
                double avgDeduction = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;

                requirementList.add(AdminDashboardDTO.RequirementAnalysisDTO.builder()
                        .probId(probId)
                        .title(title)
                        .description(description)
                        .weight(weight)
                        .avgAchieve(String.format("%.1f%%", avgAchieve))
                        .failRate(String.format("%.1f점", avgDeduction))
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

    private List<AdminDashboardDTO.EfficiencyChartDTO> getEfficiencyTokensList() {
        List<Object[]> rawStats = adminDashboardRepository.findAllTokenStatsRaw();
        List<AdminDashboardDTO.EfficiencyChartDTO> result = new ArrayList<>();
        if (rawStats == null || rawStats.isEmpty()) return result;

        double maxTokens = rawStats.stream()
                .mapToDouble(row -> row[3] != null ? ((Number) row[3]).doubleValue() : 0.0)
                .max().orElse(0.0);

        int colorIdx = 0;
        for (Object[] row : rawStats) {
            Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
            String title = row[1] != null ? (String) row[1] : "-";
            String evalType = row[2] != null ? row[2].toString() : "STANDARD";
            Double avgUserTokens = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            if (avgUserTokens <= 0) continue;

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
        List<Object[]> raw = adminDashboardRepository.findRecentSubmissions(PageRequest.of(0, 10));
        List<AdminDashboardDTO.RecentSubmissionDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            Long problemId = row[0] != null ? ((Number) row[0]).longValue() : null; // 🌟 이미 정상 매핑 중
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