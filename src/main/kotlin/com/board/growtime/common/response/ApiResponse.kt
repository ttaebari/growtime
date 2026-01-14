package com.board.growtime.common.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorInfo? = null
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }

        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(success = false, error = ErrorInfo(code, message))
        }
    }
}

data class ErrorInfo(
    val code: String,
    val message: String
)
