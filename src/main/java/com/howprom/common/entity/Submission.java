package com.howprom.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, columnDefinition = "JSON")
    private String conversation;

    @Column(name = "final_code", nullable = false, columnDefinition = "TEXT")
    private String finalCode;

    // 💡 [핵심] 컬럼명이 명확히 지정되어 있으므로 아래 변수 하나만 존재해야 합니다.
    @Column(name = "total_user_tokens", nullable = false)
    @Builder.Default
    private Integer totalUserTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private Integer completionTokens = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    @Column(name = "requirements_result", columnDefinition = "JSON")
    private String requirementsResult;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.GRADING;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    public enum SubmissionStatus {
        GRADING, PASSED, FAILED, ERROR
    }
}