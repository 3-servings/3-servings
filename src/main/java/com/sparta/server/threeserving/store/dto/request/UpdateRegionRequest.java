package com.sparta.server.threeserving.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateRegionRequest {
    @NotNull
    String name;
}
