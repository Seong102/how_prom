package com.howprom.admin.entity;

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

    @Column(nullable = false)
    private Integer difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType;

    @Column(name = "token_limit")
    private Integer tokenLimit;

    @Column(name = "correctness_weight", nullable = false)
    private Float correctnessWeight = 0.7f;

    @Column(name = "efficiency_weight", nullable = false)
    private Float efficiencyWeight = 0.3f;

    @Column(name = "avg_prompt_tokens", nullable = false)
    private Float avgPromptTokens = 0.0f;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // 1:N 관계 매핑 (Requirements와 연결)
    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Requirement> requirements = new ArrayList<>();
}