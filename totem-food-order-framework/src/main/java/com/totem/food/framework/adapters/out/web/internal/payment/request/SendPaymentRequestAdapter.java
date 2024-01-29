package com.totem.food.framework.adapters.out.web.internal.payment.request;

import com.totem.food.application.ports.in.dtos.payment.PaymentFilterDto;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.framework.adapters.out.web.internal.payment.client.PaymentMicroServiceClientApi;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class SendPaymentRequestAdapter implements ISendRequestPort<PaymentFilterDto, Boolean> {

    private final PaymentMicroServiceClientApi paymentMicroServiceClient;

    @Override
    public Boolean sendRequest(PaymentFilterDto item) {
        return Optional.ofNullable(paymentMicroServiceClient.getPayment(item.getOrderId(), item.getStatus()))
                .map(ResponseEntity::getStatusCode)
                .map(HttpStatus::value)
                .map(status -> status == HttpStatus.OK.value())
                .orElse(false);
    }
}
