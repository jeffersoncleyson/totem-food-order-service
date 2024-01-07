package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.dtos.payment.PaymentFilterDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.out.dtos.EmailNotificationDto;
import com.totem.food.application.ports.out.email.ISendEmailPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.IUpdateRepositoryPort;
import com.totem.food.application.ports.out.persistence.customer.CustomerModel;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.ports.out.persistence.payment.PaymentModel;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.application.usecases.commons.IUpdateStatusUseCase;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import lombok.SneakyThrows;
import mock.domain.CustomerDomainMock;
import mock.domain.OrderDomainMock;
import mock.models.CustomerModelMock;
import mock.models.OrderModelMock;
import mock.models.PaymentModelMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatusOrderUseCaseTest {

    @Spy
    private IOrderMapper iOrderMapper = Mappers.getMapper(IOrderMapper.class);
    @Mock
    private ISearchUniqueRepositoryPort<Optional<OrderModel>> iSearchUniqueRepositoryPort;
    @Mock
    private IUpdateRepositoryPort<OrderModel> iProductRepositoryPort;
    @Mock
    private ISendEmailPort<EmailNotificationDto, Boolean> iSendEmailPort;
    @Mock
    private ISendRequestPort<PaymentFilterDto, List<PaymentModel>> iSendRequestPaymentPort;

    @Mock
    private ISearchUniqueRepositoryPort<Optional<CustomerModel>> iSearchUniqueCustomerRepositoryPort;

    private IUpdateStatusUseCase<OrderDto> iUpdateStatusUseCase;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        iUpdateStatusUseCase = new UpdateStatusOrderUseCase(
                iOrderMapper,
                iSearchUniqueRepositoryPort,
                iProductRepositoryPort,
                iSendEmailPort,
                iSendRequestPaymentPort,
                iSearchUniqueCustomerRepositoryPort
        );
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        closeable.close();
    }

    @Test
    void updateStatus() {

        //## Mock - Objects
        var orderDomain = OrderModelMock.orderModel(OrderStatusEnumDomain.NEW);

        //## Mocks
        when(iSearchUniqueRepositoryPort.findById(Mockito.anyString())).thenReturn(Optional.of(orderDomain));

        //## When
        final var orderDto = iUpdateStatusUseCase.updateStatus(orderDomain.getId(), orderDomain.getStatus().key);

        //## Then
        verify(iOrderMapper, times(1)).toDto(any());
        verify(iSearchUniqueRepositoryPort, times(1)).findById(Mockito.anyString());

        final var orderDtoExpected = iOrderMapper.toDto(orderDomain);
        assertThat(orderDto).usingRecursiveComparison()
                .isEqualTo(orderDtoExpected);

    }

    @Test
    void updateStatusOrderNotFound() {

        //## Mock - Objects
        var orderDomain = OrderDomainMock.getOrderDomain(OrderStatusEnumDomain.NEW);

        //## Mocks
        when(iSearchUniqueRepositoryPort.findById(Mockito.anyString())).thenReturn(Optional.empty());

        //## When
        final var exceptions = assertThrows(
                ElementNotFoundException.class,
                () -> iUpdateStatusUseCase.updateStatus(orderDomain.getId(), orderDomain.getStatus().key)
        );

        //## Then
        verify(iSearchUniqueRepositoryPort, times(1)).findById(Mockito.anyString());
        assertEquals(String.format("Order [%s] not found", orderDomain.getId()), exceptions.getMessage());
    }

    @Test
    void updateStatusChangeStatusToReceived() {

        //## Mock - Objects
        var orderDomain = OrderModelMock.orderModel(OrderStatusEnumDomain.WAITING_PAYMENT);
        final var paymentDomain = PaymentModelMock.getPaymentStatusCompletedMock();

        //## Mocks
        when(iSearchUniqueRepositoryPort.findById(Mockito.anyString())).thenReturn(Optional.of(orderDomain));
        when(iSendRequestPaymentPort.sendRequest(Mockito.any(PaymentFilterDto.class))).thenReturn(List.of(paymentDomain));

        //## When
        final var exception = assertThrows(ElementNotFoundException.class, () -> iUpdateStatusUseCase.updateStatus(orderDomain.getId(), OrderStatusEnumDomain.RECEIVED.key));

        //## Then
        assertEquals(exception.getMessage(), String.format("Order [%s] needs a payment request or Payment is PENDING", orderDomain.getId()));
    }

    @Test
    void updateStatusChangeStatusToReceivedButPaymentStatusPending() {

        //## Mock - Objects
        var orderDomain = OrderModelMock.orderModel(OrderStatusEnumDomain.WAITING_PAYMENT);

        //## Mocks
        when(iSearchUniqueRepositoryPort.findById(Mockito.anyString())).thenReturn(Optional.of(orderDomain));
        when(iSendRequestPaymentPort.sendRequest(Mockito.any(PaymentFilterDto.class))).thenReturn(null);

        //## When
        final var exceptions = assertThrows(
                ElementNotFoundException.class,
                () -> iUpdateStatusUseCase.updateStatus(orderDomain.getId(), OrderStatusEnumDomain.RECEIVED.key)
        );

        //## Then
        verify(iSearchUniqueRepositoryPort, times(1)).findById(Mockito.anyString());
        verify(iSendRequestPaymentPort, times(1)).sendRequest(Mockito.any(PaymentFilterDto.class));
        assertEquals(
                String.format("Order [%s] needs a payment request or Payment is PENDING",
                        orderDomain.getId()), exceptions.getMessage()
        );
    }

    @Test
    void updateStatusChangeStatusToReady() {

        //## Mock - Objects
        var orderDomain = OrderModelMock.orderModel(OrderStatusEnumDomain.IN_PREPARATION);
        var orderDomainReady = OrderModelMock.orderModel(OrderStatusEnumDomain.READY);
        var customerModel = CustomerModelMock.getMock();

        //## Mocks
        when(iSearchUniqueRepositoryPort.findById(Mockito.anyString())).thenReturn(Optional.of(orderDomain));
        when(iProductRepositoryPort.updateItem(Mockito.any(OrderModel.class))).thenReturn(orderDomainReady);
        when(iSearchUniqueCustomerRepositoryPort.findById(Mockito.anyString())).thenReturn(Optional.of(customerModel));

        //## When
        final var orderDto = iUpdateStatusUseCase.updateStatus(orderDomain.getId(), OrderStatusEnumDomain.READY.key);

        //## Then
        verify(iOrderMapper, times(1)).toDto(any());
        verify(iSearchUniqueRepositoryPort, times(1)).findById(Mockito.anyString());
        verify(iSendRequestPaymentPort, times(0)).sendRequest(Mockito.any(PaymentFilterDto.class));
        verify(iProductRepositoryPort, times(1)).updateItem(Mockito.any(OrderModel.class));
        verify(iSendEmailPort, times(1)).sendEmail(Mockito.any(EmailNotificationDto.class));

        final var orderDtoExpected = iOrderMapper.toDto(orderDomainReady);
        assertThat(orderDto).usingRecursiveComparison()
                .isEqualTo(orderDtoExpected);

    }
}