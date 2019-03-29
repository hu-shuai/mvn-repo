package com.hs.fastService.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {

    private static final Logger infoLogger = LoggerFactory.getLogger(LogUtil.class);

    public static void error(String message, Object... params) {
        infoLogger.error(getCallMessage(message), params);
    }

    public static void info(String message, Object... params) {
        infoLogger.info(getCallMessage(message), params);
    }

    public static void error(String message, Throwable throwable) {
        infoLogger.error(message, throwable);
    }

    private static String getCallMessage(String message) {
        String callString = "";
        try {
            int i = 4;
            StackTraceElement caller = Thread.currentThread().getStackTrace()[i];

            while (caller != null
                    && (caller.getClassName().startsWith("org.springframework")
                    || caller.getMethodName().equals("invoke"))) {
                i++;
                caller = Thread.currentThread().getStackTrace()[i];
            }
            if (caller != null) {
                String className = caller.getClassName();
                if (className.contains("$")) {
                    className = className.substring(0, className.indexOf("$"));
                }
                callString = String.format("(%s : %s : %d) ： \n%s", className, caller.getMethodName(), caller.getLineNumber(), message);
            }
        } catch (Exception e) {
        }
        return  callString;
    }
}
