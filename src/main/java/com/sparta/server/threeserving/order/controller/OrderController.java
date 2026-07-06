package com.sparta.server.threeserving.order.controller;

import com.sparta.server.threeserving.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@RestController(value = "/api")
public class OrderController {
    private final OrderService orderService;

}
