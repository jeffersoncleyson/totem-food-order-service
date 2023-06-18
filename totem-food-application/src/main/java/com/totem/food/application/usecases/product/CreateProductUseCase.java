package com.totem.food.application.usecases.product;

import com.totem.food.application.ports.in.dtos.product.ProductCreateDto;
import com.totem.food.application.ports.in.dtos.product.ProductDto;
import com.totem.food.application.ports.in.mappers.product.IProductMapper;
import com.totem.food.application.ports.out.persistence.commons.ICreateRepositoryPort;
import com.totem.food.application.usecases.commons.ICreateUseCase;
import com.totem.food.domain.product.ProductDomain;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CreateProductUseCase implements ICreateUseCase<ProductCreateDto, ProductDto> {

    private final IProductMapper iProductMapper;
    private final ICreateRepositoryPort<ProductDomain> iProductRepositoryPort;

    public ProductDto createItem(ProductCreateDto item){
        final var domain = iProductMapper.toDomain(item);
        domain.fillDates();
        final var domainSaved = iProductRepositoryPort.saveItem(domain);
        return iProductMapper.toDto(domainSaved);
    }

}