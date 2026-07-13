package com.sparta.server.threeserving.store.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StoreSearchCondition {
    private String name;
    private UUID regionId;
    private UUID categoryId;
}