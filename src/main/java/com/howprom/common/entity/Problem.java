package com.howprom.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.howprom.common.entity.EvaluationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "problems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "example_input", columnDefinition = "TEXT")
    private String exampleInput;

    @Column(name = "example_output", columnDefinition = "TEXT")
    private String exampleOutput;

    @Column(nullable = false)
    private Integer difficulty = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType;

    @Column(name = "token_limit")
    private Integer tokenLimit; // BUDGET 모드용 상한선

    @Column(name = "correctness_weight", nullable = false)
    private Float correctnessWeight = 0.7f;

    @Column(name = "efficiency_weight", nullable = false)
    private Float efficiencyWeight = 0.3f;

    @Column(name = "avg_prompt_tokens", nullable = false)
    private Float avgPromptTokens = 0.0f; // Admin 통계용

    //@Column(name = "avg_user_tokens")
    //private Double avgUserTokens = 0.0d; // Domain 모드 기준값

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    //연관관계 매핑 (필요 시 유지, 패키지 경로 주의)
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Requirement> requirements = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}