package com.giyeon.chat_server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupDto {

    @NotBlank(message = "이메일을 반드시 입력해야 합니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 반드시 입력해야 합니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "이름을 반드시 입력해야 합니다.")
    @Size(min = 5, message = "이름은 최소 2자 이상이어야 합니다.")
    private String name;

}
