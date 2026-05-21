package com.howprom.problem.dto;
 
import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.Problem;
import com.howprom.common.entity.Requirement;
import lombok.Getter;
 
import java.util.List;
 
/**
 * SCR-PROB-02 문제 상세 · 풀기 화면용 DTO
 * - 채팅 UI 렌더링에 필요한 문제 정보 + 요구사항 목록
 * - 내부 관리용 필드(createdBy, avgUserTokens, isPublic 등)는 제외
 */
@Getter
public class ProblemDetailDto {
 
    private final Long id;
    private final String title;
    private final String description;
    private final String exampleInput;       // nullable - 없으면 화면에서 미표시
    private final String exampleOutput;      // nullable - 없으면 화면에서 미표시
    private final EvaluationType evaluationType;
    private final Integer tokenLimit;        // BUDGET 모드만 non-null, 나머지는 null
 
    // EFFICIENCY 모드에서 가중치 표시용 (STANDARD·BUDGET에서는 화면에서 무시)
    private final Float correctnessWeight;
    private final Float efficiencyWeight;
 
    private final List<RequirementItem> requirements;
 
    private ProblemDetailDto(Problem problem, List<Requirement> requirements) {
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.description = problem.getDescription();
        this.exampleInput = problem.getExampleInput();
        this.exampleOutput = problem.getExampleOutput();
        this.evaluationType = problem.getEvaluationType();
        this.tokenLimit = problem.getTokenLimit();
        this.correctnessWeight = problem.getCorrectnessWeight();
        this.efficiencyWeight = problem.getEfficiencyWeight();
        this.requirements = requirements.stream()
                .map(RequirementItem::from)
                .toList();
    }
 
    public static ProblemDetailDto of(Problem problem, List<Requirement> requirements) {
        return new ProblemDetailDto(problem, requirements);
    }
 
    /**
     * 요구사항 항목 - 배점(weight)은 학습 참고용으로 화면에 노출
     */
    @Getter
    public static class RequirementItem {
        private final Long id;
        private final String description;
        private final Integer weight;
        private final Integer displayOrder;
 
        private RequirementItem(Requirement requirement) {
            this.id = requirement.getId();
            this.description = requirement.getDescription();
            this.weight = requirement.getWeight();
            this.displayOrder = requirement.getDisplayOrder();
        }
 
        public static RequirementItem from(Requirement requirement) {
            return new RequirementItem(requirement);
        }
    }
}
 