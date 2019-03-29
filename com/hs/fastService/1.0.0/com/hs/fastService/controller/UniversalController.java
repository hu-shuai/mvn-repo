package com.hs.fastService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.hs.fastService.RequestConfig;
import com.hs.fastService.RequestConfig.RequestType;
import com.hs.fastService.RequestHandler;
import com.hs.fastService.entities.response.BaseResponse;
import com.hs.fastService.enums.BaseErrorMsg;
import com.hs.fastService.exceptions.ResponseException;
import com.hs.fastService.service.UniversalService;
import com.hs.fastService.util.AppUtil;
import com.hs.fastService.util.JsonUtil;
import com.hs.fastService.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(produces="application/json;charset=UTF-8")
public class UniversalController {

    @Autowired
    private UniversalService service;

    @Autowired(required = false)
    private RequestHandler requestHandler;

    @Autowired
    private Map<String, RequestConfig> requestMap;


    private RequestHandler defaultRequestHandler = new RequestHandler() {
        @Override
        public Map handleRequest(Map paramMap, RequestConfig config) {
            Map map = new HashMap();
            if (!CollectionUtils.isEmpty(config.getExtraInfo())) {
                map.put("extraInfo", config.getExtraInfo());
            }
            Map validateMap = config.getRequestParams();
            if (CollectionUtils.isEmpty(validateMap)) {
                return map;
            }
            validateMap.forEach((key, value) -> {
                Object pValue = paramMap.get(key);
                if (value == null && pValue == null) {
                    throw new ResponseException(BaseErrorMsg.NOT_REQUIRED_PARAMETER.code, "缺少参数：" + key);
                }
                if (pValue != null) {
                    map.put(key, pValue);
                } else {
                    map.put(key, value);
                }
            });
            return map;
        }

        @Override
        public Object handleResponse(BaseResponse response, RequestConfig config) {
            Map<String, Object> map = config.getResponseExcept();
            ObjectMapper json = JsonUtil.createJsonParser();
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.setFailOnUnknownId(false);
            if (map != null) {
                map.forEach((key, value) -> {
                    String[] arr;
                    if (value instanceof String[]) {
                        arr = (String[]) value;
                    } else if (value instanceof List){
                        List list = (List) value;
                        arr = (String[]) list.toArray(new String[0]);
                    } else {
                        String str = (String) value;
                        arr = str.split("\\s*,\\s*");
                    }
                    filterProvider.addFilter(key, SimpleBeanPropertyFilter.serializeAllExcept(arr));
                });
            }
            json.setFilterProvider(filterProvider);
            try {
                return json.writeValueAsString(response);
            } catch (JsonProcessingException e) {
                String msg = "返回对象json格式化异常";
                LogUtil.error(msg, e);
                return new BaseResponse(-1, msg);
            }
        }
    };

    @PostMapping(value = "/app/test")
    public Object test(@RequestParam Map map) {
        return requestMap;
    }

    @PostMapping("/app/{requestName}")
    public Object request(@RequestParam Map paramMap, @PathVariable String requestName) throws JsonProcessingException {
        RequestConfig config = requestMap.get(requestName);
        if (config == null) {
            throw  new ResponseException(BaseErrorMsg.SERVICE_NOT_EXISITED);
        }
        if (requestHandler == null) {
            requestHandler = defaultRequestHandler;
        }
        Map map = requestHandler.handleRequest(paramMap, config);
        Object result;
        RequestType requestType = config.getType();
        switch (requestType) {
            case getOne:
                result = service.get(map, config.getEntityClass());
                break;
            case getList:
                // 获取分页信息
                int pageNumber = Integer.parseInt(getParam("pageNumber", "0", paramMap));
                int pageSize = Integer.parseInt(getParam("pageSize", "10", paramMap));
                String orderBy = getParam("orderBy", "createTime", paramMap);
                String orderDirection = getParam("orderDirection", "DESC", paramMap);
                Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.Direction.valueOf(orderDirection), orderBy);
                result =  service.getList(map, config.getEntityClass(), pageable);
                break;
            case save:
                result =  service.save(map, config.getEntityClass());
                break;
            case update:
                result =  service.update(map, config.getEntityClass());
                break;
            case delete:
                result =  service.delete(map, config.getEntityClass());
                break;
            case custom:
                result =  invokeMethod(map, config);
                break;
            default:
                throw new ResponseException(-1, requestName + "的请求类型不存在， 请检查request.yml配置文件");

        }
        BaseResponse response;
        if (result instanceof Collection) {
            response = new BaseResponse((Collection) result);
        } else if (result instanceof Page) {
            response = BaseResponse.fromPage((Page)result);
        } else if (!(result instanceof BaseResponse)) {
            response = new BaseResponse(result);
        } else {
            response = (BaseResponse) result;
        }
        Object obj = requestHandler.handleResponse(response, config);
        if (obj instanceof String) {
            return obj;
        }
        return JsonUtil.getJsonParser().writeValueAsString(obj);
    }

    // 执行对应方法
    private Object invokeMethod(Map map, RequestConfig config) {
        Method method = config.getCustomMethod();
        try {
            Object customService = AppUtil.getBean(config.getServiceClass());
            return method.invoke(customService, map);
        } catch (Exception e) {
            LogUtil.error(
                    "接口配置 {} 错误： 在 serviceClass: {} 中没有找到自定义的 {} 方法，请检查request.yml配置",
                    config.getName(),
                    config.getServiceClass(),
                    config.getCustomMethod()
            );
            return new BaseResponse(BaseErrorMsg.SERVICE_NOT_EXISITED);
        }
    }


    // 获取参数
    private String getParam(String key, String defaultVaule, Map map) {
        String value = (String) map.get(key);
        map.remove(key);
        return value == null ? defaultVaule : value;
    }
}
