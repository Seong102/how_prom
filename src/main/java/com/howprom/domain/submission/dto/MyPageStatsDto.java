package com.howprom.domain.submission.dto;

//상단 요약 통계 DTO (MyPageStatsDto.java)
//마이페이지 상단 카드 레이아웃에 들어갈 통계 가공 데이터입니다.

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageStatsDto {
    private long totalSubmissions;     // 총 제출 수
    private long totalSolvedProblems;  // 풀이 완료 문제 수 (중복 제거)
    private double passRate;           // 통과율 (PASSED 건수 / 총 제출 수 * 100)
}