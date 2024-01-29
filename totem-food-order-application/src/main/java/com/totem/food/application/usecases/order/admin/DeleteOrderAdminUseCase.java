package com.totem.food.application.usecases.order.admin;

import com.totem.food.application.ports.in.dtos.order.admin.OrderAdminDto;
import com.totem.food.application.ports.out.persistence.commons.IRemoveRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.usecases.annotations.UseCase;
import com.totem.food.application.usecases.commons.IDeleteUseCase;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@UseCase
public class DeleteOrderAdminUseCase implements IDeleteUseCase<String, OrderAdminDto> {

    private final IRemoveRepositoryPort<OrderModel> iSearchUniqueRepositoryPort;

    @Override
    public OrderAdminDto removeItem(String id) {
        iSearchUniqueRepositoryPort.removeItem(id);
        return null;
    }
}
