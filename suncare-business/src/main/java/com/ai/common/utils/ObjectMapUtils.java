package com.ai.common.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ai.modules.config.entity.MedicalDrug;

public class ObjectMapUtils {
	 /***
     * 将对象转换为map对象
     * @param thisObj 对象
     * @return
     */
    public static Map objectToMap(Object thisObj)
    {
        Map map = new HashMap();
        Class c;
        try
        {
            c = Class.forName(thisObj.getClass().getName());
            //获取所有的方法
            Method[] m = c.getMethods();
            for (int i = 0; i < m.length; i++)
            {   //获取方法名
                String method = m[i].getName();
                //获取get开始的方法名
                if (method.startsWith("get")&&!method.contains("getClass"))
                {
                    try{
                        //获取对应对应get方法的value值
                        Object value = m[i].invoke(thisObj);
                        if (value != null)
                        {
                            //截取get方法除get意外的字符 如getUserName-->UserName
                            String key=method.substring(3);
                            //将属性的第一个值转为小写
                            //key=key.substring(0,1).toLowerCase()+key.substring(1);
                            key = StringCamelUtils.camel2Underline(key);
                            //将属性key,value放入对象
                            map.put(key, value);
                        }
                    }catch (Exception e) {
                        // TODO: handle exception
                        System.out.println("error:"+method);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        return map;
    }


    /**
     * 将Map对象通过反射机制转换成Bean对象
     *
     * @param map 存放数据的map对象
     * @param clazz 待转换的class
     * @return 转换后的Bean对象
     * @throws Exception 异常
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) throws Exception {
        T obj = clazz.newInstance();
        if(map != null && map.size() > 0) {
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                String propertyName = entry.getKey();       //属性名
                propertyName = StringCamelUtils.underline2Camel(propertyName);
                Object value = entry.getValue();
                String setMethodName = "set"
                        + propertyName.substring(0, 1).toUpperCase()
                        + propertyName.substring(1);
                //获取属性对应的对象字段
                Field field = getClassField(clazz, propertyName);
                if(field==null)
                    continue;
                //获取字段类型
                Class<?> fieldTypeClass = field.getType();
                //根据字段类型进行值的转换
                value = convertValType(value, fieldTypeClass);
                try{
                    //调用对象对应的set方法
//                	System.out.println(propertyName);
                    clazz.getMethod(setMethodName, field.getType()).invoke(obj, value);
                }catch(NoSuchMethodException e){
                    e.printStackTrace();
                }
            }
        }
        return obj;
    }

    /**
     * 获取指定字段名称查找在class中的对应的Field对象(包括查找父类)
     *
     * @param clazz 指定的class
     * @param fieldName 字段名称
     * @return Field对象
     */
    private static Field getClassField(Class<?> clazz, String fieldName) {
        if( Object.class.getName().equals(clazz.getName())) {
            return null;
        }
        Field []declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if(superClass != null) {// 简单的递归一下
            return getClassField(superClass, fieldName);
        }
        return null;
    }

    /**
     * 将Object类型的值，转换成bean对象属性里对应的类型值
     *
     * @param value Object对象值
     * @param fieldTypeClass 属性的类型
     * @return 转换后的值
     */
    private static Object convertValType(Object value, Class<?> fieldTypeClass) {
        Object retVal = null;
        if(Long.class.getName().equals(fieldTypeClass.getName())
                || long.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Long.parseLong(value.toString());
        } else if(Short.class.getName().equals(fieldTypeClass.getName())
                || short.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Short.parseShort(value.toString());
        } else if(Integer.class.getName().equals(fieldTypeClass.getName())
                || int.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Integer.parseInt(value.toString());
        } else if(Float.class.getName().equals(fieldTypeClass.getName())
                || float.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Float.parseFloat(value.toString());
        } else if(Double.class.getName().equals(fieldTypeClass.getName())
                || double.class.getName().equals(fieldTypeClass.getName())) {
            retVal = Double.parseDouble(value.toString());
        } else if(BigDecimal.class.getName().equals(fieldTypeClass.getName())) {
            retVal = new BigDecimal(value.toString());
        } else if(Date.class.getName().equals(fieldTypeClass.getName())) {
        	if(StringUtils.isNotBlank(value.toString())){
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        		try {
					retVal = sdf.parse(value.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}

        } else {
        	if (value instanceof String || value instanceof ArrayList) {
        		 retVal = value;
        	}else{
        		 retVal = value.toString();
        	}
        }
        return retVal;
    }

    public static void main(String... args) throws Exception{
    	MedicalDrug bean = new MedicalDrug();
        bean.setCode("111");
        bean.setDosageType("222");
        bean.setId("id");
        bean.setName("name");

        //对象转map
        Map map = objectToMap(bean);
        map.put("DOSAGE_TYPE333", "22224444");
        MedicalDrug newUser = (MedicalDrug) mapToObject(map,MedicalDrug.class);


    }
}
