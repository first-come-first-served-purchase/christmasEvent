package com.doosan.productservice.mapper;

import com.doosan.common.enums.ProductCategory;
import com.doosan.productservice.domain.Product;
import com.doosan.productservice.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @ValueMappings({
        @ValueMapping(source = "ACCESSORIES", target = "ACCESSORIES"),
        @ValueMapping(source = "SHOES", target = "SHOES"),
        @ValueMapping(source = "BAGS", target = "BAGS"),
        @ValueMapping(source = "DIGITAL", target = "DIGITAL"),
        @ValueMapping(source = "PETS", target = "PETS"),
        @ValueMapping(source = "OTHER", target = "OTHER"),
        @ValueMapping(source = "CLOTHING", target = "CLOTHING"),
        @ValueMapping(source = "BEAUTY", target = "BEAUTY"),
        @ValueMapping(source = "SPORTS", target = "SPORTS"),
        @ValueMapping(source = "HOME", target = "HOME"),
        @ValueMapping(source = "FOOD", target = "FOOD"),
        @ValueMapping(source = "BOOKS", target = "BOOKS"),
        @ValueMapping(source = "TOYS", target = "TOYS"),
        @ValueMapping(source = "ELECTRONICS", target = "DIGITAL")
    })
    ProductResponse toDto(Product product);

} 