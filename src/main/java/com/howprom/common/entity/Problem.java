package com.howprom.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "evaluation_type", nullable = false)
    private String evaluationType; // STANDARD, EFFICIENCY, BUDGET

    @Column(name = "token_limit")
    private Integer tokenLimit; // BUDGET 모드용 상한선

    @Column(name = "correctness_weight", nullable = false)
    @Builder.Default
    private Float correctnessWeight = 0.7f;

    @Column(name = "efficiency_weight", nullable = false)
    @Builder.Default
    private Float efficiencyWeight = 0.3f;

    @Column(name = "avg_user_tokens", nullable = false)
    @Builder.Default
    private Float avgUserTokens = 0.0f; // 🔄 [명칭 변경] avg_prompt_tokens -> avg_user_tokens

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // 🔗 [추가] 출제자 외래키 연관관계 매핑

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
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