package com.sparta.server.threeserving.order_management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RejectReasonStatResponse {

    private UUID storeId;
    private List<RejectReasonStatItem> items;

    @Getter
    @Builder
    public static class RejectReasonStatItem {
        private String rejectReasonCode;
        private String description;
        private Long count;
    }
}

