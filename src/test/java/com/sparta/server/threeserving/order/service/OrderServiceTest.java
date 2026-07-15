package com.sparta.server.threeserving.order.service;

import com.sparta.server.threeserving.order.entity.OrderItemOption;
import com.sparta.server.threeserving.order.repository.OrderItemOptionRepository;
import com.sparta.server.threeserving.order.repository.OrderItemRepository;
import com.sparta.server.threeserving.order.repository.OrderRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("OrderService")
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderItemOptionRepository orderItemOptionRepository;

    

}
