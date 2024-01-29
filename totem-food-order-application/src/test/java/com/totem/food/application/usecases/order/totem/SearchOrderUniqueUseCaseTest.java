package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import lombok.SneakyThrows;
import mock.models.OrderModelMock;
import mock.ports.in.dto.OrderDtoMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchOrderUniqueUseCaseTest {

    @Spy
    private IOrderMapper iOrderMapper = Mappers.getMapper(IOrderMapper.class);

    @Mock
    private ISearchUniqueRepositoryPort<Optional<OrderModel>> iSearchUniqueRepositoryPort;

    private SearchOrderUniqueUseCase searchOrderUniqueUseCase;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        searchOrderUniqueUseCase = new SearchOrderUniqueUseCase(iOrderMapper, iSearchUniqueRepositoryPort);
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        closeable.close();
    }

    @Test
    void item() {

        //## Mock - Objects and Value
        var orderDto = OrderDtoMock.getMock(OrderStatusEnumDomain.READY.toString());
        var orderModel = OrderModelMock.orderModel(OrderStatusEnumDomain.IN_PREPARATION);

        //## Given
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.ofNullable(orderModel));
        when(iOrderMapper.toDto(any(OrderModel.class))).thenReturn(orderDto);

        //## When
        var result = searchOrderUniqueUseCase.item("1");

        //## Then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(Optional.of(orderDto));

    }
}