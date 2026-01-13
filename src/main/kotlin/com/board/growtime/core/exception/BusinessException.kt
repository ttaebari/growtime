package com.board.growtime.core.exception

/**
 * 비즈니스 예외를 나타내는 기본 클래스
 * 
 * 학습 포인트:
 * 1. Custom Exception 패턴 - 도메인별 예외 클래스 정의
 * 2. 계층화된 예외 구조 - 공통 부모 클래스로 예외 관리 통일
 * 3. 에러 코드와 메시지 분리 - 국제화 및 에러 처리 개선
 */
abstract class BusinessException(
    val errorCode: String,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 사용자 관련 예외
 */
class UserNotFoundException(githubId: String) : BusinessException(
    errorCode = "USER_NOT_FOUND",
    message = "사용자를 찾을 수 없습니다: $githubId"
)

class InvalidUserDataException(message: String) : BusinessException(
    errorCode = "INVALID_USER_DATA",
    message = message
)

/**
 * 회고 관련 예외
 */
class NoteNotFoundException(noteId: Long) : BusinessException(
    errorCode = "NOTE_NOT_FOUND",
    message = "회고를 찾을 수 없습니다: $noteId"
)

class InvalidNoteDataException(message: String) : BusinessException(
    errorCode = "INVALID_NOTE_DATA",
    message = message
)

/**
 * 복무 관련 예외
 */
class InvalidServiceDateException(message: String) : BusinessException(
    errorCode = "INVALID_SERVICE_DATE",
    message = message
)

class ServiceDateNotSetException : BusinessException(
    errorCode = "SERVICE_DATE_NOT_SET",
    message = "복무 날짜가 설정되지 않았습니다"
)

/**
 * 검증 관련 예외
 */
class ValidationException(message: String) : BusinessException(
    errorCode = "VALIDATION_ERROR",
    message = message
)
