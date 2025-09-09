package com.the.dto.request;

import com.the.util.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;


import java.io.Serializable;

@Getter
public class SignInRequest implements Serializable {

    @NotBlank(message = "username must be not blank")
    private String username;

    @NotBlank(message = "password must be not blank")
    private String password;

    @NotNull(message = "platform must be not null")
    private Platform platform;

    private String version;

    private String deviceToken;

}
