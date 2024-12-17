package com.limachi.arss.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static Pattern CAMEL_TO_SNAKE_REGEX = Pattern.compile("\\B([A-Z][^A-Z]+?)");
    public static Pattern CLASS_NAME_REGEX = Pattern.compile("(?:\\w+\\$)*(\\w+)\\$?(?:\\(.*\\)?)?$");
    public static String camelToSnake(String str) {
        if (str == null || str.isEmpty()) return "";
        return CAMEL_TO_SNAKE_REGEX.matcher(str).replaceAll("_$1").toLowerCase();
    }

    public static String getSimplifiedClassName(String fullyQualifiedClassName) {
        Matcher m = CLASS_NAME_REGEX.matcher(fullyQualifiedClassName);
        if (m.find())
            return m.group(0);
        return fullyQualifiedClassName;
    }
}
