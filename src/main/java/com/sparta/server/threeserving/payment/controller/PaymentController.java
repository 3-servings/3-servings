package com.sparta.server.threeserving.payment.controller;

import com.sparta.server.threeserving.auth.UserDetailsImpl;
import com.sparta.server.threeserving.global.common.response.ApiResponse;
import com.sparta.server.threeserving.global.common.response.SuccessCode;
import com.sparta.server.threeserving.payment.dto.request.PaymentRequest;
import com.sparta.server.threeserving.payment.dto.request.TossConfirmRequest;
import com.sparta.server.threeserving.payment.dto.response.PaymentLogResponse;
import com.sparta.server.threeserving.payment.dto.response.PaymentResponse;
import com.sparta.server.threeserving.payment.dto.response.RefundSuccessResponse;
import com.sparta.server.threeserving.payment.service.PaymentService;
import com.sparta.server.threeserving.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}/payments/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId,
            @RequestBody TossConfirmRequest request
            ) {
        PaymentResponse response = paymentService.confirmPayment(
                userDetails.getUser().getId(),
                orderId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CREATED, response)
        );
    }

    @PostMapping("/{orderId}/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> postPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId,
            @RequestBody PaymentRequest request
            ){
        PaymentResponse response = paymentService.createPayment(
                userDetails.getUser().getId(),
                orderId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.CREATED, response)
        );
    }
    @PatchMapping("/{orderId}/payments/refund")
    public ResponseEntity<ApiResponse<RefundSuccessResponse>> refund(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ) {
        RefundSuccessResponse response = paymentService.refund(userDetails.getUser().getId(), orderId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.UPDATED, response)
        );
    }
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
    ){
        PaymentResponse response = paymentService.getPayment(userDetails.getUser().getId(), orderId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SUCCESS, response)
        );
    }
    @GetMapping("/{orderId}/payments/logs")
    public ResponseEntity<ApiResponse<List<PaymentLogResponse>>> getPaymentLogs(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID orderId
            ) {
        List<PaymentLogResponse> response = paymentService.getPaymentLogs(userDetails.getUser().getId(), orderId);

        return ResponseEntity.ok(
                ApiResponse.success(SuccessCode.SUCCESS, response)
        );
    }
}
