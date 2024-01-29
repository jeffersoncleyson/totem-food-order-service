package com.totem.food.application.usecases.product;

import com.totem.food.application.exceptions.ElementExistsException;
import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.ports.in.dtos.product.ProductCreateDto;
import com.totem.food.application.ports.in.dtos.product.ProductDto;
import com.totem.food.application.ports.in.dtos.product.ProductFilterDto;
import com.totem.food.application.ports.in.mappers.category.ICategoryMapper;
import com.totem.food.application.ports.in.mappers.product.IProductMapper;
import com.totem.food.application.ports.out.persistence.category.CategoryModel;
import com.totem.food.application.ports.out.persistence.commons.ICreateRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.product.ProductModel;
import com.totem.food.application.usecases.annotations.UseCase;
import com.totem.food.application.usecases.commons.ICreateUseCase;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@UseCase
public class CreateProductUseCase implements ICreateUseCase<ProductCreateDto, ProductDto> {

    private final IProductMapper iProductMapper;
    private final ICategoryMapper iCategoryMapper;
    private final ICreateRepositoryPort<ProductModel> iProductRepositoryPort;
    private final ISearchUniqueRepositoryPort<Optional<CategoryModel>> iSearchRepositoryPort;
    private final ISearchRepositoryPort<ProductFilterDto, List<ProductModel>> iSearchProductRepositoryPort;


    public ProductDto createItem(ProductCreateDto item) {
        final var domain = iProductMapper.toDomain(item);

        verifyProductExists(item);

        final var categoryModel = iSearchRepositoryPort.findById(item.getCategory())
                .orElseThrow(() -> new ElementNotFoundException(String.format("Category [%s] not found", item.getCategory())));
        final var categoryDomain = iCategoryMapper.toDomain(categoryModel);
        domain.setCategory(categoryDomain);
        domain.fillDates();
        final var model = iProductMapper.toModel(domain);
        final var domainSaved = iProductRepositoryPort.saveItem(model);
        return iProductMapper.toDto(domainSaved);
    }

    private void verifyProductExists(ProductCreateDto item) {
        var filter = ProductFilterDto.builder().name(item.getName()).build();

        var productModels = iSearchProductRepositoryPort.findAll(filter);

        if (!productModels.isEmpty()) {
            throw new ElementExistsException(String.format("Product with name [%s] already exists", item.getName()));
        }
    }

}