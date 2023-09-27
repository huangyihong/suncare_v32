package com.ai.common.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecg.common.util.oConvertUtils;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.modules.config.service.IMedicalDictService;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Auther: zhangpeng
 * @Date: 2020/5/6 10
 * @Description:
 */

@Slf4j
@Component
public class ExportXUtils {

//    private static ISysDictService dictService;

    private static IMedicalDictService medicalDictService;

    public static int DEFAULT_WIDTH = 15;


  /*  @Autowired
    public void setSysDictService(ISysDictService dictService) {
        ExportUtils.dictService = dictService;
    }*/
    @Autowired
    public void setMedicalDictService(IMedicalDictService medicalDictService) {
        ExportXUtils.medicalDictService = medicalDictService;
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
    public static <T> int exportExl(List<T> objs, Class<T> clazz, String[] mappingFields, SXSSFWorkbook workbook, String sheetName) throws Exception {

        // 生成一个表格
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);

        int startHang = 0;
        Field[] fields = getAllFields(clazz, Arrays.asList(mappingFields));

        // 设置标题样式
        CellStyle titleStyle = initTitleStyle(workbook);

        List<BeanProp> beanProps = new ArrayList<>();
        Row rowTitle = sheet.createRow(startHang);
        rowTitle.setHeight((short) 500);
        for (int i = 0, j = 0, len = fields.length; j < len; j++){
            Field field = fields[j];
            if(field == null){
                continue;
            }
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
                Cell cell = rowTitle.createCell(i);
                cell.setCellValue(excel.name());
                cell.setCellStyle(titleStyle);
                sheet.setColumnWidth(i, (int) (excel.width() * 256));
            } else {
                sheet.setColumnWidth(i, DEFAULT_WIDTH * 256);
            }

            beanProp.setField(field);
            beanProps.add(beanProp);
            i++;
        }

        startHang++;

        // out put body
        for(T obj: objs){
            Row row = sheet.createRow(startHang++);
            objectToRow(obj, beanProps, row);
        }

        return startHang;
    }

    public static <T> int exportExl(List<T> objs, Class<T> clazz,String[] titles, String[] mappingFields, SXSSFWorkbook workbook, String sheetName) throws Exception {

        // 生成一个表格
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);

        int startHang = 0;
        Field[] fields = getAllFields(clazz, Arrays.asList(mappingFields));

        // 设置标题样式
        CellStyle titleStyle = initTitleStyle(workbook);

        List<BeanProp> beanProps = new ArrayList<>();
        Row rowTitle = sheet.createRow(startHang);
        rowTitle.setHeight((short) 500);
        for (int i = 0,j = 0, len = fields.length; j < len; j++){
            Field field = fields[j];

            if(field == null){
                continue;
            }
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
                // 设置列宽
                sheet.setColumnWidth(i, (int) (excel.width() * 256));
            } else {
                sheet.setColumnWidth(i, DEFAULT_WIDTH * 256);
            }

            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(titles[j]);
            cell.setCellStyle(titleStyle);
            beanProp.setField(field);
            beanProps.add(beanProp);
            i++;
        }

        startHang++;

        // out put body
        for(T obj: objs){
            Row row = sheet.createRow(startHang++);
            objectToRow(obj, beanProps, row);
        }

        return startHang;
    }



    public static <T> int exportExl(List<Map<String, Object>> maps,String[] titles, String[] mappingFields, SXSSFWorkbook workbook, String sheetName) throws Exception {

        // 生成一个表格
        SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);

        int startHang = 0;

        // 设置标题样式
        CellStyle titleStyle = initTitleStyle(workbook);

        Row rowTitle = sheet.createRow(startHang);
        rowTitle.setHeight((short) 500);
        for (int i = 0, len = titles.length; i < len; i++){
            String title = titles[i];
            Cell cell = rowTitle.createCell(i);
            cell.setCellValue(title);
            cell.setCellStyle(titleStyle);
            sheet.setColumnWidth(i, DEFAULT_WIDTH * 256 );
        }

        startHang++;

        // out put body
        for(Map<String, Object> map: maps){
            Row row = sheet.createRow(startHang++);
            mapToRow(map, mappingFields, row);
        }

        return startHang;
    }

    public static CellStyle initTitleStyle(SXSSFWorkbook workbook){
        //设置标题字体;
        Font font = workbook.createFont();
        // 设置字体大小
        font.setFontHeightInPoints((short)11);
        // 字体加粗
        font.setBold(true);
        // 设置字体名字
        font.setFontName("Courier New");
        // 设置样式
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(font);
        // 设置单元格居中对齐
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        // 设置单元格居中对齐
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 创建单元格内容不显示自动换行
        titleStyle.setWrapText(false);
        titleStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        //solid 填充  foreground  前景色
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        titleStyle.setBorderBottom(BorderStyle.THIN);
        titleStyle.setBottomBorderColor(IndexedColors.BLACK.index);
        titleStyle.setBorderLeft(BorderStyle.THIN);
        titleStyle.setLeftBorderColor(IndexedColors.BLACK.index);
        titleStyle.setBorderRight(BorderStyle.THIN);
        titleStyle.setRightBorderColor(IndexedColors.BLACK.index);
        titleStyle.setBorderTop(BorderStyle.THIN);
        titleStyle.setTopBorderColor(IndexedColors.BLACK.index);

        return titleStyle;
    }


    public static void objectToRow(Object obj, List<BeanProp> beanProps, Row row)
            throws Exception {


        for (int i = 0, len = beanProps.size(); i < len; i++) {
            BeanProp beanProp = beanProps.get(i);
            Field field = beanProp.getField();
            field.setAccessible(true);
            Object value = field.get(obj);
            if(value == null){
                value = "";
            } else {
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
            }

//            if (validateType(value)) {
                if (value instanceof Date) {
                    row.createCell(i).setCellValue(TimeUtil.format((Date) value, TimeUtil.FORMAT_NORMAL));
                } else if(value instanceof Double || value instanceof BigDecimal){
                    String valStr = String.valueOf(value);
                    if(valStr.endsWith(".0")){
                        valStr = valStr.substring(0,valStr.length() - 2);
                    }else {
                        valStr = new BigDecimal(valStr).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    }
                    row.createCell(i).setCellValue(valStr);
                } else if(value instanceof ArrayList){
                    row.createCell(i).setCellValue(StringUtils.join((ArrayList)value, ","));
                } else {
                    row.createCell(i).setCellValue(String.valueOf(value));
                }
//            }
        }
    }

    private static void mapToRow(Map<String, Object> map, String[] fields, Row row)
            throws Exception {

        int len = fields.length;

        for (int i = 0; i < len; i++) {
            String field = fields[i];
            Object value = map.get(field);
            if(value == null){
                value = "";
            }
//            if (validateType(value)) {
                if (value instanceof Date) {
                    row.createCell(i).setCellValue(TimeUtil.format((Date) value, TimeUtil.FORMAT_NORMAL));
                } else if(value instanceof ArrayList){
                    row.createCell(i).setCellValue(StringUtils.join((ArrayList)value, ","));
                } else if(value instanceof Double){
                    String val = String.valueOf(value);
                    row.createCell(i).setCellValue(val.endsWith(".0")?val.substring(0, val.length() - 2): val);
                } else {
                    row.createCell(i).setCellValue(String.valueOf(value));
                }
//            }
        }
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
                || obj.getClass().equals(ArrayList.class)) {
            return true;
        }

        throw new Exception("Unsupport type：" + obj.getClass().getName());
    }

    public static Field[] getAllFields(Class clazz, List<String> mappingFields) throws Exception {
        Map<String, Field> map = new HashMap<>();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for(Field field: fields){
                map.put(field.getName(), field);
            }
            clazz = clazz.getSuperclass();
        }

        Field[] fields = new Field[mappingFields.size()];
        for(int i = 0, len = mappingFields.size(); i < len; i++){
            String mappingField = mappingFields.get(i);
            if(StringUtils.isNotBlank(mappingField)){
                fields[i] = map.get(mappingField);
                if(fields[i]  == null){
                    throw new Exception("字段在类中不存在：" + mappingField);
                }
            }
        }
