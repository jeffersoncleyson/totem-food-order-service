package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import com.totem.food.application.ports.in.dtos.payment.PaymentFilterDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.out.dtos.EmailNotificationDto;
import com.totem.food.application.ports.out.dtos.PaymentNotificationDto;
import com.totem.food.application.ports.out.event.ISendEventPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.IUpdateRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import com.totem.food.domain.order.totem.OrderDomain;
import lombok.SneakyThrows;
import mock.domain.OrderDomainMock;
import mock.models.OrderModelMock;
import mock.ports.in.dto.CustomerResponseMock;
import mock.ports.in.dto.OrderDtoMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatusOrderUseCaseTest {

    @Spy
    private IOrderMapper iOrderMapper;

    @Mock
    private ISearchUniqueRepositoryPort<Optional<OrderModel>> iSearchUniqueRepositoryPort;

    @Mock
    private IUpdateRepositoryPort<OrderModel> iProductRepositoryPort;

    @Mock
    private ISendEventPort<EmailNotificationDto, Boolean> iSendEmailEventPort;

    @Mock
    private ISendRequestPort<PaymentFilterDto, Boolean> iSendRequestPaymentPort;

    @Mock
    private ISendRequestPort<String, Optional<CustomerResponse>> iSearchUniqueCustomerRepositoryPort;

    @Mock
    private ISendEventPort<PaymentNotificationDto, Boolean> sendEventPort;

    private UpdateStatusOrderUseCase updateStatusOrderUseCase;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        updateStatusOrderUseCase = new UpdateStatusOrderUseCase(
                iOrderMapper,
                iSearchUniqueRepositoryPort,
                iProductRepositoryPort,
                iSendEmailEventPort,
                iSendRequestPaymentPort,
                iSearchUniqueCustomerRepositoryPort,
                sendEventPort
        );
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        closeable.close();
    }

    @Test
    void updateStatusWhenStatusEqualsREADY() {

        //## Mock - Objects and Value
        String id = UUID.randomUUID().toString();
        String customerId = UUID.randomUUID().toString();

        var customerResponse = CustomerResponseMock.getMock(customerId);

        var orderModel = OrderModelMock.orderModel(OrderStatusEnumDomain.IN_PREPARATION);
        orderModel.setCustomer(customerResponse.getId());

        var orderDomain = OrderDomainMock.getOrderDomain(OrderStatusEnumDomain.IN_PREPARATION);
        orderDomain.setCustomer(customerResponse.getId());

        var orderDto = OrderDtoMock.getMock(OrderStatusEnumDomain.READY.toString());
        orderDto.setCustomer(customerResponse.getId());

        //## Given
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.of(orderModel));
        when(iOrderMapper.toDomain(any(OrderModel.class))).thenReturn(orderDomain);
        when(iSearchUniqueCustomerRepositoryPort.sendRequest(anyString())).thenReturn(Optional.of(customerResponse));
        when(iSendEmailEventPort.sendMessage(any(EmailNotificationDto.class))).thenReturn(Boolean.valueOf(id));
        when(iOrderMapper.toModel(any(OrderDomain.class))).thenReturn(orderModel);
        when(iProductRepositoryPort.updateItem(any(OrderModel.class))).thenReturn(orderModel);
        when(iOrderMapper.toDto(any(OrderModel.class))).thenReturn(orderDto);

        //## When
        var result = updateStatusOrderUseCase.updateStatus(id, OrderStatusEnumDomain.READY.toString(), false);

        //## Then
        assertNotNull(result);
        assertThat(result).usingRecursiveComparison().ignoringFieldsOfTypes(ZonedDateTime.class).isNotNull();
        assertEquals(OrderStatusEnumDomain.READY.toString(), result.getStatus());
        assertEquals(customerResponse.getId(), result.getCustomer());

        verify(iSendRequestPaymentPort, never()).sendRequest(any(PaymentFilterDto.class));
    }

    @Test
    @Disabled("Arrumar este teste")
    void updateStatusWhenStatusEqualsRECEIVEDAndNotPayment() {

        //## Mock - Objects and Value
        var orderModel = OrderModelMock.orderModel(OrderStatusEnumDomain.WAITING_PAYMENT);
        var orderDomain = OrderDomainMock.getOrderDomain(OrderStatusEnumDomain.WAITING_PAYMENT);
        String id = UUID.randomUUID().toString();

        //## Given
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.of(orderModel));
        when(iOrderMapper.toDomain(any(OrderModel.class))).thenReturn(orderDomain);
        when(iSendRequestPaymentPort.sendRequest(any(PaymentFilterDto.class))).thenReturn(false);

        //## When
        var exception = assertThrows(ElementNotFoundException.class,
                () -> updateStatusOrderUseCase.updateStatus(id, OrderStatusEnumDomain.RECEIVED.toString(), false));

        //## Then
        assertEquals(String.format("Order [%s] needs a payment request or Payment is PENDING", id), exception.getMessage());
        verify(iOrderMapper, never()).toDto(orderModel);
    }

    @Test
    void updateStatusWhenStatusEqualsStatusParamMethod() {

        //## Mock - Objects and Value
        var orderModel = OrderModelMock.orderModel(OrderStatusEnumDomain.RECEIVED);
        var orderDto = OrderDtoMock.getMock(OrderStatusEnumDomain.RECEIVED.toString());
        String id = UUID.randomUUID().toString();

        //## Given
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.of(orderModel));
        when(iOrderMapper.toDto(any(OrderModel.class))).thenReturn(orderDto);

        //## When
        var result = updateStatusOrderUseCase.updateStatus(id, OrderStatusEnumDomain.RECEIVED.toString(), false);

        //## Then
        assertThat(result).usingRecursiveComparison().isNotNull();
        verify(iOrderMapper, times(1)).toDto(orderModel);
        verify(iOrderMapper, never()).toDomain(orderModel);

    }

    @Test
    @Disabled("Arrumar este teste")
    void updateStatusWhenElementNotFoundException() {

        //## Mock - Objects and Value
        String id = UUID.randomUUID().toString();

        //## Given
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.empty());

        //## When
        var exception = assertThrows(ElementNotFoundException.class,
                () -> updateStatusOrderUseCase.updateStatus(id, anyString(), anyBoolean()));

        //## Then
        assertEquals(String.format("Order [%s] not found", id), exception.getMessage());

    }
}