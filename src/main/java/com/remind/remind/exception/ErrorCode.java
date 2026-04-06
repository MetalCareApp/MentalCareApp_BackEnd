package com.remind.remind.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "가입되지 않은 사용자입니다. 회원가입 화면으로 이동하세요."),
    ALREADY_REGISTERED(HttpStatus.BAD_REQUEST, "U002", "이미 가입된 사용자입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "U003", "유효하지 않은 구글 토큰입니다."),
    TOKEN_VERIFICATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U004", "구글 토큰 검증 중 오류가 발생했습니다."),

    // Diary 관련
    DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "D001", "존재하지 않는 일기입니다."),
    DIARY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "D002", "해당 일기에 대한 접근 권한이 없습니다."),

    // Doctor 관련
    INVALID_PATIENT(HttpStatus.BAD_REQUEST, "DR001", "본인을 환자로 등록할 수 없습니다."),
    ALREADY_MAPPED(HttpStatus.BAD_REQUEST, "DR002", "이미 연결 요청이 진행 중이거나 등록된 환자입니다."),

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
