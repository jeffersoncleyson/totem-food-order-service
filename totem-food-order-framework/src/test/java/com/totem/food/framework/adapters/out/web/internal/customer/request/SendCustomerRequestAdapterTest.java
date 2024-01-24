package com.totem.food.framework.adapters.out.web.internal.customer.request;

import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import com.totem.food.framework.adapters.out.web.internal.customer.client.CustomerMicroServiceClientApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendCustomerRequestAdapterTest {
    private static final String CPF = "123";
    @Mock
    private CustomerMicroServiceClientApi customerMicroServiceClientApi;

    @InjectMocks
    private SendCustomerRequestAdapter sendCustomerRequestAdapter;

    @Test
    void shouldReturnOptionalEmptyWhenCustomerNotFound() {

        //## Given
        when(customerMicroServiceClientApi.getCustomerByCpf(CPF)).thenReturn(null);

        //## When
        var result = sendCustomerRequestAdapter.sendRequest(CPF);

        //## Then
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnCustomerResponseWhenCustomerFound() {

        //## Mock - Objects and Value
        var customerResponse = new CustomerResponse();
        customerResponse.setCpf(CPF);

        //## Given
        when(customerMicroServiceClientApi.getCustomerByCpf(CPF)).thenReturn(ResponseEntity.of(Optional.of(customerResponse)));

        //## When
        var result = sendCustomerRequestAdapter.sendRequest(CPF);

        //## Then
        assertTrue(result.isPresent());
        assertEquals(customerResponse, result.get());
    }
}