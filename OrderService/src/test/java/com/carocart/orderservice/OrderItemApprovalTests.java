package com.carocart.orderservice;

import com.carocart.orderservice.entity.ApprovalStatus;
import com.carocart.orderservice.entity.OrderItem;
import com.carocart.orderservice.dto.VendorDTO;
import com.carocart.orderservice.dto.VendorResponseDTO;
import com.carocart.orderservice.feign.*;
import com.carocart.orderservice.repository.OrderItemRepository;
import com.carocart.orderservice.repository.OrderRepository;
import com.carocart.orderservice.service.OrderServiceImpl;
import com.carocart.orderservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderItemApprovalTests {

    private OrderServiceImpl service;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartClient cartClient;
    @Mock
    private ProductClient productClient;
    @Mock
    private UserClient userClient;
    @Mock
    private AddressClient addressClient;
    @Mock
    private OrdersInternalUserClient ordersInternalUserClient;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private VendorClient vendorClient;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private DeliveryBoyClient deliveryClient;
    @Mock
    private DeliveryEarningClient deliveryEarningClient;
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new OrderServiceImpl(orderRepository, cartClient, productClient, userClient,
                addressClient,ordersInternalUserClient, notificationClient, vendorClient, deliveryClient, orderItemRepository, jwtUtil,deliveryEarningClient);
    }

    @Test
    void vendorCanRejectOwnItem() {
        VendorResponseDTO vendor = new VendorResponseDTO();
        vendor.setEmail("v@test.com");
        when(vendorClient.getCurrentVendor("token")).thenReturn(vendor);

        OrderItem item = new OrderItem();
        item.setVendorEmail("v@test.com");
        when(orderItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));

        service.rejectItemAsVendor("token", 1L);

        assertEquals(ApprovalStatus.REJECTED, item.getApprovalStatus()); // <-- FIXED
        verify(orderItemRepository).save(item);
    }

//    @Test
//    void adminRejectsAdminItem() {
//        OrderItem item = new OrderItem();
//        item.setApprovalRequiredBy("ADMIN");
//        when(orderItemRepository.findById(2L)).thenReturn(java.util.Optional.of(item));
//
//        service.rejectItemAsAdmin(2L);
//
//        assertEquals(ApprovalStatus.REJECTED, item.getApprovalStatus()); // <-- FIXED
//        verify(orderItemRepository).save(item);
//    }
}
