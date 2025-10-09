package com.the.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordEvent {
    private String email;
    private String username;
    private String token;
    private String type;
}
