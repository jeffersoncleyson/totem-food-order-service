package com.totem.food.framework.adapters.out.web.internal.payment.request;

import com.totem.food.application.ports.in.dtos.payment.PaymentFilterDto;
import com.totem.food.framework.adapters.out.web.internal.payment.client.PaymentMicroServiceClientApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendPaymentRequestAdapterTest {

    @Mock
    private PaymentMicroServiceClientApi paymentMicroServiceClient;

    @InjectMocks
    private SendPaymentRequestAdapter sendPaymentRequestAdapter;

    @Test
    void testSendRequestSuccess() {

        //## Mock - Object and Values
        var paymentFilterDto = new PaymentFilterDto();
        paymentFilterDto.setOrderId("1");
        paymentFilterDto.setStatus("COMPLETED");

        //## Given
        when(paymentMicroServiceClient.getPayment(anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //## When
        boolean result = sendPaymentRequestAdapter.sendRequest(paymentFilterDto);

        //## Then
        assertTrue(result, "Expected return true when HttpStatus is OK");
    }

    @Test
    void testSendRequestFailure() {

        //## Mock - Object and Values
        var paymentFilterDto = new PaymentFilterDto();
        paymentFilterDto.setOrderId("1");
        paymentFilterDto.setStatus("COMPLETED");

        //## Given
        when(paymentMicroServiceClient.getPayment(anyString(), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        //## When
        boolean result = sendPaymentRequestAdapter.sendRequest(paymentFilterDto);

        //## Then
        assertFalse(result, "Expected return false when HttpStatus is 4xx");
    }
}