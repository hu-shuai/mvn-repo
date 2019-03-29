package com.hs.fastService.dao;

import com.hs.fastService.ErrorMsg;
import com.hs.fastService.entities.entity.BaseEntity;
import com.hs.fastService.util.AppUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@NoRepositoryBean
public interface BaseDao<T extends BaseEntity> extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {


    Map<Class, BaseDao> instanceMap = new HashMap<>();

    static <T extends BaseEntity> BaseDao getInstance(Class<T> entityClass) {
        BaseDao repository = instanceMap.get(entityClass);
        synchronized (entityClass) {
            if (repository == null) {
                repository = new BaseDaoImpl(entityClass, AppUtil.getBean(EntityManager.class));
                instanceMap.put(entityClass, repository);
            }
        }
        return repository;
    }

    T findByIdOrElseThrow(Long id, ErrorMsg error);

    T findByIdOrElseThrow(Long id, int errorCode, String errorMessage);

    T findByIdOrElse(Long id, T value);

    T get(WhereInfo... wheres);

    T get(Map map);

    List<T> getAll(WhereInfo... wheres);

    Page<T> getList(Pageable pageable, WhereInfo... wheres);

    List<T> getAll(Map map);

    Page<T> getList(Map map, Pageable pageable);

    Object save(Map map);

    Object update(T entity);

    int update(Map map);

    int update(Map setMap, WhereInfo... wheres);

    int delete(Map map);

    int delete(WhereInfo... wheres);

}
