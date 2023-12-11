package ru.cities.task.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.QueryByExampleExecutor;

@NoRepositoryBean
public interface AbstractRepository<T> extends JpaRepository<T, Long>, QueryByExampleExecutor<T> {
/*
    T findById(Long id);

    List<T> findAll();

    void save(T city);

    void update(T city);

    void delete(Long id);
*/
}
