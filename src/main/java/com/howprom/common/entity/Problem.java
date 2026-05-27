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

    // 1, 2, 3번 수정: name 속성 전부 제거 → Hibernate 자동 변환에 맡김
    @Column(columnDefinition = "TEXT")
    private String exampleInput;        // → example_input

    @Column(columnDefinition = "TEXT")
    private String exampleOutput;       // → example_output

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvaluationType evaluationType;  // → evaluation_type

    private Integer tokenLimit;             // → token_limit

    @Column(nullable = false)
    @Builder.Default
    private Float correctnessWeight = 0.7f; // → correctness_weight

    @Column(nullable = false)
    @Builder.Default
    private Float efficiencyWeight = 0.3f;  // → efficiency_weight

    @Column(nullable = false)
    @Builder.Default
    private Float avgUserTokens = 0.0f;     // → avg_user_tokens

    // 4번 수정: Boolean → boolean (Lombok이 isPublic() getter 생성)
    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = false;       // → is_public

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;        // → created_at

    @Column(nullable = false)
    private LocalDateTime updatedAt;        // → updated_at

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