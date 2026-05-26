package com.howprom.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeNicknameRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 16, message = "닉네임은 2자 이상 16자 이하여야 합니다.")
    private String nickname;
}