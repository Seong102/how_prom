package com.howprom.domain.submission.dto;


//2. 상세 팝업용 DTO (따로 분리하여 호출)
//유저가 마이페이지에서 특정 행을 클릭했을 때 비동기(AJAX/Fetch)로 호출되어 팝업창에 대화 흐름과 최종 코드, 피드백을 뿌려주는 DTO입니다.

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class MyPageDetailResponseDto {
    private Long submissionId;
    private String problemTitle;
    private Integer score;
    private String feedback;
    private String finalCode;
    private Integer totalUserTokens;

    // DB 명세서 및 기능설계서의 JSON 타입 컬럼 매핑용 내부 DTO 구조체
    private List<ChatMessageDto> conversation;
    private List<RequirementResultDto> requirementsResult;
}