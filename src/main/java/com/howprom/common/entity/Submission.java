package com.howprom.common.entity;

import com.howprom.domain.submission.dto.ChatMessageDto;
import com.howprom.domain.submission.dto.RequirementResultDto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conversation", columnDefinition = "json", nullable = false)
    private List<ChatMessageDto> conversation;

    @Column(name = "final_code", columnDefinition = "TEXT", nullable = false)
    private String finalCode;

    @Column(name = "total_user_tokens", nullable = false)
    private Integer totalUserTokens = 0;

    @Column(name = "completion_tokens", nullable = false)
    private Integer completionTokens = 0;

    @Column(nullable = false)
    private Integer score = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirements_result", columnDefinition = "json")
    private List<RequirementResultDto> requirementsResult;

    @Column(nullable = false)
    private String status = "GRADING"; // GRADING, PASSED, FAILED, ERROR

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;

    @PrePersist
    protected void onSubmit() {
        this.submittedAt = LocalDateTime.now();
    }
}
