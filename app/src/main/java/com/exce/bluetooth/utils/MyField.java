package com.exce.bluetooth.utils;

import java.lang.reflect.Type;

/**
 * @Author Wangjj
 * @Create 2018/4/20.
 * @Content
 */
public class MyField {
    private String name;
    private Type type;
    private Object value;

    public MyField() {
    }

    public MyField(String name, Type type, Object value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
