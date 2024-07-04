package br.com.crudservice.interfaces;

import br.com.crudservice.dto.DTO;

public interface Mapper<T, D> {

    <D2 extends DTO> D2 toDTO(T entity, Class<D2> dtoClass);

    T toEntity(DTO dto, Class<T> entityClass);

    T toEntity(DTO dto, T entity);
}
