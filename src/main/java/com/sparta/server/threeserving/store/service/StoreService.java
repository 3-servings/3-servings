package com.sparta.server.threeserving.store.service;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.store.dto.StoreResponse;
import com.sparta.server.threeserving.store.dto.request.RegisterStore;
import com.sparta.server.threeserving.store.dto.request.StoreSearchCondition;
import com.sparta.server.threeserving.store.dto.request.UpdateStoreRequest;
import com.sparta.server.threeserving.store.entity.Category;
import com.sparta.server.threeserving.store.entity.Region;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.repository.CategoryRepository;
import com.sparta.server.threeserving.store.repository.RegionRepository;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import com.sparta.server.threeserving.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final RegionRepository regionRepository;
    private final CategoryRepository categoryRepository;


    @Transactional
    public ApiResponse<StoreResponse> registerStore(RegisterStore request, User owner) {
        Region region = regionRepository.findById(request.getRegionId()).orElseThrow(
                () -> new CustomException(ErrorCode.REGION_NOT_FOUND)
        );

        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
        if(categories.size() != request.getCategoryIds().size()){
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Store store = Store.builder()
                .owner(owner)
                .name(request.getName())
                .region(region)
                .phone(request.getPhone())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .minOrderPrice(request.getMinOrderPrice())
                .deliveryFee(request.getDeliveryFee())
                .deliveryRadiusM(request.getDelivery_radius_m())
                .build();

        categories.forEach(store::addCategory);

        Store savedStore = storeRepository.save(store);
        return ApiResponse.success(SuccessCode.CREATED, new StoreResponse(savedStore));
    }

    @Transactional(readOnly = true)
    public ApiResponse<Page<StoreResponse>> getStores(StoreSearchCondition condition, Pageable pageable, boolean isAdmin) {
        Pageable newPageable = PageRequest.of(
                pageable.getPageNumber(),
                PageService.resolvePageSize(pageable.getPageSize()),
                pageable.getSort()
        );

        Page<StoreResponse> stores = storeRepository.searchStores(
                condition.getName() == null ? "" : condition.getName(),
                condition.getRegionId(),
                condition.getCategoryId(),
                !isAdmin,
                newPageable
        ).map(StoreResponse::new);

        return ApiResponse.success(SuccessCode.SUCCESS, stores);
    }

    @Transactional
    public ApiResponse<StoreResponse> updateStore(UUID storeId, UpdateStoreRequest request, User owner) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getOwner().getId().equals(owner.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new CustomException(ErrorCode.REGION_NOT_FOUND));
        List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());

        if (categories.size() != request.getCategoryIds().size()) {
            throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        store.update(
                request.getName(),
                request.getPhone(),
                request.getAddress(),
                request.getDetailAddress(),
                request.getMinOrderPrice(),
                request.getDeliveryFee(),
                request.getDelivery_radius_m()
        );

        store.changeRegion(region);
        store.changeCategories(categories);

        return ApiResponse.success(SuccessCode.UPDATED, new StoreResponse(store));
    }

    @Transactional
    public void deleteStore(UUID storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        store.softDelete(userId);
    }

    @Transactional(readOnly = true)
    public ApiResponse<StoreResponse> getStore(UUID storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new CustomException(ErrorCode.STORE_NOT_FOUND)
        );

        return ApiResponse.success(SuccessCode.SUCCESS, new StoreResponse(store));
    }

    @Transactional
    public void changeOpenStatus(UUID storeId, Long userId, boolean isOpen) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        store.changeOpenStatus(isOpen);
    }
}
