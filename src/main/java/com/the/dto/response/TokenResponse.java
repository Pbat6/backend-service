package com.the.dto.response;

import com.the.model.Role;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class TokenResponse implements Serializable {

    private String accessToken;

    private Long userId;

    List<String> roles;

    // more over
}
