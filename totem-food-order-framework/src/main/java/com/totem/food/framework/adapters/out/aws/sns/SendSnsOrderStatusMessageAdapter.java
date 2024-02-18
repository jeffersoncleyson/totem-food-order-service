package com.totem.food.framework.adapters.out.aws.sns;

import com.totem.food.application.ports.out.dtos.PaymentNotificationDto;
import com.totem.food.application.ports.out.event.ISendEventPort;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@AllArgsConstructor
@Component
@Slf4j
public class SendSnsMessageAdapter implements ISendEventPort<PaymentNotificationDto, Boolean> {

    private final SnsTemplate snsTemplate;

    @Override
    public Boolean sendMessage(PaymentNotificationDto item) {
        Map<String, Object> headerAttributes = Map.of("status", item.getOrder().getStatus().key);
        snsTemplate.convertAndSend("payment-topic", item, headerAttributes);
        return Boolean.TRUE;
    }
}
