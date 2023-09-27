package com.ai.common.utils;

import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.common.aspect.annotation.MedicalDict;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Auther: zhangpeng
 * @Date: 2020/9/14 11
 * @Description:
 */
public class ExportXTarget {

    private SXSSFWorkbook workbook;

    public ExportXTarget(){
        this.workbook = new SXSSFWorkbook();
    }

    public <T> Page<T> createPage(Class<T> clazz,String[] titles, String[] mappingFields, String sheetName) throws Exception {
        return new Page<T>(workbook, clazz, titles, mappingFields, sheetName);
    }


    public void write(OutputStream os) throws IOException {
        this.workbook.write(os);
        this.workbook.dispose();
    }

    @Data
    public class Page<T>{

        private SXSSFWorkbook workbook;
        private Integer lineNum;
        private List<ExportXUtils.BeanProp> beanProps;
        private Sheet sheet;
        private Integer sheetCount;

        // initParams
        private Class<T> clazz;
        private String[] titles;
        private Field[] fields;
        private String[] mappingFields;

        Page(SXSSFWorkbook workbook, Class<T> clazz,String[] titles, String[] mappingFields, String sheetName) throws Exception {
            this.workbook = workbook;
            this.clazz = clazz;
            this.titles = titles;
            this.mappingFields = mappingFields;
            this.sheetCount = 0;

            Field[] fields = ExportXUtils.getAllFields(clazz, Arrays.asList(mappingFields));
            List<ExportXUtils.BeanProp> beanProps = new ArrayList<>();
            for (int i = 0, len = fields.length; i < len; i++){
                Field field = fields[i];
                if(field == null){
                    continue;
                }
                ExportXUtils.BeanProp beanProp = new ExportXUtils.BeanProp();
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
                beanProps.add(beanProp);
            }

            this.fields = fields;
            this.beanProps = beanProps;
            this.sheet = this.initSheet(sheetName);
        }

        private Sheet initSheet(String sheetName){
            // 生成一个表格
            SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);
            this.lineNum = 0;
            // 设置标题样式
            CellStyle titleStyle = ExportXUtils.initTitleStyle(workbook);

            Row rowTitle = sheet.createRow(this.lineNum);
            rowTitle.setHeight((short) 500);
            for (int i = 0,j = 0, len = fields.length; j < len; j++){
                if(fields[j] == null){
                    continue;
                }
                ExportXUtils.BeanProp beanProp = beanProps.get(i);
                if (beanProp.getExcel() != null) {
                    // 设置列宽
                    sheet.setColumnWidth(i, (int) (beanProp.getExcel().width() * 256));
                } else {
                    sheet.setColumnWidth(i, ExportXUtils.DEFAULT_WIDTH * 256);
                }

                Cell cell = rowTitle.createCell(i);
                cell.setCellValue(titles[j]);
                cell.setCellStyle(titleStyle);
                i++;
            }
            this.lineNum++;
            return sheet;
        }

        public void write(T obj) throws Exception {
            Row row = sheet.createRow(lineNum++);
            ExportXUtils.objectToRow(obj, beanProps, row);
            if(lineNum == 1000000){
                this.sheetCount++;
                String sheetName = this.sheet.getSheetName();
                int index = workbook.getSheetIndex(sheetName);
                this.sheet = this.initSheet(sheetName + "(" + this.sheetCount + ")");
                workbook.setSheetOrder(this.sheet.getSheetName(), index + 1);
            }
        }

        public void write(OutputStream os) throws IOException {
            this.workbook.write(os);
            this.workbook.dispose();
        }
    }

}
