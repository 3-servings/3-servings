package com.sparta.server.threeserving.order_management.validator;

import com.sparta.server.threeserving.global.common.exception.ErrorCode;
import com.sparta.server.threeserving.global.exception.CustomException;
import com.sparta.server.threeserving.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreAccessValidator {

    private final StoreRepository storeRepository;


    public void validateStoreAccess(Long userId,UUID storeId) {

        if (!storeRepository.existsByIdAndOwnerId(storeId, userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_STORE_ACCESS);
        }
    }


}