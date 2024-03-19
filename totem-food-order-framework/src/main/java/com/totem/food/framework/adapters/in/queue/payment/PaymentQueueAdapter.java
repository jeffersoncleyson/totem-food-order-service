package com.totem.food.framework.adapters.in.queue.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.totem.food.application.ports.in.dtos.event.AWSMessage;
import com.totem.food.application.ports.in.dtos.event.PaymentEventMessageDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.event.IReceiveEventPort;
import com.totem.food.application.usecases.commons.IUpdateStatusUseCase;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class PaymentQueueAdapter implements IReceiveEventPort<Message<AWSMessage>, Void> {

    private final IUpdateStatusUseCase<OrderDto> iUpdateStatusUseCase;
    private final ObjectMapper objectMapper;

    @SqsListener(value = "${ms.internal.queue.payment_update_order}")
    @Override
    public Void receiveMessage(Message<AWSMessage> message) {
        try {
            var paymentEventMessageDto = objectMapper.readValue(message.getPayload().getMessage(), PaymentEventMessageDto.class);
            if(paymentEventMessageDto.getStatus().equals("COMPLETED")) {
                iUpdateStatusUseCase.updateStatus(paymentEventMessageDto.getOrder(), OrderStatusEnumDomain.RECEIVED.key, true);
                Acknowledgement.acknowledge(message);
            } else if(paymentEventMessageDto.getStatus().equals("CANCELED")) {
                iUpdateStatusUseCase.updateStatus(paymentEventMessageDto.getOrder(), OrderStatusEnumDomain.CANCELED.key, true);
                Acknowledgement.acknowledge(message);
            }
        } catch (JsonProcessingException e) {
            log.error("Error to processing Event with body {}", message);
        }
        return null;
    }
}
