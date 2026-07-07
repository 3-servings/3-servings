package com.sparta.server.threeserving.store.entity;

import com.sparta.server.threeserving.global.common.BaseEntity;
import com.sparta.server.threeserving.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Table(name = "p_store")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreCategory> categoryList = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "detail_address", length = 255)
    private String detailAddress;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision =  10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "min_order_price", nullable = false)
    private Integer minOrderPrice;

    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee;

    @Column(name = "delivery_radius_m")
    private Integer deliveryRadiusM;

    @Column(name = "average_rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "is_opne", nullable = false)
    private Boolean isOpen = false;

    public Store(
            User owner,
            Region region,
            String name,
            String phone,
            String address,
            String detailAddress,
            BigDecimal latitude,
            BigDecimal longitude,
            Integer minOrderPrice,
            Integer deliveryFee,
            Integer deliveryRadiusM,
            Long createdBy
    ) {
        this.owner = owner;
        this.region = region;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.detailAddress = detailAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.minOrderPrice = minOrderPrice;
        this.deliveryFee = deliveryFee;
        this.deliveryRadiusM = deliveryRadiusM;
        this.createdBy = createdBy;
        this.createdAt = java.time.LocalDateTime.now();
    }


    public void addCategory(Category category, Long createdBy) {
        StoreCategory storeCategory = new StoreCategory(this, category, createdBy);
        this.storeCategories.add(storeCategory);
    }

    public void changeOpenStatus(Boolean isOpen, Long updatedBy) {
        this.isOpen = isOpen;
        this.updatedBy = updatedBy;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void updateRating(BigDecimal averageRating, Integer reviewCount) {
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public void changeDeliveryFee(Integer deliveryFee, Long updatedBy){
        this.deliveryFee = deliveryFee;
        this.updatedBy = updatedBy;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void changeMinOrderPrice(Integer minOrderPrice, Long updatedBy){
        this.minOrderPrice = minOrderPrice;
        this.updatedBy = updatedBy;
        this.updatedAt = java.time.LocalDateTime.now();
    }


}
