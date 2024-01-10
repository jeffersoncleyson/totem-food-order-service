package com.totem.food.framework.adapters.out.web.internal.customer.request;

import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.framework.adapters.out.web.internal.customer.client.CustomerMicroServiceClientApi;
import com.totem.food.framework.adapters.out.web.internal.payment.client.PaymentMicroServiceClientApi;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class SendCustomerRequestAdapter implements ISendRequestPort<String, Optional<CustomerResponse>> {

    private final CustomerMicroServiceClientApi customerMicroServiceClientApi;

    @Override
    public Optional<CustomerResponse> sendRequest(String cpf) {
        return Optional.ofNullable(customerMicroServiceClientApi.getCustomerByCpf(cpf))
                .map(ResponseEntity::getBody);
    }
}