//        Field[] fields = mappingFields.stream().map(map::get).toArray(Field[]::new);
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
    static class BeanProp {
        private Field field;
        private Excel excel;
        private Dict dict;
        private MedicalDict medicalDict;
        public BeanProp(){

        }

        public BeanProp(Field field){
            this.field = field;
        }
    }


    public static <T> ExportXTarget.Page<T> initExport(Class<T> clazz,String[] titles, String[] mappingFields, String sheetName) throws Exception {

        ExportXTarget exportTarget = new ExportXTarget();

        return exportTarget.createPage(clazz,
                titles, mappingFields, sheetName);
    }

   /* @Data
    public static class ExportTarget<T> {
        private Class<T> clazz;
        private String[] titles;
        private String[] mappingFields;

        private SXSSFWorkbook workbook;
        private BeanProp[] beanProps;
        private Integer lineNum;
        private Sheet sheet;

        ExportTarget(Class<T> clazz,String[] titles, String[] mappingFields){
            this.clazz = clazz;
            this.titles = titles;
            this.mappingFields = mappingFields;

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
                }
                beanProp.setField(field);
                beanProps[i] = beanProp;
            }

            this.beanProps = beanProps;
            this.workbook = new SXSSFWorkbook();
        }

        public void write(T obj) throws Exception {
            Row row = sheet.createRow(lineNum++);
            objectToRow(obj, beanProps, row);
        }

        public Sheet createSheet(String sheetName){
            // 生成一个表格
            SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);

            this.lineNum = 0;

            // 设置标题样式
            CellStyle titleStyle = initTitleStyle(workbook);

            Row rowTitle = sheet.createRow(this.lineNum);
            rowTitle.setHeight((short) 500);
            for (int i = 0, len = beanProps.length; i < len; i++){
                BeanProp beanProp = beanProps[i];
                if (beanProp.getExcel() != null) {
                    // 设置列宽
                    sheet.setColumnWidth(i, (int) (beanProp.getExcel().width() * 256));
                } else {
                    sheet.setColumnWidth(i, DEFAULT_WIDTH * 256);
                }

                Cell cell = rowTitle.createCell(i);
                cell.setCellValue(titles[i]);
                cell.setCellStyle(titleStyle);
                beanProps[i] = beanProp;
            }
            this.lineNum++;
            return sheet;
        }
    }*/
}
