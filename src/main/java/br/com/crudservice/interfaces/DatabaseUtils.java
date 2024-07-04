package br.com.crudservice.interfaces;

import br.com.crudservice.dto.DTO;
import br.com.crudservice.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public interface DatabaseUtils<T, ID> {
    Iterable<T> getAll();

    Page<T> getAll(int page, int size, Specification<T> specification);

    Page<T> getAll(int page, int size);

    List<T> getByColumn(String columnName, Object value);

    List<T> getByColumns(Map<String, Object> columnValues);

    <D extends DTO> D getByIdDTO(ID id, Class<D> dtoClass) throws ResourceNotFoundException;

    T getById(ID id) throws ResourceNotFoundException;

    boolean existsById(ID id);

    T create(DTO dto, Class<T> entityClass);

    T update(ID id, DTO dto) throws ResourceNotFoundException;

    T save(T entity);

    T attach(ID id, String resource, Object item) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ResourceNotFoundException;

    void delete(ID id) throws ResourceNotFoundException;
}
