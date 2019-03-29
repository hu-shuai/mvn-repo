package com.hs.fastService.enums;

import com.hs.fastService.util.LogUtil;

/**
 * where 条件后 对于值的操作
 * 默认值为0， 操作 0，1，2，3，4，5，6, 7
 * ( 0 等于，1 大于， 2 小于，4 大于等于，5 小于等于 6 不等于 7 in,  3 模糊查询like/ 自动在值前后加%，所以请求参数值不用加% )
 */
public enum Operation {

    EQUAL(0),
    GREATER_THAN(1),
    LESS_THAN(2),
    LIKE(3),
    GREATER_THAN_OR_EQUAL_TO(4),
    LESS_THAN_OR_EQUAL_TO(5),
    NOT_EQUAL(6),
    IN(7),
    BETWEEN(8);

    public int value;

    Operation(int value) {
        this.value = value;
    }

    public static Operation ofNameOrValue(Object value) {
        if (value == null) return Operation.EQUAL;
        if (Integer.class.isAssignableFrom(value.getClass()) || int.class.isAssignableFrom(value.getClass())) {
            return Operation.valueOf((Integer) value);
        }
        try {
            return Operation.valueOf((String) value);
        } catch (Exception ignored) {
        }
        try {
            return valueOf(Integer.parseInt((String) value));
        } catch (Exception e) {
            LogUtil.error("未能匹配枚举Operation ：", e);
        }
        return EQUAL;
    }

    public static Operation valueOf(int value) {
        switch (value) {
            case 0:
                return EQUAL;
            case 1:
                return GREATER_THAN;
            case 2:
                return LESS_THAN;
            case 3:
                return LIKE;
            case 4:
                return GREATER_THAN_OR_EQUAL_TO;
            case 5:
                return LESS_THAN_OR_EQUAL_TO;
            case 6:
                return NOT_EQUAL;
            case 7:
                return IN;
            default:
                return null;
        }
    }
}
