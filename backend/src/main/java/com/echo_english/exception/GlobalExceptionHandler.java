package com.echo_english.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

//@ControllerAdvice
public class GlobalExceptionHandler {
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
//        return new ResponseEntity<>(ApiResponse.error(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
//        return new ResponseEntity<>(ApiResponse.error("Unexpected error: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
//    }
private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Xử lý lỗi không tìm thấy tài nguyên (trả về 404)
    @ExceptionHandler({ResourceNotFoundException.class, NoSuchElementException.class}) // Bắt cả hai loại
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(Exception ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Xử lý lỗi truy cập bị cấm (trả về 403)
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ErrorDetails> handleForbiddenAccessException(ForbiddenAccessException ex, WebRequest request) {
        logger.warn("Forbidden access attempt: {}", ex.getMessage());
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                ex.getMessage(),
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    // Xử lý lỗi validation (trả về 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.warn("Validation failed: {}", errors);
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors,
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    // Xử lý các lỗi chung khác (trả về 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex); // Log cả stack trace
        ErrorDetails errorDetails = new ErrorDetails(
                new Date(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support.", // Che giấu chi tiết lỗi khỏi client
                request.getDescription(false));
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Lớp nội tuyến để cấu trúc chi tiết lỗi
    public static class ErrorDetails {
        private Date timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ErrorDetails(Date timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            // Trích xuất path ngắn gọn hơn
            this.path = path.startsWith("uri=") ? path.substring(4) : path;
        }

        // Getters (cần thiết cho Jackson serialization)
        public Date getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
    }
}
