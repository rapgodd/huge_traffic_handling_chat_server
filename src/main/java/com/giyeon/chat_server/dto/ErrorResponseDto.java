package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@Data
public class ErrorResponseDto {

    private Date timestamp;
    private String message;
    private String details;

    @Builder
    public ErrorResponseDto(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public static ErrorResponseDto error(Exception e, WebRequest request) {
        return ErrorResponseDto.builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .details(request.getDescription(false))
                    .build();
    }

}
