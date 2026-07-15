package com.sparta.server.threeserving.user.dto;

import com.sparta.server.threeserving.user.entity.UserRoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(
            regexp = "^[a-z0-9]{4,10}$",
            message = "아이디는 4~10자의 소문자(a~z)와 숫자(0~9)로만 구성되어야 합니다."
    )
    private String username;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2~20자여야 합니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
            // 대문자 + 소문자 + 숫자 + 특수문자 각 1개 이상, 공백 불가, 8~15자
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])(?=\\S+$).{8,15}$",
            message = "비밀번호는 8~15자이며 대문자·소문자·숫자·특수문자(@$!%*#?&)를 각각 1개 이상 포함해야 합니다."
    )
    private String password;

    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    private String phone;



}
