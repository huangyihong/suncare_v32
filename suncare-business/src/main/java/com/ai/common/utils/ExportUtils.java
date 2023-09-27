package com.ai.common.utils;

import com.ai.modules.config.service.IMedicalDictService;
import jxl.format.UnderlineStyle;
import jxl.write.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.Boolean;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Auther: zhangpeng
 * @Date: 2020/5/6 10
 * @Description:
 */

@Slf4j
@Component
public class ExportUtils {

//    private static ISysDictService dictService;

    private static IMedicalDictService medicalDictService;


  /*  @Autowired
    public void setSysDictService(ISysDictService dictService) {
        ExportUtils.dictService = dictService;
    }*/
    @Autowired
    public void setMedicalDictService(IMedicalDictService medicalDictService) {
        ExportUtils.medicalDictService = medicalDictService;
    }


    /**
     * 读取注解  翻译医疗字典项、excel注解的标题，宽度，数据格式化   系统字典项由于依赖关系所限暂时不支持
     * @param objs
     * @param clazz
     * @param mappingFields
     * @param sheet
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> int exportExl(List<T> objs,Class<T> clazz, String[] mappingFields, WritableSheet sheet, String pageName) throws Exception {

        int startHang = 0;
        Field[] fields = getAllFields(clazz, Arrays.asList(mappingFields));

//        ApiModel apiModel = clazz.getAnnotation(ApiModel.class);
        if(StringUtils.isNotBlank(pageName)){
            //设置大标题字体;
            WritableFont fontName = new WritableFont(WritableFont.ARIAL,18,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
            WritableCellFormat cellFormatName = new WritableCellFormat(fontName);
            cellFormatName.setAlignment(Alignment.CENTRE);
            cellFormatName.setVerticalAlignment(VerticalAlignment.CENTRE);

            sheet.mergeCells(0, 0, fields.length - 1, 0);//设置第一列、第一行和 第一列、第二行合并
            sheet.setRowView(0, 700);
            sheet.addCell(new Label(0, startHang, pageName, cellFormatName));
            startHang++;
        }

        //设置标题字体;
        WritableFont fontTitle = new WritableFont(WritableFont.ARIAL,14,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
        WritableCellFormat cellFormatTitle = new WritableCellFormat(fontTitle);
        cellFormatTitle.setVerticalAlignment(VerticalAlignment.CENTRE);
        cellFormatTitle.setBackground(Colour.IVORY);
        cellFormatTitle.setBorder(Border.ALL, BorderLineStyle.THIN);

        BeanProp[] beanProps = new BeanProp[fields.length];
        for (int i = 0, len = fields.length; i < len; i++){
            Field field = fields[i];
            BeanProp beanProp = new BeanProp();
            if (field.getAnnotation(Dict.class) != null) {
                beanProp.setDict(field.getAnnotation(Dict.class));
            }
            if (field.getAnnotation(MedicalDict.class) != null) {
                beanProp.setMedicalDict(field.getAnnotation(MedicalDict.class));
            }
            if (field.getAnnotation(Excel.class) != null) {
                Excel excel = field.getAnnotation(Excel.class);
                beanProp.setExcel(excel);
                // out put title
                sheet.addCell(new Label(i, startHang, excel.name(), cellFormatTitle));
                // 设置宽度
                sheet.setColumnView(i, (int) (excel.width() * 1.5));
            }
            beanProp.setField(field);
            beanProps[i] = beanProp;
        }

        sheet.setRowView(startHang, 500);

        startHang++;

        // out put body
        for(T obj: objs){
            Label[] labels = objectToLabels(obj, beanProps, 0,
                    startHang++);
            for (Label label : labels) {
                sheet.addCell(label);
            }
        }

        return startHang;
    }

    public static <T> int exportExl(List<T> objs,Class<T> clazz, String[] titles, String[] mappingFields, WritableSheet sheet, String pageName) throws Exception {
        if(titles.length != mappingFields.length){
            throw new Exception("标题数与字段数长度不一致");
        }
        int startLie = 0,startHang=0;

        if(StringUtils.isNotBlank(pageName) ){

            //设置大标题字体;
            WritableFont fontName = new WritableFont(WritableFont.ARIAL,18,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
            WritableCellFormat cellFormatName = new WritableCellFormat(fontName);
            cellFormatName.setAlignment(Alignment.CENTRE);
            cellFormatName.setVerticalAlignment(VerticalAlignment.CENTRE);

            sheet.mergeCells(0, 0, mappingFields.length - 1, 0);//设置第一列、第一行和 第一列、第二行合并
            sheet.setRowView(0, 700);
            sheet.addCell(new Label(0, startHang, pageName, cellFormatName));

            startHang++;
        }



        //设置标题字体;
        WritableFont fontTitle = new WritableFont(WritableFont.ARIAL,14,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
        WritableCellFormat cellFormatTitle = new WritableCellFormat(fontTitle);
        cellFormatTitle.setVerticalAlignment(VerticalAlignment.CENTRE);
        cellFormatTitle.setBackground(Colour.IVORY);
        cellFormatTitle.setBorder(Border.ALL, BorderLineStyle.THIN);

        Field[] fields = getAllFields(clazz, Arrays.asList(mappingFields));

        BeanProp[] beanProps = new BeanProp[fields.length];
        for (int i = 0, len = fields.length; i < len; i++){
            Field field = fields[i];
            BeanProp beanProp = new BeanProp();
            if (field.getAnnotation(Dict.class) != null) {
                beanProp.setDict(field.getAnnotation(Dict.class));
            }
            if (field.getAnnotation(MedicalDict.class) != null) {
                beanProp.setMedicalDict(field.getAnnotation(MedicalDict.class));
            }
            if (field.getAnnotation(Excel.class) != null) {
                Excel excel = field.getAnnotation(Excel.class);
                beanProp.setExcel(excel);
                // out put title
                sheet.addCell(new Label(i, startHang, titles[i], cellFormatTitle));
                // 设置宽度
                sheet.setColumnView(i, (int) (excel.width() * 1.5));
            }else {
            	//bean没有excel注解
            	 sheet.addCell(new Label(i, startHang, titles[i], cellFormatTitle));
            	// 设置宽度
                 sheet.setColumnView(i,  (int) (15 * 1.5));
            }
            beanProp.setField(field);
            beanProps[i] = beanProp;
        }

        sheet.setRowView(startHang, 500);

        startHang++;

        // out put body
        for(T obj: objs){
            Label[] labels = objectToLabels(obj, beanProps, startLie,
                    startHang++);
            for (Label label : labels) {
                sheet.addCell(label);
            }
        }

        return startHang;
    }

    public static int exportExl(List<Map<String, Object>> list, String[] titles, String[] mappingFields, WritableSheet sheet, String pageName) throws Exception {
        if(titles.length != mappingFields.length){
            throw new Exception("标题数与字段数长度不一致");
        }
        int startLie = 0,startHang=0;

        if(StringUtils.isNotBlank(pageName) ){

            //设置大标题字体;
            WritableFont fontName = new WritableFont(WritableFont.ARIAL,18,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
            WritableCellFormat cellFormatName = new WritableCellFormat(fontName);
            cellFormatName.setAlignment(Alignment.CENTRE);
            cellFormatName.setVerticalAlignment(VerticalAlignment.CENTRE);

            sheet.mergeCells(0, 0, mappingFields.length - 1, 0);//设置第一列、第一行和 第一列、第二行合并
            sheet.setRowView(0, 700);
            sheet.addCell(new Label(0, startHang, pageName, cellFormatName));

            startHang++;
        }



        //设置标题字体;
        WritableFont fontTitle = new WritableFont(WritableFont.ARIAL,14,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
        WritableCellFormat cellFormatTitle = new WritableCellFormat(fontTitle);
        cellFormatTitle.setVerticalAlignment(VerticalAlignment.CENTRE);
        cellFormatTitle.setBackground(Colour.IVORY);
        cellFormatTitle.setBorder(Border.ALL, BorderLineStyle.THIN);


        for (int i = 0, len = mappingFields.length; i < len; i++){
            //bean没有excel注解
            sheet.addCell(new Label(i, startHang, titles[i], cellFormatTitle));
            // 设置宽度
            sheet.setColumnView(i,  (int) (15 * 1.5));

        }

        sheet.setRowView(startHang, 500);

        startHang++;

        // out put body
        for(Map<String, Object> map: list){
            Label[] labels = objectToLabels(map, mappingFields, startLie,
                    startHang++);
            for (Label label : labels) {
                sheet.addCell(label);
            }
        }

        return startHang;
    }

    public static int exportExl(List<Map<String, Object>> list, String[] titles, String[] mappingFields,WritableWorkbook wwb, String sheetName) throws Exception {
        if(titles.length != mappingFields.length){
            throw new Exception("标题数与字段数长度不一致");
        }
        int startLie = 0,startHang=0;

        WritableSheet sheet = wwb.createSheet(sheetName, 0);
        //设置标题字体;
        WritableFont fontTitle = new WritableFont(WritableFont.ARIAL,14,WritableFont.NO_BOLD,false, UnderlineStyle.NO_UNDERLINE,Colour.BLACK);
        WritableCellFormat cellFormatTitle = new WritableCellFormat(fontTitle);
        cellFormatTitle.setVerticalAlignment(VerticalAlignment.CENTRE);
        cellFormatTitle.setBackground(Colour.IVORY);
        cellFormatTitle.setBorder(Border.ALL, BorderLineStyle.THIN);


        for (int i = 0, len = mappingFields.length; i < len; i++){
            //bean没有excel注解
            sheet.addCell(new Label(i, startHang, titles[i], cellFormatTitle));
            // 设置宽度
            sheet.setColumnView(i,  (int) (15 * 1.5));

        }

        sheet.setRowView(startHang, 500);

        startHang++;

        // out put body
        for(Map<String, Object> map: list){
            Label[] labels = objectToLabels(map, mappingFields, startLie,
                    startHang++);
            for (Label label : labels) {
                sheet.addCell(label);
            }
        }

        return startHang;
    }


    private static Label[] objectToLabels(Map<String, Object> map, String[] fields, int startRow, int currLine)
            throws Exception {

        int len = fields.length;

        Label[] labels = new Label[len];

        for (int i = 0; i < len; i++) {
            Object value = map.get(fields[i]);
            if(value == null){
                value = "";
            }

            if (validateType(value)) {
                if (value instanceof Date) {
                    labels[i] = new Label(startRow++, currLine, TimeUtil.format((Date) value, TimeUtil.FORMAT_NORMAL),
                            new WritableCellFormat(NumberFormats.TEXT));
                }  else if(value instanceof Double || value instanceof BigDecimal){
                    String valStr = String.valueOf(value);
                    if(valStr.endsWith(".0")){
                        valStr = valStr.substring(0,valStr.length() - 2);
                    } else {
                        valStr = new BigDecimal(valStr).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    }
                    labels[i] = new Label(startRow++, currLine, valStr, new WritableCellFormat(NumberFormats.TEXT));
                } else {
                    labels[i] = new Label(startRow++, currLine, String.valueOf(value), new WritableCellFormat(NumberFormats.TEXT));
                }
            }
        }

        return labels;
    }

    private static Label[] objectToLabels(Object obj, BeanProp[] beanProps, int startRow, int currLine)
            throws Exception {

        int len = beanProps.length;

        Label[] labels = new Label[len];

        for (int i = 0; i < len; i++) {
            BeanProp beanProp = beanProps[i];

            Field field = beanProp.getField();
            field.setAccessible(true);
            Object value = field.get(obj);
            if(value != null){
                // 依赖关系所限暂时不支持
                /*if(beanProp.getDict() != null){
                    Dict dict = beanProp.getDict();
                    //翻译字典值对应的txt
                    value = translateDictValue(dict.dicCode(), dict.dicText(), dict.dictTable(), String.valueOf(value));
                } else */if(beanProp.getMedicalDict() != null){
                    MedicalDict dict = beanProp.getMedicalDict();
                    //翻译字典值对应的txt
                    value = translateMedicalDictValue(dict.dicCode(), String.valueOf(value));
                } else {
                    Excel excelProp = beanProp.getExcel();
                    if(excelProp != null && excelProp.format().length() > 0) {
                        if(field.getType() == Date.class){
                            SimpleDateFormat aDate = new SimpleDateFormat(excelProp.format());
                            value = aDate.format((Date)value);
                        } else {
                            value = String.format(excelProp.format(), value);
                        }
                    }
                }
            } else {
                value = "";
            }

            if (validateType(value)) {
                if (value instanceof Date) {
                    labels[i] = new Label(startRow++, currLine, TimeUtil.format((Date) value, TimeUtil.FORMAT_NORMAL),
                            new WritableCellFormat(NumberFormats.TEXT));
                } else {
                    labels[i] = new Label(startRow++, currLine, String.valueOf(value), new WritableCellFormat(NumberFormats.TEXT));
                }
            }
        }

        return labels;
    }

    /**
     * 输出的属性所支持类型验证
     *
     * @param type
     * @return
     */
    private static boolean validateType(Object obj) throws Exception {
        if (obj.getClass().equals(String.class)
                || obj.getClass().equals(Long.class)
                || obj.getClass().equals(Byte.class)
                || obj.getClass().equals(Double.class)
                || obj.getClass().equals(Float.class)
                || obj.getClass().equals(Short.class)
                || obj.getClass().equals(Integer.class)
                || obj.getClass().equals(Boolean.class)
                || obj.getClass().equals(Date.class)
                || obj.getClass().equals(BigDecimal.class)
                || obj.getClass().equals(Timestamp.class)) {
            return true;
        }

        throw new Exception("Unsupport type：" + obj.getClass().getName());
    }

    public static Field[] getAllFields(Class clazz, List<String> mappingFields) {
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

    private static String translateMedicalDictValue(String code, String key) {
        if(oConvertUtils.isEmpty(key)) {
            return "";
        }
        StringBuilder textValue = new StringBuilder();
        String[] keys_group = key.split("\\|");
        for(int i=0;i<keys_group.length;i++) {
        	 String[] keys = keys_group[i].split(",");
             for (int j=0;j<keys.length;j++) {
            	 String k = keys[j];
                 String tmpValue = null;
                 log.debug(" 字典 key : "+ k);
                 if (k.trim().length() == 0) {
                     continue; //跳过循环
                 }
                 tmpValue = medicalDictService.queryDictTextByKey(code, k.trim());

                 if (tmpValue != null) {
                     textValue.append(tmpValue);
                     if(j!=keys.length-1&&!"".equals(textValue.toString())) {
                    	 textValue.append(",");
                     }
                 }

             }
             if(i!=keys_group.length-1&&!"".equals(textValue.toString())) {
            	 textValue.append("|");
             }
        }

        return textValue.toString();
    }


    @Data
    private static class BeanProp {
        private Field field;
        private Excel excel;
        private Dict dict;
        private MedicalDict medicalDict;
    }
}
