package com.hs.fastService.service;

import com.hs.fastService.dao.BaseDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class UniversalService {

    // 根据条件获取一条数据
    public Object get(Map map, Class entityClass) {
        return BaseDao.getInstance(entityClass).get(map);
    }

    // 根据条件获取多条数据
    public Page getList(Map map, Class entityClass, Pageable pageable) {
        if (!map.containsKey("dataState")) {
            map.put("dataState", 0);
        }
        return BaseDao.getInstance(entityClass).getList(map, pageable);
    }

    public Object save(Map map, Class entityClass) {
        return BaseDao.getInstance(entityClass).save(map);
    }

    public int update(Map map, Class entityClass) {
        return BaseDao.getInstance(entityClass).update(map);
    }

    // 删除 只进行逻辑删除（dataState 设置为 1）， 不进行物理删除
    public int delete(Map map, Class entityClass) {
        map.put("dataState", 1);
        return update(map, entityClass);
    }
}
