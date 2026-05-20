package com.howprom.domain.problem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 마이페이지 목록 및 상세 조회 시 표기용 [cite: 141, 181]

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "evaluation_type")
    private String evaluationType; // STANDARD, EFFICIENCY, BUDGET [cite: 141]

    @Column(name = "token_limit")
    private Integer tokenLimit; // BUDGET 모드용 상한선 [cite: 149]

    @Column(name = "avg_user_tokens")
    private Double avgUserTokens; // EFFICIENCY 모드 기준값 [cite: 207]

    @Column(name = "is_public")
    private Boolean isPublic;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}