package com.the.util;

public final class SearchConst {
    private SearchConst() {
    }

    public static final String SEARCH_OPERATOR = "(\\w+?)(:|<|>)(.*)";
    public static final String SORT_BY = "(\\w+?)(:)(asc|desc)";
}
