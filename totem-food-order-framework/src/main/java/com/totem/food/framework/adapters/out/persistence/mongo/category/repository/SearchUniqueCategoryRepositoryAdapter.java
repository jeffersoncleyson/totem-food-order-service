package com.totem.food.framework.adapters.out.persistence.mongo.category.repository;

import com.totem.food.application.ports.out.persistence.category.CategoryModel;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.framework.adapters.out.persistence.mongo.category.entity.CategoryEntity;
import com.totem.food.framework.adapters.out.persistence.mongo.category.mapper.ICategoryEntityMapper;
import com.totem.food.framework.adapters.out.persistence.mongo.commons.BaseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@AllArgsConstructor
@Component
public class SearchUniqueCategoryRepositoryAdapter implements ISearchUniqueRepositoryPort<Optional<CategoryModel>> {

    @Repository
    protected interface CategoryRepositoryMongoDB extends BaseRepository<CategoryEntity, String> {

    }

    private final CategoryRepositoryMongoDB repository;
    private final ICategoryEntityMapper iCategoryEntityMapper;

    @Override
    public Optional<CategoryModel> findById(String id) {
        return repository.findById(id).map(iCategoryEntityMapper::toModel);
    }

}
