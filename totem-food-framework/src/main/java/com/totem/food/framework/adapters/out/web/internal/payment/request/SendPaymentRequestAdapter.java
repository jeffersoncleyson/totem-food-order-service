package com.totem.food.framework.adapters.out.web.internal.payment.request;

import com.totem.food.application.ports.in.dtos.payment.PaymentFilterDto;
import com.totem.food.application.ports.out.persistence.payment.PaymentModel;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.framework.adapters.out.web.internal.payment.client.PaymentMicroServiceClientApi;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class SendPaymentRequestAdapter implements ISendRequestPort<PaymentFilterDto, List<PaymentModel>> {

    private final PaymentMicroServiceClientApi paymentMicroServiceClient;

    @Override
    public List<PaymentModel> sendRequest(PaymentFilterDto item) {
        return Collections.emptyList();
    }
}
