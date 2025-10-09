package com.the.model;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@RedisHash("RedisToken")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisToken implements Serializable {
    private String id;
    private String refreshToken;
    private String resetToken;

    @TimeToLive
    private Long timeToLive; // Time in seconds
}
