package com.sparta.server.threeserving.store.dto.request;

import lombok.Getter;

import java.util.UUID;

@Getter
public class StoreSearchCondition {
    private String name;
    private UUID regionId;
    private UUID categoryId;
}