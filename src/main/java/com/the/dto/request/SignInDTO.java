package com.the.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;


import java.io.Serializable;

@Getter
public class SignInDTO implements Serializable {

    @NotBlank(message = "username must be not blank")
    private String username;

    @NotBlank(message = "password must be not blank")
    private String password;
}
