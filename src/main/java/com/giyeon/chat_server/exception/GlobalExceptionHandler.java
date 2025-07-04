package com.giyeon.chat_server.exception;

import com.giyeon.chat_server.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<?> globalError(Exception e, WebRequest request) {

        ErrorResponseDto errorResponseDto = ErrorResponseDto.error(e,request);
        log.error("Runtime Exception: {}", errorResponseDto.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponseDto);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidationException(IllegalArgumentException e, WebRequest request) {
        ErrorResponseDto errorResponse = ErrorResponseDto.error(e,request);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }


}
