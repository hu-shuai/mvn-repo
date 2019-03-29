package com.hs.fastService.enums;

import com.hs.fastService.util.LogUtil;

/**
 * 连接符，默认值为 0， （0, 1, 2, 3）
 * 0 表示 and 连接条件， a=3 and b=4
 * 1 表示 or 连接条件，
 * 2 表示 and 连接，并且和前面的条件是一个作用域， （和之前的条件在一个括号内）
 * 3 表示 or 连接，并且和前面的条件是一个作用域， （和之前的条件在一个括号内）
 *
 * 例如：where (a=3 and b=4) or (c=5 or d=6),
 * 第一个and 值为 2 (AND_SPACE)， 第一个 or 值为 1， 第二个 or 值为 3 (OR_SPACE)；
 */
public enum  Connector {

    AND(0), OR(1), AND_SPACE(2), OR_SPACE(3);

    public int value;

    Connector(int value) {
        this.value = value;
    }

    public static Connector ofNameOrValue(Object value) {
        if (value == null) return Connector.AND;
        if (Integer.class.isAssignableFrom(value.getClass())
                || int.class.isAssignableFrom(value.getClass())) {
            return Connector.valueOf((Integer) value);
        }
        try {
            return Connector.valueOf((String) value);
        } catch (Exception ignored) {
        }
        try {
            return valueOf(Integer.parseInt((String) value));
        } catch (Exception e) {
            LogUtil.error("未能匹配枚举Connector ：", e);
        }
        return AND;
    }

    public static Connector valueOf(int value) {
        switch (value) {
            case 0:
                return AND;
            case 1:
                return OR;
            case 2:
                return AND_SPACE;
            case 3:
                return OR_SPACE;
            default:
                return AND;
        }
    }
}
