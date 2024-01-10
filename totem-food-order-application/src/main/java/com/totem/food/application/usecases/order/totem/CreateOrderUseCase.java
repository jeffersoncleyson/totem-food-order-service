package com.totem.food.application.usecases.order.totem;

import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.exceptions.InvalidInput;
import com.totem.food.application.ports.in.dtos.customer.CustomerResponse;
import com.totem.food.application.ports.in.dtos.order.totem.ItemQuantityDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderCreateDto;
import com.totem.food.application.ports.in.dtos.order.totem.OrderDto;
import com.totem.food.application.ports.in.dtos.product.ProductFilterDto;
import com.totem.food.application.ports.in.mappers.order.totem.IOrderMapper;
import com.totem.food.application.ports.in.mappers.product.IProductMapper;
import com.totem.food.application.ports.out.persistence.commons.ICreateRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchRepositoryPort;
import com.totem.food.application.ports.out.persistence.order.totem.OrderModel;
import com.totem.food.application.ports.out.persistence.product.ProductModel;
import com.totem.food.application.ports.out.web.ISendRequestPort;
import com.totem.food.application.usecases.annotations.UseCase;
import com.totem.food.application.usecases.commons.ICreateWithIdentifierUseCase;
import com.totem.food.domain.order.enums.OrderStatusEnumDomain;
import com.totem.food.domain.order.totem.OrderDomain;
import com.totem.food.domain.product.ProductDomain;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@UseCase
public class CreateOrderUseCase implements ICreateWithIdentifierUseCase<OrderCreateDto, OrderDto> {

    private final IOrderMapper iOrderMapper;
    private final IProductMapper iProductMapper;
    private final ICreateRepositoryPort<OrderModel> iCreateRepositoryPort;
    private final ISendRequestPort<String, Optional<CustomerResponse>> iSearchUniqueCustomerRepositoryPort;
    private final ISearchRepositoryPort<ProductFilterDto, List<ProductModel>> iSearchProductRepositoryPort;

    @Override
    public OrderDto createItem(OrderCreateDto item, String customerIdentifier) {

        if(!item.isOrderValid()) {
            throw new InvalidInput("Order is invalid");
        }

        final var domain = new OrderDomain();

        setCustomer(customerIdentifier, domain);
        setProductsToDomain(item, domain);

        domain.updateOrderStatus(OrderStatusEnumDomain.NEW);
        domain.calculatePrice();
        domain.fillDates();

        final var model = iOrderMapper.toModel(domain);
        final var domainSaved = iCreateRepositoryPort.saveItem(model);
        return iOrderMapper.toDto(domainSaved);
    }

    private void setCustomer(String identifier, OrderDomain domain) {

        if(StringUtils.isNotEmpty(identifier)) {
            final var customerResponse = iSearchUniqueCustomerRepositoryPort.sendRequest(identifier)
                    .orElseThrow(() -> new ElementNotFoundException(String.format("Customer [%s] not found", identifier)));
            domain.setCustomer(customerResponse.getCpf());
        }
    }

    private void setProductsToDomain(OrderCreateDto item, OrderDomain domain) {
        if(CollectionUtils.isNotEmpty(item.getProducts())){

            final var ids = item.getProducts().stream().map(ItemQuantityDto::getId).toList();
            final var productFilterDto = ProductFilterDto.builder().ids(ids).build();
            final var products = iSearchProductRepositoryPort.findAll(productFilterDto);

            if(CollectionUtils.size(item.getProducts()) != CollectionUtils.size(products)){
                throw new ElementNotFoundException(String.format("Products [%s] some products are invalid", ids));
            }

            final var productsDomainToAdd = getProductDomains(item, products);
            domain.setProducts(productsDomainToAdd);
        }
    }

    // TODO - Refatorar este método
    private List<ProductDomain> getProductDomains(OrderCreateDto item, List<ProductModel> products) {
        final var productDomainMap = products.stream().collect(Collectors.toMap(ProductModel::getId, iProductMapper::toDomain));
        final var productsDomainToAdd = new ArrayList<ProductDomain>();

        for (ItemQuantityDto itemX : item.getProducts()) {
            for (int i = 0; i < itemX.getQtd(); i++) {
                productsDomainToAdd.add(productDomainMap.get(itemX.getId()));
            }
        }
        return productsDomainToAdd;
    }
}