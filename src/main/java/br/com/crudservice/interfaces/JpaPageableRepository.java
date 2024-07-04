package br.com.crudservice.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

@NoRepositoryBean
@Repository
public interface JpaPageableRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
