package com.sparta.server.threeserving.store.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.store.dto.request.CreateRegionRequest;
import com.sparta.server.threeserving.store.dto.request.UpdateRegionRequest;
import com.sparta.server.threeserving.store.dto.response.RegionResponse;
import com.sparta.server.threeserving.store.entity.Region;
import com.sparta.server.threeserving.store.service.RegionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;


    @PostMapping
    public ApiResponse<RegionResponse> createRegion(@Valid @RequestBody CreateRegionRequest request){
        return regionService.createRegion(request);
    }

    @GetMapping
    public ApiResponse<Page<RegionResponse>> getRegions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
            ){
        return regionService.getRegions(pageable);
    }

    @GetMapping("/service-area")
    public ApiResponse<Page<RegionResponse>> getServiceRegions(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
    ){
        return regionService.getServiceRegions(pageable);
    }

    @GetMapping("/{regionId}")
    public ApiResponse<RegionResponse> getRegion(@PathVariable UUID regionId){
        return regionService.getRegion(regionId);
    }

    @PutMapping("/{regionId}")
    public ApiResponse<Void> updateRegion(@PathVariable UUID regionId, @Valid @RequestBody UpdateRegionRequest request){
        regionService.updateRegion(regionId, request);
        return ApiResponse.success(SuccessCode.UPDATED);
    }

    @PatchMapping("/{regionId}/service-on")
    public ApiResponse<Void> serviceOnRegion(@PathVariable UUID regionId){
        regionService.serviceOnRegion(regionId);
        return ApiResponse.success(SuccessCode.UPDATED);
    }

    @PatchMapping("/{regionId}/service-off")
    public ApiResponse<Void> serviceOffRegion(@PathVariable UUID regionId){
        regionService.serviceOffRegion(regionId);
        return ApiResponse.success(SuccessCode.UPDATED);
    }

    @DeleteMapping("/{regionId}")
    public ApiResponse<Void> deleteRegion(@PathVariable UUID regionId, @AuthenticationPrincipal UserDetailsImpl userDetails){
        Long userId = userDetails.getUser().getId();
        regionService.deleteRegion(regionId, userId);
        return ApiResponse.success(SuccessCode.DELETED);
    }
}
