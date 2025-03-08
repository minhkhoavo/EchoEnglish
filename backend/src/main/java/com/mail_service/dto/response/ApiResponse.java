package com.mail_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// https://github.com/omniti-labs/jsend for more detail
public class ApiResponse<T> {
    private String status;  // "success", "fail", or "error"
    private T data;
    private String message;
    private List<ErrorDetail> errors;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ErrorDetail {
        private String field;
        private String message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> fail(List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .status("fail")
                .data(null)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .build();
    }
}
