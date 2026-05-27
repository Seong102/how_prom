package com.howprom.code.controller;

import com.howprom.code.dto.CodeRunRequest;
import com.howprom.code.dto.CodeRunResponse;
import com.howprom.code.service.CodeRunService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/code")
@RequiredArgsConstructor
public class CodeRunController {

    private final CodeRunService codeRunService;

    @PostMapping("/run")
    public ResponseEntity<?> run(@RequestBody CodeRunRequest request) {
        try {
            CodeRunResponse response = codeRunService.run(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("코드 실행 중 서버 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}