package br.com.crudservice.services;

import br.com.crudservice.dto.DTO;
import br.com.crudservice.interfaces.Mapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MapperImpl<T, D extends DTO> implements Mapper<T, D> {

    protected final ModelMapper modelMapper;

    @Autowired
    public MapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public <D extends DTO> D toDTO(T entity, Class<D> dtoClass) {
        return modelMapper.map(entity, dtoClass);
    }

    @Override
    public T toEntity(DTO dto, Class<T> entityClass) {
        return modelMapper.map(dto, entityClass);
    }

    @Override
    public T toEntity(DTO dto, T entity) {
        this.modelMapper.map(dto, entity);
        return entity;
    }
}