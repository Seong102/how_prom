package com.howprom.admin.service;

import com.howprom.admin.dto.AdminDashboardDTO;
import com.howprom.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;

    public AdminDashboardDTO getDashboardStats() {
        // 1. 상단 카드 데이터
        Long totalSubmissions = adminDashboardRepository.countTotalSubmissions();
        Double passRate = adminDashboardRepository.calculateTotalPassRate();
        Long activeUsers = adminDashboardRepository.countActiveUsers();
        Long publicProblems = adminDashboardRepository.countPublicProblems();

        String formattedPassRate = passRate != null ? String.format("%.1f%%", passRate) : "0.0%";

        // 2. 하단 문제별 통계 조립
        List<Object[]> rawProblems = adminDashboardRepository.findProblemTableStatsRaw();
        List<AdminDashboardDTO.ProblemTableDTO> problemList = new ArrayList<>();
        
        for (Object[] row : rawProblems) {
            long totalCount = row[2] != null ? (Long) row[2] : 0L;
            double avgScore = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            long passed = row[4] != null ? (Long) row[4] : 0L;
            long failed = row[5] != null ? (Long) row[5] : 0L;
            long error = row[6] != null ? (Long) row[6] : 0L;

            boolean hasSub = totalCount > 0;

            problemList.add(AdminDashboardDTO.ProblemTableDTO.builder()
                    .id((Long) row[0])
                    .title((String) row[1])
                    .totalCount(totalCount + "건")
                    .avgScore(hasSub ? String.format("%.1f점", avgScore) : "-")
                    .passedCount(hasSub ? passed + "건" : "-")
                    .failedCount(hasSub ? failed + "건" : "-")
                    .errorCount(hasSub ? error + "건" : "-")
                    .hasSubmissions(hasSub)
                    .hasErrors(error > 0)
                    .build());
        }

        // 3. 효율성 토큰 차트 데이터 조립
        List<AdminDashboardDTO.EfficiencyChartDTO> efficiencyList = getEfficiencyTokensList();

        // 4. 요구사항 분석 데이터 조립
        List<AdminDashboardDTO.RequirementAnalysisDTO> requirementList = getRequirementAnalysisList();

        return AdminDashboardDTO.builder()
                .totalSubmissions(totalSubmissions != null ? totalSubmissions : 0L)
                .totalPassRate(formattedPassRate)
                .activeUserCount(activeUsers != null ? activeUsers : 0L)
                .publicProblemCount(publicProblems != null ? publicProblems : 0L)
                .problemList(problemList)
                .efficiencyList(efficiencyList)
                .requirementList(requirementList)
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
            // 🔥 [수정] SELECT 절 변경에 따른 인덱스 포워딩 최적화
            // row[0]: problem_id, row[1]: title, row[2]: description, row[3]: avg_achieve, row[4]: fail_rate
            String probId = row[0] != null ? "#" + row[0].toString() : "#0";
            String title = row[1] != null ? (String) row[1] : "알 수 없는 문제";
            String description = row[2] != null ? (String) row[2] : "요구사항 명세가 없습니다.";
            
            double avgAchieve = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            double failRate = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;
            
            String color = colors[colorIdx % colors.length];
            colorIdx++;

            result.add(AdminDashboardDTO.RequirementAnalysisDTO.builder()
                    .probId(probId)
                    .title(title) // 🔥 DTO 내부 정적 클래스에 추가했던 title 필드 매핑
                    .description(description)
                    .avgAchieve(String.format("%.1f%%", avgAchieve))
                    .failRate(String.format("%.1f%%", failRate))
                    .color(color)
                    .build());
        }
        return result;
    }
}