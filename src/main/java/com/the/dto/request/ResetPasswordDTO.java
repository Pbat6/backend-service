package com.the.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ResetPasswordDTO {

    @NotBlank(message = "secretKey must be not blank")
    private String resetToken;

    @NotBlank(message = "password must be not blank")
    private String password;

    @NotBlank(message = "confirmPassword must be not blank")
    private String confirmPassword;

}
