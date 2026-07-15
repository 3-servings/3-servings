package com.sparta.server.threeserving.store.dto;

import com.sparta.server.threeserving.store.dto.response.CategoryResponse;
import com.sparta.server.threeserving.store.dto.response.RegionResponse;
import com.sparta.server.threeserving.store.dto.response.StoreOwnerResponse;
import com.sparta.server.threeserving.store.entity.Store;
import com.sparta.server.threeserving.store.entity.StoreCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class StoreResponse {
    UUID id;
    String name;
    StoreOwnerResponse owner;
    String phone;
    String address;
    String detailAddress;
    RegionResponse region;
    List<CategoryResponse> categories;
    int minOrderPrice;
    int deliveryFee;
    int delivery_radius_m;

    public StoreResponse(Store store){
        this.id = store.getId();
        this.name = store.getName();
        this.owner = new StoreOwnerResponse(store.getOwner());
        this.phone = store.getPhone();
        this.address = store.getAddress();
        this.detailAddress = store.getDetailAddress();
        this.region = new RegionResponse(store.getRegion());
        this.minOrderPrice = store.getMinOrderPrice();
        this.deliveryFee = store.getDeliveryFee();
        this.delivery_radius_m = store.getDeliveryRadiusM();
        this.categories = store.getCategoryList().stream()
                .map(StoreCategory::getCategory)
                .map(CategoryResponse::new)
                .toList();
    }
}
