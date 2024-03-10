package com.totem.food.application.ports.out.dtos;

import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentNotificationDto {

    private OrderModel order;
}
