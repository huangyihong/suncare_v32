package com.ai.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 使用反射机制快速操作POJO对象
 * @author songxiaoliang
 *
 */
public class ReflectHelper {
    /**
     * 调用目标对象名为field的属性值的get方法
     *
     * @param object
     *            目标对象
     * @param fieldName
     *            属性名称
     * @return get方法的返回值
     */
    static public Object getValue(Object object, String fieldName) throws Exception{
        if(StringUtils.isBlank(fieldName)){
            return "";
        }
    	String[] fieldNames = fieldName.split("[.]");
    	if(fieldNames.length==1){
    		Method getMethod = getGetter(fieldName, object.getClass());
    		Object value = getMethod.invoke(object);
    		return value;
    	}else{
    		Object obj = object;
            for (String name : fieldNames) {
                Method getMethod = getGetter(name, obj.getClass());
                obj = getMethod.invoke(obj);
                if (obj == null) {
                    break;
                }
            }
    		return obj==null ? "" : obj.toString();
    	}
    }

    /**
     * 调用目标对象名为field的属性值的set方法
     *
     * @param object
     *            目标对象
     * @param fieldName
     *            属性名称
     * @param value
     *            设置的值
     */
    static public void setValue(Object object, String fieldName, Object value) throws Exception
    {
        Method setMethod = getSetter(fieldName, object.getClass());
        Class paramClass = setMethod.getParameterTypes()[0];

        if (value != null)
        {
            if (paramClass.equals(value.getClass()))
            {
                setMethod.invoke(object, value);
            }
            else
                if (paramClass.isAssignableFrom(value.getClass()))
                {
                    setMethod.invoke(object, value);
                }
                else
                {
                    setMethod.invoke(object, convertObjectType(value, paramClass));
                }
        }
        else
        {
            setMethod.invoke(object, (Object) null);
        }
    }

    /**
     * 根据对象的类型和属性名获取其对应的get方法
     *
     * @param fieldName
     *            属性名
     * @param clazz
     *            类名
     * @return 该属性对应的get方法
     * @throws NoSuchMethodException
     */
    public static Method getGetter(String fieldName, Class clazz) throws NoSuchMethodException
    {

        if (fieldName == null || fieldName.length() <= 1) {
            throw new NoSuchMethodException(clazz.getName() + "类中不存在属性为" + fieldName + "的set方法");
        }

        String name = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method getter = null;

        try
        {
            getter = clazz.getMethod("get" + name);
        }
        catch (NoSuchMethodException e)
        {
            getter = clazz.getMethod("is" + name);
        }

        if (getter == null)
        {
            throw new NoSuchMethodException(clazz.getName() + "类中不存在属性为" + fieldName + "的set方法");
        }

        return getter;
    }

    /**
     * 根据对象的类型和属性名获取其对应的get方法
     *
     * @param fieldName
     *            属性名
     * @param clazz
     *            类名
     * @return 该属性对应的get方法
     * @throws NoSuchMethodException
     */
    public static Method getGetter(String fieldName, Class clazz, Class paramType) throws NoSuchMethodException
    {

        if (fieldName == null || fieldName.length() <= 1)
        {
            throw new NoSuchMethodException(clazz.getName() + "类中不存在属性为" + fieldName + "的set方法");
        }

        String name = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method getter = null;

        try
        {
            getter = clazz.getMethod("get" + name, paramType);
        }
        catch (NoSuchMethodException e)
        {
            getter = clazz.getMethod("is" + name, paramType);
        }

        if (getter == null)
        {
            throw new NoSuchMethodException(clazz.getName() + "类中不存在属性为" + fieldName + "的set方法");
        }

        return getter;
    }

    /**
     * 根据对象的类型和属性名获取其对应的set方法
     *
     * @param fieldName
     *            属性名
     * @param clazz
     *            类名
     * @return 该属性对应的set方法
     * @throws NoSuchMethodException
     */
    public static Method getSetter(String fieldName, Class clazz) throws NoSuchMethodException
    {
        // 先查找get方法，因为后面需要根据get方法的返回值来获取对应的set方法
        Method getter = getGetter(fieldName, clazz);

        String name = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method setter = clazz.getMethod("set" + name, getter.getReturnType());

        if (setter == null)
        {
            throw new NoSuchMethodException(clazz.getName() + "类中不存在属性为" + fieldName + "的get方法");
        }

        return setter;
    }

