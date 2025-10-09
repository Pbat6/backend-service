package com.the.dto.request;

import lombok.Getter;

@Getter
public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
}
