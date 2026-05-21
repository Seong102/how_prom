package com.howprom.domain.submission.entity;

// 1. 올바른 프로젝트 내 도메인 엔티티 임포트 (팀원들의 폴더 구조에 맞게 매핑)
import com.howprom.common.entity.User;       // 👈 스프링 내부 객체가 아닌 실제 유저 엔티티
import com.howprom.domain.problem.entity.Problem; // 👈 스프링 내부 객체가 아닌 실제 문제 엔티티

// 2. DTO 구조체 임포트 (JSON 직렬화용)
import com.howprom.domain.submission.dto.ChatMessageDto;
import com.howprom.domain.submission.dto.RequirementResultDto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //  올바른 엔티티 타입으로 수정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem; //  올바른 엔티티 타입으로 수정

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conversation", columnDefinition = "json")
    private List<ChatMessageDto> conversation;

    @Column(name = "final_code", columnDefinition = "TEXT")
    private String finalCode;

    private Integer totalUserTokens;
    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback; // 👈 LLM #2가 생성한 최종 피드백 문장 저장용 필드 추가

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirements_result", columnDefinition = "json")
    private List<RequirementResultDto> requirementsResult;

    private String status; // PASSED, FAILED, ERROR
    private LocalDateTime createdAt;
}