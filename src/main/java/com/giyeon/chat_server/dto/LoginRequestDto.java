package com.giyeon.chat_server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDto {

    @Email(message="이메일 형식이 올바르지 않습니다.")
    @NotBlank(message="이메일은 필수 입력 항목입니다.")
    private String email;

    @NotNull(message="비밀번호는 필수 입력 항목입니다.")
    private String password;

}
