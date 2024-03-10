package com.totem.food.framework.adapters.out.email;

import com.totem.food.application.ports.out.dtos.EmailNotificationDto;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class SendEmailEventAdapterTest {

    @Mock
    private SnsTemplate snsTemplate;

    private SendEmailEventAdapter sendEmailEventAdapter;

    @BeforeEach
    public void setup() {
        openMocks(this);
        sendEmailEventAdapter = new SendEmailEventAdapter(snsTemplate);
    }

    @Test
    void sendEmailTest() {

        //## Mock - Object
        var emailNotificationDto = new EmailNotificationDto();

        //## Given - When
        Boolean result = sendEmailEventAdapter.sendMessage(emailNotificationDto);

        //## Then
        verify(snsTemplate).convertAndSend("email-topic", emailNotificationDto);
        assertTrue(result);
    }
}