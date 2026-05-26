package com.howprom.submission.dto;

//
//목록 조회용 경량 DTO (MyPageListDto.java)
//마이페이지 메인 테이블에 뿌려질 5개 필드 위주의 가벼운 DTO입니다. conversation JSON 데이터는 제외하여 성능을 최적화합니다

import com.howprom.common.entity.EvaluationType;
import com.howprom.common.entity.SubmissionStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // JPQL 'new' 키워드 매핑을 위해 필수
public class MyPageListDto {
    private Long submissionId;
    private Long problemId;
    private String problemTitle;
    private EvaluationType evaluationType;
    private Integer score;
    private SubmissionStatus status;
    private Integer totalUserTokens;
    private LocalDateTime createdAt;
}