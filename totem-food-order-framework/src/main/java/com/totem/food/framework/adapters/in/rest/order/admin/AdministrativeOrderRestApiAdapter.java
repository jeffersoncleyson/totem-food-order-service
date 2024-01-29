package com.totem.food.framework.adapters.in.rest.order.admin;

import com.totem.food.application.ports.in.dtos.order.admin.OrderAdminDto;
import com.totem.food.application.ports.in.dtos.order.admin.OrderAdminFilterDto;
import com.totem.food.application.ports.in.rest.IRemoveRestApiPort;
import com.totem.food.application.ports.in.rest.ISearchRestApiPort;
import com.totem.food.application.usecases.commons.IDeleteUseCase;
import com.totem.food.application.usecases.commons.ISearchUseCase;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.totem.food.framework.adapters.in.rest.constants.Routes.ADM_ORDER;
import static com.totem.food.framework.adapters.in.rest.constants.Routes.API_VERSION_1;
import static com.totem.food.framework.adapters.in.rest.constants.Routes.ORDER_ID;

@RestController
@RequestMapping(value = API_VERSION_1 + ADM_ORDER)
@AllArgsConstructor
public class AdministrativeOrderRestApiAdapter implements
        ISearchRestApiPort<OrderAdminFilterDto, ResponseEntity<List<OrderAdminDto>>>,
        IRemoveRestApiPort<String, ResponseEntity<Void>> {

    private final ISearchUseCase<OrderAdminFilterDto, List<OrderAdminDto>> iSearchOrderUseCase;
    private final IDeleteUseCase<String, OrderAdminDto> iDeleteUseCase;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Override
    public ResponseEntity<List<OrderAdminDto>> listAll(OrderAdminFilterDto filter) {
        final var orders = iSearchOrderUseCase.items(filter);
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    @Override
    @DeleteMapping(ORDER_ID)
    public ResponseEntity<Void> deleteById(@PathVariable String orderId) {
        iDeleteUseCase.removeItem(orderId);
        return ResponseEntity.noContent().build();
    }
}
