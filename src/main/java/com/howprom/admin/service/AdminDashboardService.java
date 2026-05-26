package com.howprom.admin.service;

import com.howprom.admin.dto.AdminDashboardDTO;
import com.howprom.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public AdminDashboardDTO getDashboardStats() {

        Long totalSubmissions = adminDashboardRepository.countTotalSubmissions();
        Double passRate = adminDashboardRepository.calculateTotalPassRate();
        Long activeUsers = adminDashboardRepository.countActiveUsers();
        Long publicProblems = adminDashboardRepository.countPublicProblems();
        Long totalProblems = adminDashboardRepository.countTotalProblems();

        String formattedPassRate = passRate != null ? String.format("%.1f%%", passRate) : "0.0%";

        // 2번: evaluationType 포함해서 조립
        List<Object[]> rawProblems = adminDashboardRepository.findProblemTableStatsRaw();
        List<AdminDashboardDTO.ProblemTableDTO> problemList = new ArrayList<>();

        for (Object[] row : rawProblems) {
            long totalCount = row[3] != null ? (Long) row[3] : 0L;
            double avgScore = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            long passed = row[5] != null ? (Long) row[5] : 0L;
            long failed = row[6] != null ? (Long) row[6] : 0L;
            long error = row[7] != null ? (Long) row[7] : 0L;
            boolean hasSub = totalCount > 0;

            problemList.add(AdminDashboardDTO.ProblemTableDTO.builder()
                    .id((Long) row[0])
                    .title((String) row[1])
                    .evaluationType(row[2] != null ? row[2].toString() : "")
                    .totalCount(totalCount + "건")
                    .avgScore(hasSub ? String.format("%.1f점", avgScore) : "-")
                    .passedCount(hasSub ? passed + "건" : "-")
                    .failedCount(hasSub ? failed + "건" : "-")
                    .errorCount(hasSub ? error + "건" : "-")
                    .hasSubmissions(hasSub)
                    .hasErrors(error > 0)
                    .build());
        }

        List<AdminDashboardDTO.EfficiencyChartDTO> efficiencyList = getEfficiencyTokensList();
        
        // [수정 완료] RequirementListDTO 오타를 원래 타입인 RequirementAnalysisDTO로 복구했습니다.
        List<AdminDashboardDTO.RequirementAnalysisDTO> requirementList = getRequirementAnalysisList();

        // 4번: 최근 제출 현황
        List<AdminDashboardDTO.RecentSubmissionDTO> recentSubmissionList = getRecentSubmissions();

        // 5번: 최고 점수 랭킹 (방안 A 중복 제거 로직 유지)
        List<AdminDashboardDTO.TopScoreDTO> topScoreList = getTopScores();

        return AdminDashboardDTO.builder()
                .totalSubmissions(totalSubmissions != null ? totalSubmissions : 0L)
                .totalPassRate(formattedPassRate)
                .activeUserCount(activeUsers != null ? activeUsers : 0L)
                .publicProblemCount(publicProblems != null ? publicProblems : 0L)
                .totalProblemCount(totalProblems != null ? totalProblems : 0L)
                .problemList(problemList)
                .efficiencyList(efficiencyList)
                .requirementList(requirementList)
                .recentSubmissionList(recentSubmissionList)
                .topScoreList(topScoreList)
                .build();
    }

    private List<AdminDashboardDTO.EfficiencyChartDTO> getEfficiencyTokensList() {
        List<Object[]> rawStats = adminDashboardRepository.findEfficiencyTokenStats();
        List<AdminDashboardDTO.EfficiencyChartDTO> result = new ArrayList<>();
        if (rawStats.isEmpty()) return result;

        double maxTokens = rawStats.stream()
                .mapToDouble(row -> ((Number) row[2]).doubleValue())
                .max().orElse(0.0);

        String[] colors = {"#E28743", "#76A1EF", "#79C3C4", "#4A90E2"};
        int colorIdx = 0;

        for (Object[] row : rawStats) {
            Long id = (Long) row[0];
            String title = (String) row[1];
            Double avgUserTokens = ((Number) row[2]).doubleValue();
            String barWidth = maxTokens > 0 ? (int)((avgUserTokens / maxTokens) * 100) + "%" : "0%";
            String color = colors[colorIdx % colors.length];
            colorIdx++;

            result.add(AdminDashboardDTO.EfficiencyChartDTO.builder()
                    .id(id)
                    .title(title)
                    .avgUserTokens(String.format("%,d", avgUserTokens.intValue()))
                    .barWidth(barWidth)
                    .color(color)
                    .build());
        }
        return result;
    }

    private List<AdminDashboardDTO.RequirementAnalysisDTO> getRequirementAnalysisList() {
        List<AdminDashboardDTO.RequirementAnalysisDTO> result = new ArrayList<>();
        List<Object[]> rawReqs = adminDashboardRepository.findRequirementStatsRaw();
        if (rawReqs == null || rawReqs.isEmpty()) return result;

        String[] colors = {"#4A90E2", "#79C3C4", "#76A1EF", "#E28743"};
        int colorIdx = 0;

        for (Object[] row : rawReqs) {
            String probId = row[0] != null ? "#" + row[0].toString() : "#0";
            String title = row[1] != null ? (String) row[1] : "알 수 없는 문제";
            String description = row[2] != null ? (String) row[2] : "요구사항 명세가 없습니다.";
            Integer weight = row[3] != null ? ((Number) row[3]).intValue() : 0;
            double avgAchieve = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            double failRate = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
            String color = colors[colorIdx % colors.length];
            colorIdx++;

            result.add(AdminDashboardDTO.RequirementAnalysisDTO.builder()
                    .probId(probId)
                    .title(title)
                    .description(description)
                    .weight(weight)
                    .avgAchieve(String.format("%.1f%%", avgAchieve))
                    .failRate(String.format("%.1f%%", failRate))
                    .color(color)
                    .build());
        }
        return result;
    }

    private List<AdminDashboardDTO.RecentSubmissionDTO> getRecentSubmissions() {
        List<Object[]> raw = adminDashboardRepository.findRecentSubmissions();
        List<AdminDashboardDTO.RecentSubmissionDTO> result = new ArrayList<>();

        int limit = Math.min(raw.size(), 10);
        for (int i = 0; i < limit; i++) {
            Object[] row = raw.get(i);
            String nickname = row[0] != null ? (String) row[0] : "-";
            String problemTitle = row[1] != null ? (String) row[1] : "-";
            Integer score = row[2] != null ? ((Number) row[2]).intValue() : 0;
            String status = row[3] != null ? row[3].toString() : "-";
            String submittedAt = row[4] != null
                    ? ((LocalDateTime) row[4]).format(FORMATTER) : "-";

            result.add(AdminDashboardDTO.RecentSubmissionDTO.builder()
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
        Map<Long, AdminDashboardDTO.TopScoreDTO> topScoreMap = new LinkedHashMap<>();

        for (Object[] row : raw) {
            Long problemId = row[0] != null ? (Long) row[0] : 0L;
            String problemTitle = row[1] != null ? (String) row[1] : "-";
            String nickname = row[2] != null ? (String) row[2] : "-";
            Integer score = row[3] != null ? ((Number) row[3]).intValue() : 0;
            Integer totalUserTokens = row[4] != null ? ((Number) row[4]).intValue() : 0;
            String submittedAt = row[5] != null ? ((LocalDateTime) row[5]).format(FORMATTER) : "-";

            AdminDashboardDTO.TopScoreDTO current = AdminDashboardDTO.TopScoreDTO.builder()
                    .problemId(problemId)
                    .problemTitle(problemTitle)
                    .nickname(nickname)
                    .score(score)
                    .totalUserTokens(totalUserTokens)
                    .submittedAt(submittedAt)
                    .build();

            if (!topScoreMap.containsKey(problemId)) {
                topScoreMap.put(problemId, current);
            } else {
                AdminDashboardDTO.TopScoreDTO existing = topScoreMap.get(problemId);
                boolean shouldReplace = false;

                if (current.getScore() > existing.getScore()) {
                    shouldReplace = true;
                } else if (current.getScore().equals(existing.getScore())) {
                    if (current.getTotalUserTokens() < existing.getTotalUserTokens()) {
                        shouldReplace = true;
                    }
                }

                if (shouldReplace) {
                    topScoreMap.put(problemId, current);
                }
            }
        }

        return new ArrayList<>(topScoreMap.values());
    }
}