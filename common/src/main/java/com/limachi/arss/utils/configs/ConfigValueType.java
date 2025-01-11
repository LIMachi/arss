package com.limachi.arss.utils.configs;

public enum ConfigValueType {
    STRING,
    BOOLEAN,
    INT,
    FLOAT,
    LIST,
    MAP,
//    OBJECT,
    NULL;

    public boolean primitive() {
        return switch (this) {
            case STRING, BOOLEAN, INT, FLOAT -> true;
            default -> false;
        };
    }
}