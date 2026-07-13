package com.sparta.server.threeserving.store.service;


import java.util.Set;

public class PageService {
    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 30, 50);

    public static int resolvePageSize(int size){
        return ALLOWED_PAGE_SIZES.contains(size) ? size : 10;
    }
}
