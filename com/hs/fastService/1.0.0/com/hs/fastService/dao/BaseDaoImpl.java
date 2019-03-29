package com.hs.fastService.dao;

import com.hs.fastService.ErrorMsg;
import com.hs.fastService.entities.entity.BaseEntity;
import com.hs.fastService.enums.BaseErrorMsg;
import com.hs.fastService.enums.Connector;
import com.hs.fastService.enums.Operation;
import com.hs.fastService.exceptions.ResponseException;
import com.hs.fastService.util.AppUtil;
import com.hs.fastService.util.JsonUtil;
import com.hs.fastService.util.LogUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 复杂条件 说明
 * 1、如果请求参数带有 extraInfo 字段 说明是复杂查询，然后正常请求参数全用and连接， extraInfo的值 由后台开发人员提供
 * 2、示例：下面表示  where username = 'username请求参数值' or age > age请求参数值
 *    extraInfo: [
 *      {
 *          key: "username", // 参数名
 *          operation: 0,  // {@link Operation} 操作, 可以是 Operation枚举的名称也可以是值
 *          value: "zhangsan" // value
 *      },
 *      {
 *          key: "age", // 参数名
 *          connector: 1,   // {@link Connector} 连接符，可以是 Connector枚举的名称也可以是值
 *          operation: 1  // {@link Operation} 操作, 可以是 Operation枚举的名称也可以是值
 *          value: "zhangsan" // value 取此值查询
 *      }
 *    ]
 *
 */
