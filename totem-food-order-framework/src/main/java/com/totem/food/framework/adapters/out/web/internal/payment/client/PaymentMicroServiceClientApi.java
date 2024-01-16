package com.totem.food.framework.adapters.out.web.internal.payment.client;

import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PaymentMicroServiceClientApi", url = "${ms.internal.payment.url}")
public interface PaymentMicroServiceClientApi {

    @GetMapping(value = "/v1/totem/payment/order/{orderId}/payment-status/{statusName}", produces = "application/json")
    ResponseEntity<CustomerResponse> getPayment(@PathVariable("orderId") String orderId, @PathVariable("statusName") String statusName);

}
