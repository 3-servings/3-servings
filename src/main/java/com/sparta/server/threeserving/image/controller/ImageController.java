package com.sparta.server.threeserving.image.controller;

import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.image.dto.request.ImagePresignedUrlRequest;
import com.sparta.server.threeserving.image.dto.response.ImagePresignedUrlResponse;
import com.sparta.server.threeserving.image.service.ImageS3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageS3Service imageS3Service;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<ImagePresignedUrlResponse>> getPresignedUrl(
            @Valid @RequestBody ImagePresignedUrlRequest request
    ) {
        ImagePresignedUrlResponse response = imageS3Service.generatePresignedUrl(request);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SUCCESS, response));
    }
}
