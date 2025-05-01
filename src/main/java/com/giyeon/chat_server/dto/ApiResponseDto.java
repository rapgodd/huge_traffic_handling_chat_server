package com.giyeon.chat_server.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponseDto<T> {
    private int code;
    private T data;
}
