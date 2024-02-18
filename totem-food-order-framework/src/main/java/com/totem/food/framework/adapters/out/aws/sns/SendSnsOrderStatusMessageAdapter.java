package com.totem.food.framework.adapters.out.aws.sns;

import com.totem.food.application.ports.out.dtos.PaymentNotificationDto;
import com.totem.food.application.ports.out.event.ISendEventPort;
import io.awspring.cloud.sns.core.SnsTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SendSnsOrderStatusMessageAdapter implements ISendEventPort<PaymentNotificationDto, Boolean> {

    private final SnsTemplate snsTemplate;
    private final String paymentTopic;

    public SendSnsOrderStatusMessageAdapter(Environment env, SnsTemplate snsTemplate) {
        this.paymentTopic = env.getProperty("ms.internal.topic.payment");
        this.snsTemplate = snsTemplate;
    }

    @Override
    public Boolean sendMessage(PaymentNotificationDto item) {
        Map<String, Object> headerAttributes = Map.of("status", item.getOrder().getStatus().key);
        snsTemplate.convertAndSend(this.paymentTopic, item, headerAttributes);
        return Boolean.TRUE;
    }
}
