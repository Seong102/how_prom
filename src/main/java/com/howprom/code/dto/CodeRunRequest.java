package com.howprom.code.dto;

import lombok.Getter;

@Getter
public class CodeRunRequest {
    private String code;   // 에디터에서 받은 Java 소스코드
    private String stdin;  // 사용자 입력값 (없으면 빈 문자열)
}