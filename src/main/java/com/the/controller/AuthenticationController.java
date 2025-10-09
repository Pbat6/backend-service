package com.the.controller;

import com.the.dto.request.ChangePasswordDTO;
import com.the.dto.request.ResetPasswordDTO;
import com.the.dto.request.SignInRequest;
import com.the.dto.response.ResponseData;
import com.the.dto.response.TokenResponse;
import com.the.service.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/sign-in")
    public ResponseData<TokenResponse> accessToken(@RequestBody SignInRequest request, HttpServletResponse response) {
        return new ResponseData<>(HttpStatus.OK.value(), "Login successful", authenticationService.signIn(request, response));
    }

    @PostMapping("/refresh-token")
    public ResponseData<TokenResponse> refreshToken(HttpServletRequest request) {
        return new ResponseData<>(HttpStatus.OK.value(), "Update Access Token Successful", authenticationService.refreshToken(request));
    }

    @PostMapping("/log-out")
    public ResponseData<String> removeToken(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.removeToken(request, response);
        return new ResponseData<>(HttpStatus.OK.value(), "Log Out Successful");
    }

    @PostMapping("/forgot-password")
    public ResponseData<String> forgotPassword(@RequestBody String email) {
        try{
            authenticationService.forgotPassword(email);
            return new ResponseData<>(HttpStatus.OK.value(), "Send Email Successful, Please Check Your Email");
        }catch (Exception e){
            return new ResponseData<>(HttpStatus.OK.value(), "Send Email Successful, Please Check Your Email");
        }
    }

    @PostMapping("/reset-password")
    public ResponseData<String> resetPassword(@RequestBody @Valid ResetPasswordDTO request) {
        authenticationService.resetPassword(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Reset Password Successful. Please login again");
    }

    @PostMapping("/change-password")
    public ResponseData<String> changePassword(@RequestBody @Valid ChangePasswordDTO request) {
        authenticationService.changePassword(request);
        return new ResponseData<>(HttpStatus.OK.value(), "Change Password Successful. Please login again");
    }
}
