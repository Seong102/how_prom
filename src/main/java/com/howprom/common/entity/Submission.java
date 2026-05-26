package com.howprom.common.entity;

import com.howprom.submission.dto.ChatMessageDto;
import com.howprom.submission.dto.RequirementResultDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "submissions")
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

    // Hibernate 6.x 이상에서 JSON 자동 매핑
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conversation", columnDefinition = "json", nullable = false)
    private List<ChatMessageDto> conversation;

    @Column(name = "final_code", columnDefinition = "TEXT", nullable = false)
    private String finalCode;

    @Column(name = "total_user_tokens", nullable = false)
    @Builder.Default
    private Integer totalUserTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    @Builder.Default
    private Integer completionTokens = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirements_result", columnDefinition = "json")
    private List<RequirementResultDto> requirementsResult;

    // 타입 안정성을 위해 Enum 사용
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.GRADING;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    // 엔티티 저장 전 자동으로 시간 설정
    @PrePersist
    protected void onSubmit() {
        this.submittedAt = LocalDateTime.now();
    }
}