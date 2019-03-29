package com.hs.fastService;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class RequestConfig {

    private String name;

    private RequestType type;

    private Class entityClass;

    private Class serviceClass;

    private List<Map<String, Object>> extraInfo;

    private Method customMethod;

    private Map<String, Object> requestParams;

    private Map<String, Object> responseExcept;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public Map<String, Object> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String, Object> requestParams) {
        this.requestParams = requestParams;
    }

    public Map<String, Object> getResponseExcept() {
        return responseExcept;
    }

    public void setResponseExcept(Map<String, Object> responseExcept) {
        this.responseExcept = responseExcept;
    }

    public Class getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
    }

    public Method getCustomMethod() {
        return customMethod;
    }

    public void setCustomMethod(Method customMethod) {
        this.customMethod = customMethod;
    }

    public List<Map<String, Object>> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(List<Map<String, Object>> extraInfo) {
        this.extraInfo = extraInfo;
    }

    public enum RequestType {
        getOne, getList, save, update, delete, custom;
    }
}
