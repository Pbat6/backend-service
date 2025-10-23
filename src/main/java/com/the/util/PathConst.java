package com.the.util;

public final class PathConst {

    private PathConst(){
    }

    // Tiền tố
    public static final String AUTH = "/auth";
    public static final String USER = "/user";
    public static final String COMMON = "/common";

    // Auth endpoints
    public static final String AUTH_CHANGE_PASSWORD = AUTH + "/change-password";
    public static final String AUTH_LOG_OUT = AUTH + "/log-out";
    public static final String AUTH_ALL = AUTH + "/**";

    // Common endpoints
    public static final String COMMON_RESEND_LINK = COMMON + "/resend-link";

    // User endpoints
    public static final String USER_HOME = USER + "/*";
    public static final String USER_LIST = USER + "/list";
    public static final String USER_SEARCH = USER + "/search";
}
