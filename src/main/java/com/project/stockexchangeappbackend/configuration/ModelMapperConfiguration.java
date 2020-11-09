package com.project.stockexchangeappbackend.configuration;

import com.project.stockexchangeappbackend.dto.ResourceDTO;
import com.project.stockexchangeappbackend.entity.Resource;
import com.project.stockexchangeappbackend.entity.Stock;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class ModelMapperConfiguration {

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();

        Converter<Stock, String> extractName = ctx -> ctx.getSource() == null ? null :
                ctx.getSource().getName();
        Converter<Stock, String> extractAbbreviation = ctx -> ctx.getSource() == null ? null :
                ctx.getSource().getAbbreviation();
        Converter<Stock, BigDecimal> extractCurrentPrice = ctx -> ctx.getSource() == null ? null :
                ctx.getSource().getCurrentPrice();

        modelMapper.createTypeMap(Resource.class, ResourceDTO.class)
                .addMappings(mapper -> {
                    mapper.using(extractName).map(Resource::getStock, ResourceDTO::setName);
                    mapper.using(extractAbbreviation).map(Resource::getStock, ResourceDTO::setAbbreviation);
                    mapper.using(extractCurrentPrice).map(Resource::getStock, ResourceDTO::setCurrentPrice);
                });

        return modelMapper;
    }
}
