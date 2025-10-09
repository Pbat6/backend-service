package com.the.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UserType {
    @JsonProperty("manager")
    MANAGER,
    @JsonProperty("admin")
    ADMIN,
    @JsonProperty("user")
    USER;
}