    /**
     * 将来源对象转换成目标类型的对象
     *
     * @param obj
     *            来源对象
     * @param type
     *            目标类型
     * @return 目标类型的对象
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     */
    public static Object convertObjectType(Object obj, Class type) throws NoSuchMethodException,
            IllegalArgumentException
    {
        if (type.equals(Long.class) || type.equals(long.class))
        {
            if (obj != null)
            {
                return Long.valueOf(obj.toString());
            }
            else
            {
                return 0L;
            }
        }
        else
            if (type.equals(Integer.class) || type.equals(int.class))
            {
                if (obj != null)
                {
                    return Integer.valueOf(obj.toString());
                }
                else
                {
                    return 0;
                }
            }
            else
                if (type.equals(Float.class) || type.equals(float.class))
                {
                    if (obj != null)
                    {
                        return Float.valueOf(obj.toString());
                    }
                    else
                    {
                        return 0f;
                    }
                }
                else
                    if (type.equals(Double.class) || type.equals(double.class))
                    {
                        if (obj != null)
                        {
                            return Double.valueOf(obj.toString());
                        }
                        else
                        {
                            return 0d;
                        }
                    }
                    else
                        if (type.equals(Boolean.class) || type.equals(boolean.class))
                        {
                            if (obj != null)
                            {
                                return Boolean.valueOf(obj.toString());
                            }
                            else
                            {
                                return false;
                            }
                        }
                        else
                            if (type.equals(Date.class))
                            {
                                if (obj != null)
                                {
                                    return TimeUtil.parse(obj.toString(), TimeUtil.FORMAT_NORMAL);
                                }
                                else
                                {
                                    return null;
                                }
                            }
                            else
                                if (type.equals(String.class))
                                {
                                    if (obj != null)
                                    {
                                        return obj.toString();
                                    }
                                    else
                                    {
                                        return null;
                                    }
                                }
                                else
                                {
                                    throw new NoSuchMethodException("无法将" + obj.toString() + "转换为" + type.getName()
                                            + "类型");
                                }
    }

    /**
     * 在对象之间复制属性（成员变量的值）
     *
     * @param from
     *            来源对象
     * @param to
     *            目标对象
     * @param ignoreSetError
     *            如果目标对象不存在对应的setter方法，是否忽略
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    static public void copy(Object from, Object to, boolean ignoreSetError) throws NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Class clazz = null;

        if (from == null || to == null)
        {
            return;
        }

        clazz = from.getClass();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            Method getter = getGetter(field.getName(), from.getClass());
            Object value = getter.invoke(from);

            try
            {
                Method setter = getSetter(field.getName(), from.getClass());
                setter.invoke(to, value);
            }
            catch (NoSuchMethodException e)
            {
                if (!ignoreSetError)
                {
                    throw e;
                }
            }
            catch (IllegalAccessException e)
            {
                if (!ignoreSetError)
                {
                    throw e;
                }
            }
            catch (IllegalArgumentException e)
            {
                if (!ignoreSetError)
                {
                    throw e;
                }
            }
            catch (InvocationTargetException e)
            {
                if (!ignoreSetError)
                {
                    throw e;
                }
            }
        }
    }

    /**
     * 比较两个POJO对象的差别
     *
     * @param o1
     *            对象1
     * @param o2
     *            对象2
     * @return 差别，该Map的Key值为有差别的属性名（成员变量名）， Value为一个长度为2的数组，分别存放两个对象中该成员变量对象的值
     */
    static public Map<String, Object[]> diff(Object o1, Object o2)
    {
        Class clazz = null;

        if (o1 == null && o2 == null)
        {
            return null;
        }
        else
            if (o1 == null)
            {
                clazz = o2.getClass();
            }
            else
                if (o2 == null)
                {
                    clazz = o1.getClass();
                }
                else
                    if (!o1.getClass().equals(o2.getClass()))
                    {
                        return null;
                    }
                    else
                    {
                        clazz = o1.getClass();
                    }

        Map<String, Object[]> difference = new LinkedHashMap<String, Object[]>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
        {
            Method getter = null;
            try
            {
                getter = getGetter(field.getName(), clazz);
            }
            catch (NoSuchMethodException e)
            {
                continue;
            }

            if (getter == null)
            {
                continue;
            }

            Object[] values = new Object[2];
            try
            {
                if (o1 != null)
                {
                    values[0] = getter.invoke(o1);
                }

                if (o2 != null)
                {
                    values[1] = getter.invoke(o2);
                }

                if (values[0] != null && values[1] != null)
                {
                    if (!values[0].equals(values[1]))
                    {
                        difference.put(field.getName(), values);
                    }
                }
                else
                    if (values[0] != null || values[1] != null)
                    {
                        difference.put(field.getName(), values);
                    }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }

        return difference;
    }

}
