package com.the.util;

public final class PermissionConst {
    private PermissionConst() {}

    // User Permissions
    public static final String USER_CREATE = "user:create";
    public static final String USER_VIEW = "user:view";
    public static final String USER_UPDATE = "user:update";
    public static final String USER_DELETE = "user:delete";
    public static final String USER_RESEND_LINK = "user:resend_link";

    // Course Permissions
    public static final String COURSE_CREATE = "course:create";
    public static final String COURSE_VIEW = "course:view";
    public static final String COURSE_UPDATE = "course:update";
    public static final String COURSE_DELETE = "course:delete";
    public static final String COURSE_ENROLL = "course:enroll";
}
