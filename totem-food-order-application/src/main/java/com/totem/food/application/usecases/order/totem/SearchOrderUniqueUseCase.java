package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderFilterDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.out.persistence.commons.ISearchRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.usecases.annotations.UseCase;
import com.totem.food.application.usecases.commons.ISearchUniqueUseCase;
import com.totem.food.application.usecases.commons.ISearchUseCase;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.IN_PREPARATION;
import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.READY;
import static com.totem.food.domain.order.enums.OrderStatusEnumDomain.RECEIVED;

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
