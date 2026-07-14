// ReviewImagePresignResponse.java  (1단계 응답)
package com.sparta.server.threeserving.review.dto;

import java.util.List;

public record ReviewImagePresignResponse(
        List<Item> items
) {
    public record Item(
            String key,         // 3단계에서 그대로 돌려줄 값
            String uploadUrl,   // 프론트가 PUT 할 presigned URL
            String publicUrl,   // 업로드 후 접근 URL(미리보기용)
            int sequence
    ) {}
}