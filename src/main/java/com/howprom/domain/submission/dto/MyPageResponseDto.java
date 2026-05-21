package com.howprom.domain.submission.dto;

//메인 화면 통합 응답 DTO (MyPageResponseDto.java)
//상단 통계와 하단 페이징 목록을 한 번에 묶어서 프론트엔드로 넘겨주는 최종 반환 객체입니다.


import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class MyPageResponseDto {
    private MyPageStatsDto stats;
    private Page<MyPageListDto> submissions;
}