package com.totem.food.framework.adapters.in.rest.category;

import com.fasterxml.jackson.core.type.TypeReference;
import com.totem.food.application.ports.in.dtos.category.CategoryCreateDto;
import com.totem.food.application.ports.in.dtos.category.CategoryDto;
import com.totem.food.application.ports.in.dtos.category.CategoryFilterDto;
import com.totem.food.application.usecases.commons.ICreateUseCase;
import com.totem.food.application.usecases.commons.IDeleteUseCase;
import com.totem.food.application.usecases.commons.ISearchUniqueUseCase;
import com.totem.food.application.usecases.commons.ISearchUseCase;
import com.totem.food.application.usecases.commons.IUpdateUseCase;
import com.totem.food.domain.exceptions.ResourceNotFound;
import com.totem.food.framework.test.utils.TestUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static com.totem.food.framework.adapters.in.rest.constants.Routes.ADM_CATEGORY;
import static com.totem.food.framework.adapters.in.rest.constants.Routes.API_VERSION_1;
import static com.totem.food.framework.adapters.in.rest.constants.Routes.CATEGORY_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AdministrativeCategoriesRestApiAdapterTest {

    @Mock
    private ICreateUseCase<CategoryCreateDto, CategoryDto> iCreateCategoryUseCase;

    @Mock
    private ISearchUseCase<CategoryFilterDto, List<CategoryDto>> iSearchCategoryUseCase;

    @Mock
    private ISearchUniqueUseCase<String, CategoryDto> iSearchUniqueUseCase;

    @Mock
    private IDeleteUseCase<String, CategoryDto> iDeleteCategoryUseCase;

    @Mock
    private IUpdateUseCase<CategoryCreateDto, CategoryDto> iUpdateCategoryUseCase;

    private AdministrativeCategoriesRestApiAdapter administrativeCategoriesRestApiAdapter;
    private MockMvc mockMvc;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        this.administrativeCategoriesRestApiAdapter = new AdministrativeCategoriesRestApiAdapter(iCreateCategoryUseCase, iSearchCategoryUseCase, iSearchUniqueUseCase, iDeleteCategoryUseCase, iUpdateCategoryUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(administrativeCategoriesRestApiAdapter).build();
    }

    @SneakyThrows
    @AfterEach
    void tearDown() {
        closeable.close();
    }

    @ParameterizedTest
    @ValueSource(strings = API_VERSION_1 + ADM_CATEGORY)
    void testCreateCategory(String endpoint) throws Exception {

        //## Mocks - Objects and Values
        var categoryCreateDto = new CategoryCreateDto("Suco");
        var categoryDto = new CategoryDto("1", "Suco", ZonedDateTime.now(), ZonedDateTime.now());

        //## Given
        when(iCreateCategoryUseCase.createItem(any(CategoryCreateDto.class))).thenReturn(categoryDto);

        final String json = TestUtils.toJSON(categoryCreateDto).orElseThrow();
        final MockHttpServletRequestBuilder httpServletRequest = MockMvcRequestBuilders.post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //## When
        final ResultActions resultActions = mockMvc.perform(httpServletRequest);

        //## Then
        resultActions.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final String responseJson = resultActions.andReturn().getResponse().getContentAsString();

        final var response = TestUtils.toObject(responseJson, CategoryDto.class).orElseThrow();

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(categoryDto);

        verify(iCreateCategoryUseCase, times(1)).createItem(any(CategoryCreateDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = API_VERSION_1 + ADM_CATEGORY)
    void listAllCategoriesWhenBodyNotNull(String endpoint) throws Exception {

        //## Mocks - Objects and Values
        var filter = new CategoryFilterDto("Suco");

        var lanche = new CategoryDto("1", "Lanche", ZonedDateTime.now(), ZonedDateTime.now());
        var suco = new CategoryDto("2", "Suco", ZonedDateTime.now(), ZonedDateTime.now());
        List<CategoryDto> categories = List.of(lanche, suco);

        //## Given
        when(iSearchCategoryUseCase.items(any(CategoryFilterDto.class))).thenReturn(categories);

        final String json = TestUtils.toJSON(filter).orElseThrow();
        final MockHttpServletRequestBuilder httpServletRequest = MockMvcRequestBuilders.get(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //## When
        final ResultActions resultActions = mockMvc.perform(httpServletRequest);

        //## Then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final String responseJson = resultActions.andReturn().getResponse().getContentAsString();

        final var response = TestUtils.toTypeReferenceObject(responseJson, new TypeReference<List<CategoryDto>>() {
        }).orElseThrow();

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(categories);

        verify(iSearchCategoryUseCase, times(1)).items(any(CategoryFilterDto.class));
    }

    @ParameterizedTest
    @ValueSource(strings = API_VERSION_1 + ADM_CATEGORY)
    void listAllCategoriesWhenBodyNull(String endpoint) throws Exception {

        //## Mocks - Objects and Values
        var lanche = new CategoryDto("1", "Lanche", ZonedDateTime.now(), ZonedDateTime.now());
        var suco = new CategoryDto("2", "Suco", ZonedDateTime.now(), ZonedDateTime.now());
        List<CategoryDto> categories = List.of(lanche, suco);

        //## Given
        when(iSearchCategoryUseCase.items(null)).thenReturn(categories);

        final MockHttpServletRequestBuilder httpServletRequest = MockMvcRequestBuilders.get(endpoint);

        //## When
        final ResultActions resultActions = mockMvc.perform(httpServletRequest);

        //## Then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final String responseJson = resultActions.andReturn().getResponse().getContentAsString();

        final var response = TestUtils.toTypeReferenceObject(responseJson, new TypeReference<List<CategoryDto>>() {
        }).orElseThrow();

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(categories);

        verify(iSearchCategoryUseCase, times(1)).items(null);
    }

    @ParameterizedTest
    @ValueSource(strings = API_VERSION_1 + ADM_CATEGORY + CATEGORY_ID)
    void getCategoryByID(String endpoint) throws Exception {

        //## Mocks - Objects and Values
        var categoryDto = new CategoryDto("1", "Suco", ZonedDateTime.now(), ZonedDateTime.now());

        //## Given
        when(iSearchUniqueUseCase.item(anyString())).thenReturn(categoryDto);

        final MockHttpServletRequestBuilder httpServletRequest = MockMvcRequestBuilders.get(endpoint, categoryDto.getId())
                .contentType(MediaType.APPLICATION_JSON);

        //## When
        final ResultActions resultActions = mockMvc.perform(httpServletRequest);

        //## Then
        resultActions.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final String responseJson = resultActions.andReturn().getResponse().getContentAsString();

        final var response = TestUtils.toObject(responseJson, CategoryDto.class).orElseThrow();

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(categoryDto);

        verify(iSearchUniqueUseCase, times(1)).item(categoryDto.getId());
    }

    @Test
    void getCategoryByIDWhenNotFound() {

        //## Mocks - Objects and Values
        final String errorMessage = "Not Found";

        //## Given
        when(iSearchUniqueUseCase.item(anyString())).thenThrow(new ResourceNotFound(ISearchUniqueUseCase.class, errorMessage));

        //## When
        var exception = assertThrows(Exception.class,
                () -> administrativeCategoriesRestApiAdapter.getById(anyString()));

        //## Then
        assertEquals(errorMessage, exception.getMessage());
        verify(iSearchUniqueUseCase, times(1)).item(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = API_VERSION_1 + ADM_CATEGORY + CATEGORY_ID)
    void deleteCategoryByID(String endpoint) throws Exception {

        //## Given
        final MockHttpServletRequestBuilder httpServletRequest = MockMvcRequestBuilders.delete(endpoint, UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON);

        //## When
        final ResultActions resultActions = mockMvc.perform(httpServletRequest);

        //## Then
        resultActions.andDo(print())
                .andExpect(status().isNoContent());

        verify(iDeleteCategoryUseCase).removeItem(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = API_VERSION_1 + ADM_CATEGORY + CATEGORY_ID)
    void updateCategory(String endpoint) throws Exception {

        //## Mocks - Objects and Values
        var categoryCreateDto = new CategoryCreateDto("Suco");
        var categoryDto = new CategoryDto("1", "Suco", ZonedDateTime.now(), ZonedDateTime.now());

        //## Given
        when(iUpdateCategoryUseCase.updateItem(any(CategoryCreateDto.class), anyString())).thenReturn(categoryDto);

        final String json = TestUtils.toJSON(categoryCreateDto).orElseThrow();
        final MockHttpServletRequestBuilder httpServletRequest = MockMvcRequestBuilders.put(endpoint, categoryDto.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        //## When
        final ResultActions resultActions = mockMvc.perform(httpServletRequest);

        //## Then
        resultActions.andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        final String responseJson = resultActions.andReturn().getResponse().getContentAsString();

        final var response = TestUtils.toObject(responseJson, CategoryDto.class).orElseThrow();

        Assertions.assertThat(response)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(ZonedDateTime.class)
                .isEqualTo(categoryDto);

        verify(iUpdateCategoryUseCase, times(1)).updateItem(any(CategoryCreateDto.class), anyString());
    }

}