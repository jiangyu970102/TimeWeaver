package com.timeweaver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为 3-50 个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为 6-100 个字符")
    private String password;

    @Size(max = 50, message = "昵称最长 50 个字符")
    private String nickname;

    @jakarta.validation.constraints.Email(message = "邮箱格式不正确")
    private String email;
}
