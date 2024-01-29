package mocks.dtos;

import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.dtos.product.ProductDto;

import java.util.List;

public class OrderDtoMock {

    public static OrderDto getMock(String status) {
        var orderDto = new OrderDto();
        orderDto.setId("1");
        orderDto.setCustomer("123");
        orderDto.setProducts(List.of(ProductDto.builder().id("1").build()));
        orderDto.setStatus(status);
        orderDto.setPrice(25.0);
        return orderDto;
    }
}
