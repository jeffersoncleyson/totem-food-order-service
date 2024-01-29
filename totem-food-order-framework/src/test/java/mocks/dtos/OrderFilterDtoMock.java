package mocks.dtos;

import com.totem.food.application.ports.in.dtos.order.totem.OrderFilterDto;

import java.util.Set;

import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.NEW;

public class OrderFilterDtoMock {

    public static OrderFilterDto getStatusNew() {
        return OrderFilterDto.builder()
                .cpf("123")
                .orderId("1")
                .status(Set.of(NEW.toString()))
                .build();
    }
}
