package com.sparta.server.threeserving.store.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.store.dto.request.CreateRegionRequest;
import com.sparta.server.threeserving.store.dto.request.UpdateRegionRequest;
import com.sparta.server.threeserving.store.dto.response.RegionResponse;
import com.sparta.server.threeserving.store.entity.Region;
import com.sparta.server.threeserving.store.repository.RegionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;




    public ApiResponse<RegionResponse> createRegion(CreateRegionRequest request) {
        if(regionRepository.existsByName(request.getName())){
            throw new CustomException(ErrorCode.DUPLICATED_RESOURCE);
        }

        Region region = Region.builder()
                .name(request.getName())
                .build();

        Region savedRegion = regionRepository.save(region);

        return ApiResponse.success(SuccessCode.CREATED, new RegionResponse(savedRegion));
    }

    public ApiResponse<RegionResponse> getRegion(UUID regionId) {
        Region region = regionRepository.findById(regionId).orElseThrow(
                () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
        );
        return ApiResponse.success(SuccessCode.SUCCESS, new RegionResponse(region));
    }

    @Transactional
    public void serviceOnRegion(UUID regionId) {
        Region region = regionRepository.findById(regionId).orElseThrow(
                () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
        );

        region.changeServiceArea(true);
    }

    @Transactional
    public void serviceOffRegion(UUID regionId) {
        Region region = regionRepository.findById(regionId).orElseThrow(
                () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
        );

        region.changeServiceArea(false);
    }

    @Transactional
    public void updateRegion(UUID regionId, UpdateRegionRequest request) {
        Region region = regionRepository.findById(regionId).orElseThrow(
                () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
        );

        region.update(request.getName());
    }

    @Transactional
    public void deleteRegion(UUID regionId, Long userId) {
        Region region = regionRepository.findById(regionId).orElseThrow(
                () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
        );

        region.softDelete(userId);
    }

    public ApiResponse<Page<RegionResponse>> getServiceRegions(Pageable pageable) {
        Pageable newPageable = PageRequest.of(pageable.getPageNumber(),PageService.resolvePageSize(pageable.getPageSize()),  pageable.getSort());
        Page<RegionResponse> regions = regionRepository.findByIsServiceArea(true, newPageable).map(RegionResponse::new);


        return ApiResponse.success(SuccessCode.SUCCESS, regions);
    }

    public ApiResponse<Page<RegionResponse>> getRegions(Pageable pageable) {
        Pageable newPageable = PageRequest.of(pageable.getPageNumber(),PageService.resolvePageSize(pageable.getPageSize()),  pageable.getSort());
        Page<RegionResponse> regions = regionRepository.findByIsServiceArea(false, newPageable).map(RegionResponse::new);

        return ApiResponse.success(SuccessCode.SUCCESS, regions);
    }


}
