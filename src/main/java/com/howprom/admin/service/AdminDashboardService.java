package com.howprom.admin.service;

import com.howprom.admin.dto.AdminDashboardDTO;
import com.howprom.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    // 💡 대시보드 UI에 어울리는 풍부한 고품격 테마 색상 배열 확장 (12색)
    private static final String[] EXTENDED_COLORS = {
            "#4A90E2", "#76A1EF", "#79C3C4", "#2ECC71", 
            "#E28743", "#E74C3C", "#9B59B6", "#1ABC9C", 
            "#F1C40F", "#34495E", "#E67E22", "#3498DB"
    };

    public AdminDashboardDTO getDashboardStats() {

        Long totalSubmissions = adminDashboardRepository.countTotalSubmissions();
        Double passRate = adminDashboardRepository.calculateTotalPassRate();
        Long activeUsers = adminDashboardRepository.countActiveUsers();
        Long publicProblems = adminDashboardRepository.countPublicProblems();
        Long totalProblems = adminDashboardRepository.countTotalProblems();

        String formattedPassRate = passRate != null ? String.format("%.1f%%", passRate) : "0.0%";

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
                    .errorCount(hasSub ? error > 0 ? error + "건" : "-" : "-")
                    .hasSubmissions(hasSub)
                    .hasErrors(error > 0)
                    .build());
        }

        List<AdminDashboardDTO.EfficiencyChartDTO> efficiencyList = getEfficiencyTokensList();
        List<AdminDashboardDTO.RequirementAnalysisDTO> requirementList = getRequirementAnalysisList();
        List<AdminDashboardDTO.RecentSubmissionDTO> recentSubmissionList = getRecentSubmissions();
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
        List<Object[]> rawStats = adminDashboardRepository.findProblemTableStatsRaw();
        List<AdminDashboardDTO.EfficiencyChartDTO> result = new ArrayList<>();
        if (rawStats.isEmpty()) return result;

        // 1. 먼저 0 token 이하인 항목을 제외한 데이터만 필터링
        List<Object[]> filteredStats = rawStats.stream()
                .filter(row -> {
                    Double avgTokens = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
                    return avgTokens > 0;   // 제출 이력이 있어 평균 토큰이 1 이상인 것만 포함
                })
                .collect(Collectors.toList());

        if (filteredStats.isEmpty()) return result;

        // 2. 필터링된 데이터 기준으로 maxTokens 계산
        double maxTokens = filteredStats.stream()
                .mapToDouble(row -> row[4] != null ? ((Number) row[4]).doubleValue() : 0.0)
                .max().orElse(0.0);

        int colorIdx = 0;

        for (Object[] row : filteredStats) {
            Long id = (Long) row[0];
            String title = (String) row[1];
            Double avgUserTokens = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            String evalType = row[2] != null ? row[2].toString() : "STANDARD";

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

    private List<AdminDashboardDTO.RequirementAnalysisDTO> getRequirementAnalysisList() {
        List<AdminDashboardDTO.RequirementAnalysisDTO> result = new ArrayList<>();
        List<Object[]> rawReqs = adminDashboardRepository.findRequirementStatsRaw();
        if (rawReqs == null || rawReqs.isEmpty()) return result;

        int colorIdx = 0;

        for (Object[] row : rawReqs) {
            String probId = row[0] != null ? "#" + row[0].toString() : "#0";
            String title = row[1] != null ? (String) row[1] : "알 수 없는 문제";
            String description = row[2] != null ? (String) row[2] : "요구사항 명세가 없습니다.";
            Integer weight = row[3] != null ? ((Number) row[3]).intValue() : 0;
            double avgAchieve = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            double failRate = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
            
            // 💡 확장된 컬러 팔레트 적용
            String color = EXTENDED_COLORS[colorIdx % EXTENDED_COLORS.length];
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
        List<Object[]> raw = adminDashboardRepository.findRecentSubmissions(PageRequest.of(0, 10));
        List<AdminDashboardDTO.RecentSubmissionDTO> result = new ArrayList<>();

        for (Object[] row : raw) {
            String nickname = row[0] != null ? (String) row[0] : "-";
            String problemTitle = row[1] != null ? (String) row[1] : "-";
            Integer score = row[2] != null ? ((Number) row[2]).intValue() : 0;
            String status = row[3] != null ? row[3].toString() : "-";
            String submittedAt = toFormattedString(row[4]);

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

    /**
     * 💡 리팩토링 완료: ROW_NUMBER() 기반 랭킹 최적화 반영
     * DB Native Query 레벨에서 이미 완벽한 순위 정렬 및 문제당 단 1건 필터링이 완료되어 넘어옵니다.
     * 따라서 자바단에서 복잡하게 연산하던 LinkedHashMap 중복 제어 루프를 전부 제거했습니다.
     */
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