package com.howprom.admin.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter 
@Builder
@NoArgsConstructor  // 기본 생성자 보장
@AllArgsConstructor // 빌더 패턴 연동을 위한 전체 생성자 유지
public class ProblemStatsDTO {
    
    private Long problemId;      
    private String title;        
    private Long totalCount;     
    private Double avgScore;     
    private Long passedCount;    
    private Long failedCount;    
    private Long errorCount;     
    private boolean hasSubmissions; 
    private boolean hasErrors;

    /**
     * 🔥 [추가] 통과율(Pass Rate) 안전 계산용 동적 Getter
     * 타임리프 화면에서 ${statsMap[prob.id].passRate}로 바로 호출할 수 있게 해줍니다.
     */
    public double getPassRate() {
        if (totalCount == null || totalCount == 0 || passedCount == null) {
            return 0.0;
        }
        return ((double) passedCount / totalCount) * 100.0;
    }

    /**
     * 🔥 [핵심] JPQL Repository의 'SELECT new' 쿼리가 요구하는 4개짜리 전용 생성자!
     * (s.problem.id, COUNT(s.id), AVG(s.score), SUM(...)) 순서와 타입을 그대로 일치시킵니다.
     */
    public ProblemStatsDTO(Long problemId, Long totalCount, Double avgScore, Long passedCount) {
        this.problemId = problemId;
        this.totalCount = totalCount;
        this.avgScore = avgScore;
        this.passedCount = passedCount != null ? passedCount : 0L;
    }

    /**
     * 타임리프의 ${prob.id} 호출을 우회 처리하기 위한 포워딩 메서드
     */
    public Long getId() {
        return this.problemId;
    }
}