public class BaseDaoImpl<T extends BaseEntity>
        extends SimpleJpaRepository<T, Long>
        implements BaseDao<T> {


    private EntityManager entityManager;
    private Class<T> domainClass;

    public BaseDaoImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.domainClass = entityInformation.getJavaType();
    }

    public BaseDaoImpl(Class<T> domainClass, EntityManager em) {
        this(JpaEntityInformationSupport.getEntityInformation(domainClass, em), em);
    }

    @Override
    public T findByIdOrElseThrow(Long id, ErrorMsg error) {
       return findById(id).orElseThrow(() -> new ResponseException(error));
    }

    @Override
    public T findByIdOrElseThrow(Long id, int errorCode, String errorMessage) {
        return findById(id).orElseThrow(() -> new ResponseException(errorCode, errorMessage));
    }

    @Override
    public T findByIdOrElse(Long id, T value) {
        return findById(id).orElse(value);
    }

    @Override
    public T get(WhereInfo... wheres) {
        if (wheres == null || wheres.length == 0) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        return findOne(createSpecification(wheres)).orElse(null);
    }

    @Override
    public T get(Map map) {
        if (map == null || map.size() == 0) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        // 根据复杂条件查询数据， 复杂条件规则见类说明
        WhereInfo [] whereInfos = parseExtraInfoToWhereInfo(map);
        if (whereInfos != null && whereInfos.length != 0) {
            Specification<T> spec = createSpecification(whereInfos);
            return findOne(spec).orElse(null);
        }
        // 简单的 AND 条件查询数据
        Example<T> example = Example.of(mapToBean(map));
        return findOne(example).orElse(null);
    }

    @Override
    public List<T> getAll(WhereInfo... whereInfos) {
        if (whereInfos != null && whereInfos.length != 0) {
            Specification<T> spec = createSpecification(whereInfos);
            return findAll(spec);
        }
        return findAll();
    }

    @Override
    public Page<T> getList(Pageable pageable, WhereInfo... wheres) {
        return findAll(createSpecification(wheres), pageable);
    }

    @Override
    public List<T> getAll(Map map) {
        WhereInfo [] whereInfos = parseExtraInfoToWhereInfo(map);
        if (whereInfos != null && whereInfos.length != 0) {
            Specification<T> spec = createSpecification(whereInfos);
            return findAll(spec);
        }
        Example<T> example = Example.of(mapToBean(map));
        return findAll(example);
    }

    @Override
    public Page<T> getList(Map map, Pageable pageable) {
        WhereInfo [] whereInfos = parseExtraInfoToWhereInfo(map);
        if (whereInfos != null && whereInfos.length != 0) {
            Specification<T> spec = createSpecification(whereInfos);
            return findAll(spec, pageable);
        }
        // 简单的 AND 条件查询数据
        Example<T> example = Example.of(mapToBean(map));
        return findAll(example, pageable);
    }

    @Override
    public <S extends T> S save(S entity) {
        if (entity == null) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        Long id = entity.getId();
        if (id != null) {
            T dbEntity = findByIdOrElseThrow(id, BaseErrorMsg.DATA_NOT_EXISTED);
            copyPropertiesIfNotNull(entity, dbEntity);
            return (S) super.save(dbEntity);
        }
        return super.save(entity);
    }

    public Object save(Map map) {
        if (map == null || map.isEmpty()) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        Object ids = map.get("ids");
        if (ids != null) {
            return update(map);
        }
        return save(mapToBean(map));
    }

    public Object update(T entity) {
        if (entity == null) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        if (entity.getId() == null) {
            return super.save(entity);
        }
        final BeanWrapper src = new BeanWrapperImpl(entity);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Map setMap = new HashMap();
        for(PropertyDescriptor pd : pds) {
            String name = pd.getName();
            Object value = src.getPropertyValue(name);
            if (value == null || "id".equals(name)) {
                continue;
            }
            setMap.put(name, value);
        }
        WhereInfo whereInfo = new WhereInfo("id", entity.getId());
        return update(setMap, whereInfo);
    }

    @Override
    public int update(Map map) {
        if (map == null || map.isEmpty()) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        Object id = map.get("id");
        map.remove("id");
        if (id != null) {
            return update(map, new WhereInfo("id", id));
        }
        Object ids = map.get("ids");
        map.remove("ids");
        if (ids != null) {
            return update(map, new WhereInfo("id", ids, Operation.IN));
        }

        Object extraInfo = map.get("extraInfo");
        map.remove("extraInfo");
        Map extraMap = new HashMap(1);
        extraMap.put("extraInfo", extraInfo);
        WhereInfo[] whereInfos = parseExtraInfoToWhereInfo(extraMap);
        return update(map, whereInfos);
    }


    @Override
    public int update(Map setMap, WhereInfo... wheres) {
        if (setMap.size() == 0 || wheres == null || wheres.length == 0) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<T> update = cb.createCriteriaUpdate(domainClass);
        Root<T> root = update.from(domainClass);
        Set<Map.Entry<String, Object>> set = setMap.entrySet();
        for(Map.Entry<String, Object> entry : set) {
            String name = entry.getKey();
            Object value = entry.getValue();
            value = convertValue(name, value);
            if (value == null) continue;
            update.set(name, value);
        }
        Predicate predicate = parseWhereInfo(root, wheres);
        update.where(predicate);
        return entityManager.createQuery(update).executeUpdate();
    }

    @Override
    public int delete(Map map) {
        return delete(parseExtraInfoToWhereInfo(map));
    }

    @Override
    public int delete(WhereInfo... wheres) {
        if (wheres == null || wheres.length == 0) {
            throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER);
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<T> delete = cb.createCriteriaDelete(domainClass);
        Root<T> r = delete.from(domainClass);
        Predicate predicate = parseWhereInfo(r, wheres);
        delete.where(predicate);
        return entityManager.createQuery(delete).executeUpdate();
    }

    private WhereInfo[] parseExtraInfoToWhereInfo(Map paramsMap) {
        Object extraInfo = paramsMap.get("extraInfo");
        if (StringUtils.isEmpty(extraInfo)) {
            return null;
        }
        paramsMap.remove("extraInfo");
        try {
            List list;
            if (extraInfo instanceof List) {
                list = (List) extraInfo;
                if (list.isEmpty()) {
                    return null;
                }
            } else {
                list = JsonUtil.getJsonParser().readValue((String)extraInfo, List.class);
            }
            // 处理附加复杂参数
            List<WhereInfo> whereInfos = new ArrayList<>();
            for (Object obj : list) {
                Map map = (Map) obj;
                WhereInfo info = WhereInfo.fromMap(map);
                Object pValue = paramsMap.get(info.getKey());
                if (!StringUtils.isEmpty(info.getReplaceKey())) {
                    Object replaceValue = paramsMap.get(info.getReplaceKey());
                    if (replaceValue != null) {
                        info.setValue(replaceValue);
                    }
                    paramsMap.remove(info.getReplaceKey());
                } else if (pValue != null) {
                    info.setValue(pValue);
                    paramsMap.remove(info.getKey());
                }

                whereInfos.add(info);
            }
            // 处理普通参数
            Set<Map.Entry<String, Object>> set = paramsMap.entrySet();
            for(Map.Entry<String, Object> entry : set) {
                whereInfos.add(new WhereInfo(entry.getKey(), entry.getValue()));
            }
            return whereInfos.toArray(new WhereInfo[0]);
        } catch (IOException e) {
            LogUtil.error("extraInfo 数据解析异常", e);
            throw  new ResponseException(-1, "extraInfo 数据解析异常");
        }
    }

    private Predicate parseWhereInfo(Root root, WhereInfo... wheres) {
        if (wheres == null || wheres.length == 0) {
            return null;
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        Predicate predicate = null;
        Predicate tempPredicate = null;
        Connector tempConnector = null;
        for (WhereInfo where : wheres) {
            String key = where.getKey();
            Object value = where.getValue();
            Operation operation = where.getOperation();
            if (operation == null) {
                operation = Operation.EQUAL;
            }
            Connector connector = where.getConnector();
            if (connector == null) {
                connector = Connector.AND;
            }
            // 数据类型不匹配时，进行转换
            if (operation != Operation.IN && operation != Operation.BETWEEN) {
                value = convertValue(key, value);
            } else if (operation == Operation.IN && value instanceof String) {
                value = AppUtil.getBean(ConversionService.class).convert(value, Object[].class);
            }
            if (value == null) continue;
            Predicate p = createPredicate(cb, root, operation, key, value);
            if (tempPredicate == null) {
                tempPredicate = p;
                continue;
            }

            switch (connector) {
                case AND:
                case OR:
                    if (predicate == null) {
                        predicate = tempPredicate;
                    } else {
                        if (tempConnector == Connector.OR) {
                            predicate = cb.or(predicate, tempPredicate);
                        } else {
                            predicate = cb.and(predicate, tempPredicate);
                        }
                    }
                    tempPredicate = p;
                    tempConnector = connector;
                    break;
                case AND_SPACE:
                    tempPredicate = cb.and(tempPredicate, p);
                    break;
                case OR_SPACE:
                    tempPredicate = cb.or(tempPredicate, p);
                    break;
            }
        }
        if (predicate == null) {
            return tempPredicate;
        }
        if (tempConnector == Connector.OR) {
            return cb.or(predicate, tempPredicate);
        }
        return cb.and(predicate, tempPredicate);
    }

    private Predicate createPredicate(CriteriaBuilder cb, Root root, Operation operation, String key, Object value) {
        Path path = root.get(key);
        switch (operation) {
            case EQUAL:
                return cb.equal(path, value);
            case GREATER_THAN:
                return cb.greaterThan(path, (Comparable) value);
            case LESS_THAN:
                return cb.lessThan(path, (Comparable) value);
            case LIKE:
                return cb.like(path, "%" + value + "%");
            case GREATER_THAN_OR_EQUAL_TO:
                return cb.greaterThanOrEqualTo(path, (Comparable) value);
            case LESS_THAN_OR_EQUAL_TO:
                return cb.lessThanOrEqualTo(path, (Comparable) value);
            case NOT_EQUAL:
                return cb.notEqual(path, value);
            case IN:
                if (value instanceof Collection) {
                    return path.in((Collection) value);
                } else if (value.getClass().isArray()){
                    return path.in((Object[])value);
                } else {
                    return path.in(value);
                }
            case BETWEEN:
                String s = (String) value;
                String[] arr = s.split("\\s*,\\s*");
                if (arr.length != 2) {
                    throw new ResponseException(-500, "操作符 between 格式不正确");
                }
                Object start = convertValue(key, arr[0]);
                Object end = convertValue(key, arr[1]);
                return cb.between(path, (Comparable)start, (Comparable)end);
        }
        return null;
    }

    private Object convertValue(String fieldName, Object value) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(domainClass, fieldName);
        if (pd == null) {
            return null;
        }
        if (!pd.getPropertyType().isAssignableFrom(value.getClass())) {
            value = AppUtil.getBean(ConversionService.class).convert(value, pd.getPropertyType());
        }
        return value;
    }

    private Specification<T> createSpecification(WhereInfo... wheres) {
        return (Specification<T>) (root, query, cb) -> parseWhereInfo(root, wheres);
    }

    private T mapToBean(Map map) {
        return AppUtil.getBean(ConversionService.class).convert(map, domainClass);
    }

    private void copyPropertiesIfNotNull(Object source, Object target) {
        if (source == target) {
            return;
        }
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        for(PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue != null) {
                try {
                    Method method = pd.getWriteMethod();
                    if (method == null) {
                        continue;
                    }
                    if ("_null_".equals(srcValue)) {
                        method.invoke(target, new Object[]{null});
                    } else if ("_empty_".equals(srcValue)) {
                        method.invoke(target, "");
                    } else {
                        method.invoke(target, srcValue);
                    }
                } catch (InvocationTargetException | IllegalAccessException e ) {
                    LogUtil.error("参数赋值错误：", e);
                    throw new ResponseException(-1, "参数赋值出错");
                }
            }
        }
    }
}
