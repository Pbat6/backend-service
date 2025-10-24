package com.the.controller;

import com.the.dto.response.ResponseData;
import com.the.dto.response.TokenResponse;
import com.the.model.User;
import com.the.service.CommonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/common")
@Tag(name = "Common Controller")
public class CommonController {
    private final CommonService commonService;

    @PostMapping("/resend-link")
    @PreAuthorize("hasAuthority('user:resend_link')")
    public ResponseData<TokenResponse> accessToken(@RequestBody String email) {
        User actor = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commonService.resendLink(email, actor);
        return new ResponseData<>(HttpStatus.OK.value(), "Resend link successful");
    }
}
