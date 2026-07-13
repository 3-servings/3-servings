package com.sparta.server.threeserving.store.dto.response;

import com.sparta.server.threeserving.store.entity.Region;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class RegionResponse {
    UUID id;
    String name;
    Boolean isServiceArea;
    Instant createdAt;

    public RegionResponse(Region region){
        this.id = region.getId();
        this.name = region.getName();
        this.isServiceArea = region.getIsServiceArea();
        this.createdAt = region.getCreatedAt();
    }
}
