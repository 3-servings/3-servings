package com.sparta.server.threeserving.store.dto.response;

import com.sparta.server.threeserving.store.entity.Category;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class CategoryResponse {
    UUID id;
    String name;
    Instant createdAt;

    public CategoryResponse(Category category){
        this.id = category.getId();
        this.name = category.getName();
        this.createdAt = category.getCreatedAt();
    }
}
