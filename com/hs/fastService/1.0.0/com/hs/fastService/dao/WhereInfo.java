package com.hs.fastService.dao;


import com.hs.fastService.enums.Connector;
import com.hs.fastService.enums.Operation;

import java.util.Map;

public class WhereInfo {
    private String key; // 参数名
    private Object value; // 值
    private String replaceKey; // 替换参数名

    /**
     * 连接符，默认值为 0， （0, 1, 2, 3）
     * 0 表示 and 连接条件， a=3 and b=4
     * 1 表示 or 连接条件，
     * 2 表示 and 连接，并且和前面的条件是一个作用域， （和之前的条件在一个括号内）
     * 3 表示 or 连接，并且和前面的条件是一个作用域， （和之前的条件在一个括号内）
     *
     * 例如：where (a=3 and b=4) or (c=5 or d=6),
     * 第一个and 值为 2， 第一个 or 值为 1， 第二个 or 值为 3；
     */
    private Connector connector;

    /**
     * 操作符 默认值为0， 操作 0，1，2，3，4，5，6, 7
     * ( 0 等于，1 大于， 2 小于，4 大于等于，5 小于等于 6 不等于 7 in,  3 模糊查询like/ 自动在值前后加%，所以请求参数值不用加% )
     */
    private Operation operation;


    public WhereInfo(String key, Object value, Connector connector, Operation operation) {
        this.key = key;
        this.value = value;
        this.connector = connector;
        this.operation = operation;
    }

    public WhereInfo(String key, Object value, Connector connector) {
        this(key, value, connector, Operation.EQUAL);
    }

    public WhereInfo(String key, Object value, Operation operation) {
        this(key, value, Connector.AND, operation);
    }

    public WhereInfo(String key, Object value) {
        this(key, value, Connector.AND, Operation.EQUAL);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public void setConnector(int connector) {
        this.connector = Connector.valueOf(connector);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public void setOperation(int operation) {
        this.operation = Operation.valueOf(operation);
    }

    public String getReplaceKey() {
        return replaceKey;
    }

    public void setReplaceKey(String replaceKey) {
        this.replaceKey = replaceKey;
    }

    public static WhereInfo fromMap(Map map) {
        String key = (String) map.get("key");
        Object value = map.get("value");
        Operation operation = Operation.ofNameOrValue(map.get("operation"));
        Connector connector = Connector.ofNameOrValue(map.get("connector"));
        WhereInfo info = new WhereInfo(key, value, connector, operation);
        info.setReplaceKey((String) map.get("replaceKey"));
        return info;
    }
}
