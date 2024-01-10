package com.totem.food.framework.adapters.out.web.internal.customer.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "CustomerMicroServiceClientApi", url = "${ms.internal.customer.url}")
public interface CustomerMicroServiceClientApi {

}
