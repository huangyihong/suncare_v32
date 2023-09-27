package com.ai.common.utils;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @Auther: zhangpeng
 * @Date: 2020/7/8 17
 * @Description:
 */
public class ImportXUtils {
    private static <T> List<T> readSheet(Class<T> clazz, String[] mappingFields, Sheet sheet, int startLine)
            throws Exception {
        List<T> datas = new ArrayList<>();
        Field[] fields = getAllFields(clazz, Arrays.asList(mappingFields));
        int rowCount = sheet.getLastRowNum();
        int lineCount = mappingFields.length;
        // loop the 行
        for (int i = startLine; i <= rowCount; i++) {
            Row row = sheet.getRow(i);
            T obj = clazz.newInstance();
            for(int j = 0; j < lineCount; j++){
                Field field = fields[j];

                String valueStr = row.getCell(j).getStringCellValue();
                Object value = ImportXUtils.castToType(field.getType(), valueStr);

                field.setAccessible(true);
                field.set(obj, value);
                field.setAccessible(false);
            }
            datas.add(obj);
        }
        return datas;
    }

    private static Field[] getAllFields(Class clazz, List<String> mappingFields) {
        Map<String, Field> map = new HashMap<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field: fields){
                map.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }


        Field[] fields = mappingFields.stream().map(map::get).toArray(Field[]::new);
        return fields;
    }


    private static Object castToType(Class<?> returnType, String value) throws Exception {
        if (returnType.equals(String.class)) {
            return value;
        } else if (returnType.equals(Boolean.TYPE)) {
            return Boolean.valueOf(value);
        } else if (returnType.equals(Integer.TYPE)) {
            return Integer.valueOf(value);
        } else if (returnType.equals(Short.TYPE)) {
            return Short.valueOf(value);
        } else if (returnType.equals(Float.TYPE)) {
            return Float.valueOf(value);
        } else if (returnType.equals(Double.TYPE)) {
            return Double.valueOf(value);
        } else if (returnType.equals(Byte.TYPE)) {
            return Byte.valueOf(value);
        } else if (returnType.equals(Long.TYPE)) {
            return Long.valueOf(value);
        } else if (returnType.toString().contains("java.lang.Long")) {
            return Long.valueOf(value);
        } else if (returnType.toString().contains("java.math.BigDecimal")) {
            return new BigDecimal(value);
        } else if (returnType.toString().contains("java.lang.Integer")) {
            return new Integer(value);
        } else if (returnType.toString().contains("java.util.Date")) {
            value = value.trim();
            String dateFormat = "";
            if (value.length() == 10) dateFormat = TimeUtil.FORMAT_DATE_ONLY;
            else if (value.length() == 19) dateFormat = TimeUtil.FORMAT_NORMAL;
            else
                throw new Exception(String.format("非法日期格式（合法为%s、%s）:%s", TimeUtil.FORMAT_DATE_ONLY, TimeUtil.FORMAT_NORMAL, value));
            Date date = TimeUtil.parse(value, dateFormat);
            return date;
        } else if (returnType.toString().contains("java.sql.Timestamp")) {
            value = value.trim();
            String dateFormat = "";
            if (value.length() == 10) dateFormat = TimeUtil.FORMAT_DATE_ONLY;
            else if (value.length() == 19) dateFormat = TimeUtil.FORMAT_NORMAL;
            else
                throw new Exception(String.format("非法日期格式（合法为%s、%s）:%s", TimeUtil.FORMAT_DATE_ONLY, TimeUtil.FORMAT_NORMAL, value));
            Date date = TimeUtil.parse(value, dateFormat);
            return new Timestamp(date.getTime());

        } else {
            throw new Exception("unsupport Type: " + returnType.toString()
                    + "; cell contents: " + value);
        }
    }
}
