package com.exce.bluetooth.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.exce.bluetooth.bean.UserInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * @Author Wangjj
 * @Create 2018/4/17.
 * @Content
 */
public class SharedPreferenceUtil {
    /**
     * 保存在手机里面的文件名(自定义)
     */
    public static final String FILE_NAME = "share_ecg_data";
    private static final String LOGIN_NAME = "账号ID";
    private static final String LOGIN_PASSWORD = "账号密码";
    private static SharedPreferences mSharedPreferences;

    /**
     * 账号
     *
     * @param context
     * @param loginName
     */
    public static void putLoginName(Context context, String loginName) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(LOGIN_NAME, loginName);
        edit.apply();
    }

    public static String getLoginName(Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString(LOGIN_NAME, "");
    }

    /**
     * 密码
     *
     * @param context
     * @param password
     */
    public static void putLoginPassword(Context context, String password) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(LOGIN_PASSWORD, password);
        edit.apply();
    }

    public static String getLoginPassword(Context context) {
        mSharedPreferences = context.getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        return mSharedPreferences.getString(LOGIN_PASSWORD, "");
    }




    public static void saveUser(Context context, String preferenceName, String key, UserInfo user) throws Exception {
        if (user instanceof Serializable) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceName, context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(user);//把对象写到流里
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                editor.putString(key, temp);
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new Exception("User must implements Serializable");
        }
    }





    public static UserInfo getUser(Context context, String preferenceName,String key) {
        SharedPreferences sharedPreferences=context.getSharedPreferences(preferenceName,context.MODE_PRIVATE);
        String temp = sharedPreferences.getString(key, "");
        ByteArrayInputStream bais =  new ByteArrayInputStream(Base64.decode(temp.getBytes(), Base64.DEFAULT));
        UserInfo user = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            user = (UserInfo) ois.readObject();
        } catch (IOException e) {
        }catch(ClassNotFoundException e1) {

        }
        return user;
    }







    /**
     * 保存数据的方法,我们需要拿到保存数据的具体类型,然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 得到保存数据的方法,我们根据默认值得到保存的数据的具体类型,然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(Context context, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }
        return null;
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.contains(key);
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    public static Map getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getAll();
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }
            return null;
        }

        /**
         * 如果找到则使用apply执行,否则使用commit
         *
         * @param editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }
}
