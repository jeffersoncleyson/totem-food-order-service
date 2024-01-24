package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.usecases.annotations.UseCase;
import com.totem.food.application.usecases.commons.ISearchUniqueUseCase;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
@UseCase
public class SearchOrderUniqueUseCase implements ISearchUniqueUseCase<String, Optional<OrderDto>> {

    private final IOrderMapper iOrderMapper;
    private final ISearchUniqueRepositoryPort<Optional<OrderModel>> iSearchUniqueRepositoryPort;

    @Override
    public Optional<OrderDto> item(String id) {
        return iSearchUniqueRepositoryPort.findById(id)
                .map(iOrderMapper::toDto);
    }
}
