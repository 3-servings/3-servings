package com.sparta.server.threeserving.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRegionRequest {
    @NotNull
    String name;
}
