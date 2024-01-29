package mock.models;

import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import com.totem.food.domain.product.ProductDomain;

import java.time.ZonedDateTime;
import java.util.List;

public class OrderModelMock {


    public static OrderModel orderModel(OrderStatusEnumDomain orderStatusEnumDomain) {
        return OrderModel.builder()
                .id("1")
                //@todo - setar customerId
                // .customer(CustomerDomainMock.getMock())
                .products(List.of(new ProductDomain()))
                .price(49.99)
                .status(orderStatusEnumDomain)
                .modifiedAt(ZonedDateTime.parse("2023-04-03T13:28:20.606-03:00"))
                .createAt(ZonedDateTime.parse("2023-04-03T13:28:20.606-03:00"))
                .receivedAt(ZonedDateTime.parse("2023-04-03T13:28:20.606-03:00"))
                .build();
    }
}
