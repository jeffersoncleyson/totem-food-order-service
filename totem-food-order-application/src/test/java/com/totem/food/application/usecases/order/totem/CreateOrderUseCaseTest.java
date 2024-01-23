package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.exceptions.InvalidInput;
import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import com.totem.food.application.ports.in.dtos.order.totem.OrderCreateDto;
import com.totem.food.application.ports.in.dtos.product.ProductFilterDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.in.mappers.product.IProductMapper;
import com.totem.food.application.ports.out.persistence.commons.ICreateRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.ports.out.persistence.product.ProductModel;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.domain.order.totem.OrderDomain;
import lombok.SneakyThrows;
import mock.domain.ProductDomainMock;
import mock.models.ProductModelMock;
import mock.ports.in.dto.CustomerResponseMock;
import mock.ports.in.dto.OrderCreateDtoMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.NEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Spy
    private IOrderMapper iOrderMapper = Mappers.getMapper(IOrderMapper.class);

    @Spy
    private IProductMapper iProductMapper = Mappers.getMapper(IProductMapper.class);

    @Mock
    private ICreateRepositoryPort<OrderModel> iCreateRepositoryPort;

    @Mock
    private ISendRequestPort<String, Optional<CustomerResponse>> iSearchUniqueCustomerRepositoryPort;

    @Mock
    private ISearchRepositoryPort<ProductFilterDto, List<ProductModel>> iSearchProductRepositoryPort;

    private CreateOrderUseCase createOrderUseCase;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        createOrderUseCase = new CreateOrderUseCase(iOrderMapper, iProductMapper, iCreateRepositoryPort, iSearchUniqueCustomerRepositoryPort, iSearchProductRepositoryPort);
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        closeable.close();
    }

    @Test
    void createItem() {

        //## Mock - Objects and Values
        var textGeneric = UUID.randomUUID().toString();
        var orderCreateDto = OrderCreateDtoMock.getMock("1");
        var customerResponse = CustomerResponseMock.getMock("1");
        var productModel = ProductModelMock.getMock("1");
        var productDomain = ProductDomainMock.getMock("1");
        var orderModel = OrderModel.builder()
                .customer("123")
                .status(NEW)
                .products(List.of(productDomain))
                .build();

        //## Given
        when(iSearchUniqueCustomerRepositoryPort.sendRequest(anyString())).thenReturn(Optional.of(customerResponse));
        when(iSearchProductRepositoryPort.findAll(any(ProductFilterDto.class))).thenReturn(List.of(productModel));
        when(iProductMapper.toDomain(any(ProductModel.class))).thenReturn(productDomain);
        when(iCreateRepositoryPort.saveItem(any(OrderModel.class))).thenReturn(orderModel);

        //## When
        var result = createOrderUseCase.createItem(orderCreateDto, "1");

        //## Then
        assertNotNull(result);
        assertEquals(NEW.toString(), result.getStatus());
        assertThat(result.getProducts()).hasSize(1);

        verify(iOrderMapper, times(1)).toModel(any(OrderDomain.class));
        verify(iProductMapper, times(1)).toDomain(any(ProductModel.class));

    }

    @Test
    void createItemWhenProdcutNotEqualsSizeCollectionAndElementNotFoundException() {

        //## Mock - Objects and Values
        var textGeneric = UUID.randomUUID().toString();
        var orderCreateDto = OrderCreateDtoMock.getMock(textGeneric);
        var customerResponse = CustomerResponseMock.getMock(textGeneric);
        var productModel = ProductModelMock.getMock(textGeneric);

        //## Given
        when(iSearchUniqueCustomerRepositoryPort.sendRequest(anyString())).thenReturn(Optional.of(customerResponse));
        when(iSearchProductRepositoryPort.findAll(any(ProductFilterDto.class))).thenReturn(List.of(productModel, productModel));

        //## When
        var exception = assertThrows(ElementNotFoundException.class,
                () -> createOrderUseCase.createItem(orderCreateDto, textGeneric));

        //## Then
        assertEquals("Products [[" + textGeneric + "]] some products are invalid", exception.getMessage());

    }

    @Test
    void createItemWhenElementNotFoundException() {

        //## Mock - Objects and Values
        var textGeneric = UUID.randomUUID().toString();
        var orderCreateDto = OrderCreateDtoMock.getMock(textGeneric);

        //## Given
        when(iSearchUniqueCustomerRepositoryPort.sendRequest(textGeneric)).thenReturn(Optional.empty());

        //## When
        var exception = assertThrows(ElementNotFoundException.class,
                () -> createOrderUseCase.createItem(orderCreateDto, textGeneric));

        //## Then
        assertEquals("Customer [" + textGeneric + "] not found", exception.getMessage());
    }

    @Test
    void createItemWhenExceptionInvalidInput() {

        //## Mock - Objects and Values
        var orderCreateDto = new OrderCreateDto();
        orderCreateDto.isOrderValid();

        String customerIdentifier = UUID.randomUUID().toString();

        //## Given - When
        var exception = assertThrows(InvalidInput.class, () -> createOrderUseCase.createItem(orderCreateDto, customerIdentifier));

        //## Then
        assertEquals("Order is invalid", exception.getMessage());
    }

}