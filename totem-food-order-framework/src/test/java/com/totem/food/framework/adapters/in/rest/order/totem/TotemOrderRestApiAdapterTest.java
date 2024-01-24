package com.totem.food.framework.adapters.in.rest.order.totem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.totem.food.application.ports.in.dtos.context.XUserIdentifierContextDto;
import com.totem.food.application.ports.in.dtos.order.totem.ItemQuantityDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderCreateDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderFilterDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderUpdateDto;
import com.totem.food.application.usecases.commons.IContextUseCase;
import com.totem.food.application.usecases.commons.ICreateWithIdentifierUseCase;
import com.totem.food.application.usecases.commons.ISearchUniqueUseCase;
import com.totem.food.application.usecases.commons.ISearchUseCase;
import com.totem.food.application.usecases.commons.IUpdateStatusUseCase;
import com.totem.food.application.usecases.commons.IUpdateUseCase;
import com.totem.food.framework.test.utils.TestUtils;
import lombok.SneakyThrows;
import mocks.dtos.OrderDtoMock;
import mocks.dtos.OrderFilterDtoMock;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.NEW;
import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.WAITING_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class TotemOrderRestApiAdapterTest {

    private final String ENDPOINT = "/v1/totem/order";
    private final String ENDPOINT_ORDER_ID = "/v1/totem/order/{orderId}";
    private final String ENDPOINT_ORDER_ID_STATUS_NAME = "/v1/totem/order/{orderId}/status/{statusName}";

    private MockMvc mockMvc;

    private AutoCloseable autoCloseable;

    @Mock
    private ICreateWithIdentifierUseCase<OrderCreateDto, OrderDto> iCreateUseCase;

    @Mock
    private ISearchUseCase<OrderFilterDto, List<OrderDto>> iSearchProductUseCase;

    @Mock
    private IUpdateUseCase<OrderUpdateDto, OrderDto> iUpdateUseCase;

    @Mock
    private IUpdateStatusUseCase<OrderDto> iUpdateStatusUseCase;

    @Mock
    private IContextUseCase<XUserIdentifierContextDto, String> iContextUseCase;

    @Mock
    private ISearchUniqueUseCase<String, Optional<OrderDto>> iSearchUniqueUseCase;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        var totemOrderRestApiAdapter = new TotemOrderRestApiAdapter(
                iCreateUseCase,
                iSearchProductUseCase,
                iUpdateUseCase,
                iUpdateStatusUseCase,
                iContextUseCase,
                iSearchUniqueUseCase
        );
        mockMvc = MockMvcBuilders.standaloneSetup(totemOrderRestApiAdapter).build();
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        autoCloseable.close();
    }

    @ParameterizedTest
    @ValueSource(strings = ENDPOINT)
    void create(String endpoint) throws Exception {

        //## Mock - Object
        final var customerId = "123";
        var orderCreateDto = new OrderCreateDto();
        orderCreateDto.setProducts(List.of(new ItemQuantityDto(1, "produto")));

        var orderDto = OrderDtoMock.getMock(NEW.toString());

        when(iContextUseCase.getContext()).thenReturn(customerId);

        //## Given
        when(iCreateUseCase.createItem(any(OrderCreateDto.class), eq(customerId))).thenReturn(orderDto);

        final var jsonOpt = TestUtils.toJSON(orderCreateDto);
        final var json = jsonOpt.orElseThrow();
        final var httpServletRequest = post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final var responseJson = resultActions.andReturn().getResponse().getContentAsString();
        final var orderDtoResponseOpt = TestUtils.toObject(responseJson, OrderDto.class);
        final var orderDtoResponse = orderDtoResponseOpt.orElseThrow();

        assertThat(orderDtoResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(orderDto);

        verify(iCreateUseCase, times(1)).createItem(any(OrderCreateDto.class), eq(customerId));
    }


    @ParameterizedTest
    @ValueSource(strings = ENDPOINT)
    void listAll(String endpoint) throws Exception {

        //## Mock - Object
        var orderDto = OrderDtoMock.getMock(NEW.toString());

        var orderFilterDto = OrderFilterDtoMock.getStatusNew();

        //## Given
        when(iSearchProductUseCase.items(any(OrderFilterDto.class))).thenReturn(Collections.singletonList(orderDto));

        final var httpServletRequest = get(endpoint)
                .queryParam("customerId", orderFilterDto.getCpf());

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final var responseJson = resultActions.andReturn().getResponse().getContentAsString();
        final var orderDtoResponseOpt = TestUtils.toTypeReferenceObject(responseJson, new TypeReference<List<OrderDto>>() {
        });
        final var orderDtoResponse = orderDtoResponseOpt.orElseThrow();

        assertThat(orderDtoResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isNotNull();

        verify(iSearchProductUseCase, times(1)).items(any(OrderFilterDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = ENDPOINT_ORDER_ID)
    void update(String endpoint) throws Exception {

        //## Mock - Object
        var orderDto = OrderDtoMock.getMock(NEW.toString());

        //## Given
        when(iUpdateUseCase.updateItem(any(OrderUpdateDto.class), anyString())).thenReturn(orderDto);

        final var jsonOpt = TestUtils.toJSON(orderDto);
        final var json = jsonOpt.orElseThrow();
        final var httpServletRequest = put(endpoint, orderDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final var responseJson = resultActions.andReturn().getResponse().getContentAsString();
        final var orderDtoResponseOpt = TestUtils.toObject(responseJson, OrderDto.class);
        final var orderDtoResponse = orderDtoResponseOpt.orElseThrow();

        AssertionsForClassTypes.assertThat(orderDto)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(orderDtoResponse);

        verify(iUpdateUseCase, times(1))
                .updateItem(Mockito.any(OrderUpdateDto.class), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = ENDPOINT_ORDER_ID)
    void updateWhenNotFound(String endpoint) throws Exception {

        //## Mock - Object
        var orderDto = OrderDtoMock.getMock(NEW.toString());

        //## Given
        when(iUpdateUseCase.updateItem(any(OrderUpdateDto.class), anyString())).thenReturn(null);

        final var json = TestUtils.toJSON(orderDto).orElseThrow();
        final var httpServletRequest = put(endpoint, orderDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print()).andExpect(status().isNotFound());

        verify(iUpdateUseCase, times(1))
                .updateItem(Mockito.any(OrderUpdateDto.class), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = ENDPOINT_ORDER_ID_STATUS_NAME)
    void updateStatus(String endpoint) throws Exception {

        //## Mock - Object
        var orderDto = OrderDtoMock.getMock(WAITING_PAYMENT.toString());

        //## Given
        when(iUpdateStatusUseCase.updateStatus(anyString(), anyString())).thenReturn(orderDto);

        final var json = TestUtils.toJSON(orderDto).orElseThrow();
        final var httpServletRequest = put(endpoint, orderDto.getId(), NEW.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final var responseJson = resultActions.andReturn().getResponse().getContentAsString();
        final var orderDtoResponseOpt = TestUtils.toObject(responseJson, OrderDto.class);
        final var orderDtoResponse = orderDtoResponseOpt.orElseThrow();

        AssertionsForClassTypes.assertThat(orderDtoResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isNotNull();

        assertEquals(orderDtoResponse.getStatus(), WAITING_PAYMENT.toString());

        verify(iUpdateStatusUseCase, times(1)).updateStatus(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = ENDPOINT_ORDER_ID)
    void getById(String endpoint) throws Exception {

        //## Mock - Object
        String orderId = "1";
        var orderDto = OrderDtoMock.getMock(NEW.toString());

        //## Given
        when(iSearchUniqueUseCase.item(anyString())).thenReturn(Optional.of(orderDto));

        final var httpServletRequest = get(endpoint, orderId);

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final var responseJson = resultActions.andReturn().getResponse().getContentAsString();
        final var orderDtoResponseOpt = TestUtils.toObject(responseJson, OrderDto.class);
        final var orderDtoResponse = orderDtoResponseOpt.orElseThrow();

        assertThat(orderDtoResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(orderDto);

        verify(iSearchUniqueUseCase, times(1)).item(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = ENDPOINT_ORDER_ID)
    void getByIdWhenNotContent(String endpoint) throws Exception {

        //## Mock - Object
        String orderId = UUID.randomUUID().toString();

        //## Given
        when(iSearchUniqueUseCase.item(anyString())).thenReturn(Optional.empty());

        final var httpServletRequest = get(endpoint, orderId);

        //### When
        final var resultActions = mockMvc.perform(httpServletRequest);

        //### Then
        resultActions.andDo(print()).andExpect(status().isNoContent());

        verify(iSearchUniqueUseCase, times(1)).item(anyString());
    }

}