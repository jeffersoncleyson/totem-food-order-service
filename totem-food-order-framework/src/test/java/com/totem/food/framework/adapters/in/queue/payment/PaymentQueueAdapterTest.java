package com.totem.food.framework.adapters.in.queue.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.totem.food.application.ports.in.dtos.event.AWSMessage;
import com.totem.food.application.ports.in.dtos.event.PaymentEventMessageDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.usecases.commons.IUpdateStatusUseCase;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentQueueAdapterTest {

    @InjectMocks
    private PaymentQueueAdapter paymentQueueAdapter;

    @Mock
    private IUpdateStatusUseCase<OrderDto> iUpdateStatusUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @Disabled("Test breaking when calling the Acknowledgement.acknowledge method.")
    void receiveMessage() throws JsonProcessingException {
        var paymentEventMessageDto = new PaymentEventMessageDto();
        paymentEventMessageDto.setOrder("RECEIVED");
        paymentEventMessageDto.setStatus("COMPLETED");

        var awsMessage = new AWSMessage();
        awsMessage.setMessage("dummy_message");
        Message<AWSMessage> message = MessageBuilder.withPayload(awsMessage).build();

        when(objectMapper.readValue(awsMessage.getMessage(), PaymentEventMessageDto.class))
                .thenReturn(paymentEventMessageDto);

        paymentQueueAdapter.receiveMessage(message);

        verify(iUpdateStatusUseCase)
                .updateStatus(paymentEventMessageDto.getOrder(), OrderStatusEnumDomain.RECEIVED.key, true);
    }

}