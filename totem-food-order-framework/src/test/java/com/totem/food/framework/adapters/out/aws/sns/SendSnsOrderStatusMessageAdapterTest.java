package com.totem.food.framework.adapters.out.aws.sns;

import com.totem.food.application.ports.out.dtos.PaymentNotificationDto;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import io.awspring.cloud.sns.core.SnsTemplate;
import mocks.models.OrderModelMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SendSnsOrderStatusMessageAdapterTest {

    @Mock
    private Environment env;

    @Mock
    private SnsTemplate snsTemplate;

    @InjectMocks
    private SendSnsOrderStatusMessageAdapter sendSnsOrderStatusMessageAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMessage() {

        //## Mock - Object
        String paymentTopic = "paymentTopic";
        var orderModel = OrderModelMock.getOrderModel(OrderStatusEnumDomain.FINALIZED);
        var item = new PaymentNotificationDto();
        item.setOrder(orderModel);
        Map<String, Object> headerAttributes = Map.of("status", item.getOrder().getStatus().key);

        //## Given
        ReflectionTestUtils.setField(sendSnsOrderStatusMessageAdapter, "paymentTopic", paymentTopic);

        //## When
        sendSnsOrderStatusMessageAdapter.sendMessage(item);

        //## Then
        verify(snsTemplate, never()).convertAndSend(eq(paymentTopic), eq(item), eq(headerAttributes));
        verifyNoMoreInteractions(snsTemplate);
    }
}