package com.totem.food.framework.adapters.out.web.internal.payment.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "PaymentMicroServiceClientApi", url = "${ms.internal.payment.url}")
public interface PaymentMicroServiceClientApi {

}
