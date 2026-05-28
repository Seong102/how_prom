package com.howprom.code.service;

import com.howprom.code.dto.CodeRunRequest;
import com.howprom.code.dto.CodeRunResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CodeRunService {

    /* 실행 제한 */
    private static final int COMPILE_TIMEOUT_SEC = 10;  // 컴파일 최대 10초
    private static final int RUN_TIMEOUT_SEC      = 5;  // 실행 최대 5초
    private static final int MAX_OUTPUT_CHARS     = 10_000; // 출력 최대 10,000자

    public CodeRunResponse run(CodeRunRequest request) throws IOException {

        // 1. 임시 디렉토리 생성 (실행마다 독립된 공간)
        Path tempDir = Files.createTempDirectory("howprom_");

        try {
            // 2. 소스 파일 생성 — public class 이름 추출하여 파일명 결정
            String className = extractClassName(request.getCode());
            Path sourceFile = tempDir.resolve(className + ".java");
            Files.writeString(sourceFile, request.getCode(), StandardCharsets.UTF_8);

            // 3. 컴파일 (javac)
            String compileError = compile(tempDir, sourceFile);
            if (compileError != null) {
                return CodeRunResponse.compileError(compileError);
            }

            // 4. 실행 (java)
            return execute(tempDir, request.getStdin(), className);

        } finally {
            // 5. 임시 파일 정리
            deleteDirectory(tempDir);
        }
    }

    /* ── 컴파일 ── */
    private String compile(Path tempDir, Path sourceFile) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "javac",
                "-encoding", "UTF-8",
                sourceFile.toString()
        );
        pb.directory(tempDir.toFile());
        pb.redirectErrorStream(true); // stdout + stderr 합치기

        Process process = pb.start();
        String output = readStream(process.getInputStream());

        try {
            boolean finished = process.waitFor(COMPILE_TIMEOUT_SEC, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "컴파일 시간 초과 (" + COMPILE_TIMEOUT_SEC + "초)";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "컴파일 중단됨";
        }

        // 종료 코드 0 = 성공
        return process.exitValue() == 0 ? null : output;
    }

    /* ── 실행 ── */
    private CodeRunResponse execute(Path tempDir, String stdin, String className) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-cp", tempDir.toString(),
                "-Xmx128m",           // 메모리 128MB 제한
                "-Dfile.encoding=UTF-8",           // 한글 출력 인코딩
                "-Dstdout.encoding=UTF-8",         // stdout 인코딩
                "-Dstderr.encoding=UTF-8",         // stderr 인코딩
                className
        );
        pb.directory(tempDir.toFile());

        Process process = pb.start();

        // stdin 입력값 전달
        if (stdin != null && !stdin.isBlank()) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            process.getOutputStream().close();
        }

        // stdout / stderr 동시 읽기 (데드락 방지)
        StringBuffer stdoutBuf = new StringBuffer();
        StringBuffer stderrBuf = new StringBuffer();

        Thread stdoutThread = new Thread(() -> {
            try { stdoutBuf.append(readStream(process.getInputStream())); }
            catch (IOException ignored) {}
        });
        Thread stderrThread = new Thread(() -> {
            try { stderrBuf.append(readStream(process.getErrorStream())); }
            catch (IOException ignored) {}
        });
        stdoutThread.start();
        stderrThread.start();

        try {
            boolean finished = process.waitFor(RUN_TIMEOUT_SEC, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return CodeRunResponse.runtimeError(
                        "실행 시간 초과 (" + RUN_TIMEOUT_SEC + "초)\n무한루프 또는 과도한 연산이 없는지 확인하세요.");
            }
            stdoutThread.join(1000);
            stderrThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CodeRunResponse.runtimeError("실행 중단됨");
        }

        String stdout = truncate(stdoutBuf.toString());
        String stderr = stderrBuf.toString();

        if (process.exitValue() != 0 && !stderr.isBlank()) {
            // 예외 스택 트레이스에서 핵심 줄만 추출
            return CodeRunResponse.runtimeError(extractRuntimeError(stderr));
        }

        return CodeRunResponse.success(stdout);
    }

    /* ── 유틸 ── */
    private String readStream(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private String truncate(String s) {
        if (s.length() <= MAX_OUTPUT_CHARS) return s;
        return s.substring(0, MAX_OUTPUT_CHARS) + "\n... (출력이 너무 깁니다. 앞 " + MAX_OUTPUT_CHARS + "자만 표시)";
    }

    /** 스택 트레이스에서 핵심 오류 메시지만 추출 */
    private String extractRuntimeError(String stderr) {
        StringBuilder sb = new StringBuilder();
        for (String line : stderr.split("\n")) {
            // at com.sun... 같은 JVM 내부 스택은 제외
            if (!line.trim().startsWith("at java.")
                    && !line.trim().startsWith("at sun.")
                    && !line.trim().startsWith("at jdk.")) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /** 코드에서 public class 이름 추출 — 없으면 "Main" 기본값 */
    private String extractClassName(String code) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Main";
    }

    private void deleteDirectory(Path dir) {
        try {
            Files.walk(dir)
                    .sorted((a, b) -> b.compareTo(a)) // 파일 먼저, 디렉토리 나중
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); }
                        catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
}