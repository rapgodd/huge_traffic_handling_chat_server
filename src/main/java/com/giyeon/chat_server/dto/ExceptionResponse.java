package com.giyeon.chat_server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@Data
public class ExceptionResponse {

    private Date timestamp;
    private String message;
    private String details;

    @Builder
    public ExceptionResponse(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public static ExceptionResponse error(Exception e, WebRequest request) {
        return ExceptionResponse.builder()
                    .timestamp(new Date())
                    .message(e.getMessage())
                    .details(request.getDescription(false))
                    .build();
    }

}
