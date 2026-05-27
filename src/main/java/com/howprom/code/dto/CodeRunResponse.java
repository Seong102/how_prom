package com.howprom.code.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeRunResponse {
    private String stdout;        // 정상 출력
    private String compileError;  // 컴파일 오류 (javac stderr)
    private String runtimeError;  // 런타임 오류 (java stderr)

    /* 정적 팩토리 메서드 */
    public static CodeRunResponse success(String stdout) {
        return new CodeRunResponse(stdout, null, null);
    }

    public static CodeRunResponse compileError(String error) {
        return new CodeRunResponse(null, error, null);
    }

    public static CodeRunResponse runtimeError(String error) {
        return new CodeRunResponse(null, null, error);
    }
}