package com.exce.bluetooth.utils;

import com.exce.bluetooth.bean.MyField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 自定义对象迭代器
 * @Author Wangjj
 * @Create 2018/4/19.
 * @Content
 */
public class MyObjIterator {
    private Field[] fields;
    private Object obj;
    private int subscript = 0; // 当前下标
    private int maxSubscript; // 下标的最大值

    public MyObjIterator(Object obj) {
        this.obj = obj;
        fields = obj.getClass().getDeclaredFields();
        maxSubscript = fields.length - 1;
    }

    // 查看下一个还有木有
    public boolean hasNext() {
        return subscript <= maxSubscript;
    }

    // 获取下一个属性
    public MyField next() {
        String name = fields[subscript].getName();
        String getMethod = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Object value;
        try {
            Method m = obj.getClass().getMethod(getMethod);
            value = m.invoke(obj);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        MyField myField = new MyField(name, fields[subscript].getType(), value);
        subscript++;
        return myField;
    }
}
