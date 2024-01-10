package com.totem.food.application.usecases.category;

import com.totem.food.application.exceptions.ElementNotFoundException;
import com.totem.food.application.ports.in.dtos.category.CategoryCreateDto;
import com.totem.food.application.ports.in.dtos.category.CategoryDto;
import com.totem.food.application.ports.in.dtos.category.CategoryFilterDto;
import com.totem.food.application.ports.in.mappers.category.ICategoryMapper;
import com.totem.food.application.ports.out.persistence.category.CategoryModel;
import com.totem.food.application.ports.out.persistence.commons.ISearchRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.ISearchUniqueRepositoryPort;
import com.totem.food.application.ports.out.persistence.commons.IUpdateRepositoryPort;
import com.totem.food.application.usecases.commons.IUpdateUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Spy
    private ICategoryMapper iCategoryMapper = Mappers.getMapper(ICategoryMapper.class);

    @Mock
    private ISearchRepositoryPort<CategoryFilterDto, CategoryModel> iSearchRepositoryPort;


    @Mock
    private IUpdateRepositoryPort<CategoryModel> iUpdateRepositoryPort;

    @Mock
    private ISearchUniqueRepositoryPort<Optional<CategoryModel>> iSearchUniqueRepositoryPort;

    private IUpdateUseCase<CategoryCreateDto, CategoryDto> iUpdateUseCase;


    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
        this.iUpdateUseCase = new UpdateCategoryUseCase(iCategoryMapper, iUpdateRepositoryPort, iSearchUniqueRepositoryPort);
    }

    @Test
    void updateItemWhenCategoryExist() {

        //## Given
        final var categoryDomain = new CategoryModel("123", "Name", ZonedDateTime.now(ZoneOffset.UTC), ZonedDateTime.now(ZoneOffset.UTC));
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.of(categoryDomain));
        when(iUpdateRepositoryPort.updateItem(any(CategoryModel.class))).thenReturn(categoryDomain);

        //## When
        final var categoryCreateDto = new CategoryCreateDto("name");
        final var categoryDto = iUpdateUseCase.updateItem(categoryCreateDto, anyString());

        //## Then
        assertEquals(categoryDomain.getName(), categoryDto.getName());
        verify(iCategoryMapper).toDto(any());

    }

    @Test
    void createItemWhenCategoryNotFound() {

        //## Given
        final var categoryCreateDto = new CategoryCreateDto("Name");
        when(iSearchUniqueRepositoryPort.findById(anyString())).thenReturn(Optional.empty());

        //## Then
        assertThrows(ElementNotFoundException.class, () -> iUpdateUseCase.updateItem(categoryCreateDto, "123"));

    }
}