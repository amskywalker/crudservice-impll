package br.com.crudservice.services;

import br.com.crudservice.dto.DTO;
import br.com.crudservice.exceptions.ResourceNotFoundException;
import br.com.crudservice.interfaces.JpaPageableRepository;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class DatabaseUtils<T, ID, D extends DTO> implements br.com.crudservice.interfaces.DatabaseUtils<T, ID> {

    protected final JpaPageableRepository<T, ID> repository;
    private final MapperImpl<T, D> mapper;
    private final EntityManager entityManager;

    public DatabaseUtils(JpaPageableRepository<T, ID> repository, MapperImpl<T, D> mapper, EntityManager entityManager) {
        this.repository = repository;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Iterable<T> getAll() {
        return repository.findAll();
    }

    @Override
    public Page<T> getAll(int page, int size, Specification<T> specification) {
        Pageable pageable = PageRequest.of(page, size);
        return this.repository.findAll(specification, pageable);
    }

    @Override
    public Page<T> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return this.repository.findAll(pageable);
    }

    @Override
    public <D extends DTO> D getByIdDTO(ID id, Class<D> dtoClass) throws ResourceNotFoundException {
        T entity = getById(id);
        return mapper.toDTO(entity, dtoClass);
    }

    @Override
    public T getById(ID id) throws ResourceNotFoundException {
        Optional<T> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new ResourceNotFoundException("Recurso não encontrado id: " + id);
        }
        return entity.get();
    }

    @Override
    public List<T> getByColumn(String columnName, Object value) {
        Specification<T> specification = (root, query, criteriaBuilder) -> {
            if (value == null) {
                return criteriaBuilder.isNull(root.get(columnName));
            }
            return criteriaBuilder.equal(root.get(columnName), value);
        };

        return repository.findAll(specification);
    }

    public QueryBuilder getByColumnBuilder(String columnName, Object value) {
        return new QueryBuilder(columnName, value);
    }

    @Override
    public List<T> getByColumns(Map<String, Object> columnValues) {
        Specification<T> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (Map.Entry<String, Object> entry : columnValues.entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();
                if (value == null) {
                    predicates.add(criteriaBuilder.isNull(root.get(columnName)));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get(columnName), value));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(specification);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    public T create(DTO dto, Class<T> entityClass) {
        T entity = mapper.toEntity(dto, entityClass);
        return repository.save(entity);
    }

    @Override
    public T update(ID id, DTO dto) throws ResourceNotFoundException {
        T entityToUpdate = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado id: " + id));
        CustomBeanUtils.copyNonNullProperties(dto, entityToUpdate);
        return repository.save(entityToUpdate);
    }

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public void delete(ID id) throws ResourceNotFoundException {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado id: " + id);
        }
        repository.deleteById(id);
    }

    public T attach(ID id, String resource, Object item) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ResourceNotFoundException {
        T entity = getById(id);
        Method getter = entity.getClass().getMethod("get" + resource.substring(0, 1).toUpperCase() + resource.substring(1));
        @SuppressWarnings("unchecked") Set<Object> items = (Set<Object>) getter.invoke(entity);
        if (items == null) {
            items = new HashSet<>();
        }
        items.add(item);
        Method setter = entity.getClass().getMethod("set" + resource.substring(0, 1).toUpperCase() + resource.substring(1), Set.class);
        setter.invoke(entity, items);
        return save(entity);
    }

    public class QueryBuilder {
        private final Specification<T> specification;
        private final List<String> relations = new ArrayList<>();

        public QueryBuilder(String columnName, Object value) {
            this.specification = (root, query, criteriaBuilder) -> {
                if (value == null) {
                    return criteriaBuilder.isNull(root.get(columnName));
                }
                return criteriaBuilder.equal(root.get(columnName), value);
            };
        }

        public QueryBuilder with(String... relations) {
            Collections.addAll(this.relations, relations);
            return this;
        }

        public List<T> fetch(Class<T> entityClass) {
            EntityGraph<T> entityGraph = entityManager.createEntityGraph(entityClass);
            for (String relation : relations) {
                System.out.println(relation);
                entityGraph.addAttributeNodes(relation);
            }

            CriteriaQuery<T> query = entityManager.getCriteriaBuilder().createQuery(entityClass);
            Root<T> root = query.from(entityClass);
            query.select(root).where(specification.toPredicate(root, query, entityManager.getCriteriaBuilder()));

            return entityManager.createQuery(query)
                    .setHint("javax.persistence.fetchgraph", entityGraph)
                    .getResultList();
        }
    }
}
