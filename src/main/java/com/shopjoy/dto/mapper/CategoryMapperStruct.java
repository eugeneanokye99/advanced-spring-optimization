package com.shopjoy.dto.mapper;

import com.shopjoy.dto.request.CreateCategoryRequest;
import com.shopjoy.dto.request.UpdateCategoryRequest;
import com.shopjoy.dto.response.CategoryResponse;
import com.shopjoy.entity.Category;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Category entity and DTOs providing type-safe bean mapping.
 * Replaces manual mapping boilerplate with compile-time generated code.
 */
@Mapper(
    componentModel = "spring", // Generate Spring component
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapperStruct {

    /**
     * Maps CreateCategoryRequest to Category entity.
     * 
     * @param request the create category request
     * @return the mapped category entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toCategory(CreateCategoryRequest request);

    /**
     * Maps Category entity to CategoryResponse.
     * 
     * @param category the category entity
     * @return the mapped category response
     */
    CategoryResponse toCategoryResponse(Category category);

    /**
     * Maps list of Category entities to list of CategoryResponse DTOs.
     * 
     * @param categories list of category entities
     * @return list of category response DTOs
     */
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    /**
     * Updates existing Category entity from UpdateCategoryRequest.
     * Only maps non-null values from the request.
     * 
     * @param request the update request
     * @param category the existing category to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategoryFromRequest(UpdateCategoryRequest request, @MappingTarget Category category);
}