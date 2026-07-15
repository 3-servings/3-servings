package com.sparta.server.threeserving.store.dto.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RegisterStore {
    @NotNull
    String name;
    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    String phone;
    @NotNull
    String address;
    String detailAddress;
    @NotNull
    UUID regionId;
    @NotEmpty
    List<UUID> categoryIds;
    @PositiveOrZero
    int minOrderPrice;
    @PositiveOrZero
    int deliveryFee;
    @PositiveOrZero
    int delivery_radius_m;

}
