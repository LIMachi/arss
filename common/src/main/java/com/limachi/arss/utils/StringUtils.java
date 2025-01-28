package com.limachi.arss.utils;

import java.util.HashSet;
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
        if (m.find()) {
            var s = m.group(0).split("\\$");
            return s[s.length - 1];
        }
        return fullyQualifiedClassName;
    }

    //add escapeChar in front of each instance of the characters from set in input
    public static String escape(String input, char escapeChar, char ... set) {
        StringBuilder out = new StringBuilder();
        var test = new HashSet<Character>();
        for (char c : set)
            test.add(c);
        for (char c : input.toCharArray()) {
            if (test.contains(c))
                out.append(escapeChar);
            out.append(c);
        }
        return out.toString();
    }

    public static class StringWalker {
        final String original;
        final char[] chars;
        int cursor;

        public StringWalker(String input) {
            original = input;
            chars = original.toCharArray();
            cursor = 0;
        }

        public String get(int skip, int len) {
            int start = Math.clamp(cursor + skip, 0, chars.length);
            int end = Math.clamp(cursor + skip + len, 0, chars.length);
            len = Math.abs(start - end);
            if (len == 0)
                return "";
            if (start < end)
                return original.substring(start, end);
            else {
                StringBuilder out = new StringBuilder();
                for (int c = end - 1; c >= start; --c)
                    out.append(chars[c]);
                return out.toString();
            }
        }

        public String get(int len) { return get(0, len); }
        public String get() { return get(0, remainder()); }
        public char at(int index) { return index >= 0 && index < chars.length ? chars[index] : '\0'; }
        public char c(int offset) { return at(offset + cursor); }
        public char c() { return at(cursor); }
        public void consume(int amount) { cursor = Math.clamp(cursor + amount, 0, chars.length); }
        public int cursor() { return cursor; }
        public boolean ok(int offset) {
            char c = c(offset);
            return c != '\0' && c != '\u0003' && c != '\u0004';
        }
        public boolean ok() { return ok(0); }
        public boolean finished() { return !ok(); }
        public int len() { return chars.length; }
        public int remainder() { return Math.clamp(chars.length - cursor, 0, chars.length); }
    }
}
