package com.ai.common.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author vincent
 */
public class ExcelXUtils {
    private static Log logger = LogFactory.getLog(ExcelXUtils.class);
    private static final int DEF_START_ROW = 0; // 默认起始列

    public static final FileFilter EXCEL_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.getName().endsWith(".xlsx")) {
                return true;
            }

            return false;
        }
    };

    /**
     * 将一行数据读取到Object里
     * 每一列值与mappingAttrs定义的Object属性一一对应
     *
     * @param clazz
     * @param cells
     * @param mappingFields
     * @return
     * @throws Exception
     */
    private static Object readLine(XSSFRow xssfRow, Class clazz, String[] mappingFields)
            throws Exception {
        boolean haveData = false;
        Object instance = clazz.newInstance();
        int length = xssfRow.getLastCellNum() > mappingFields.length ? mappingFields.length
                : xssfRow.getLastCellNum();

        for (int j = 0; j < length; j++) {
            if (StringUtils.isBlank(mappingFields[j])) {
                continue;
            }
            XSSFCell cell = xssfRow.getCell(j);
            if(cell!=null&&ExcelTool.getXValue(cell) != null && "内容结束".equals(ExcelTool.getXValue(cell))){
                break;
            }
            if (cell!=null&&ExcelTool.getXValue(cell) != null && !"".equals(ExcelTool.getXValue(cell))) {
                try {
                    haveData = true;
                    String value = ExcelTool.getXValue(cell).trim();

                    // get return type of fieldNames[j]
                    Method getMethod = ReflectHelper.getGetter(mappingFields[j], clazz);
                    Type returnType = getMethod.getReturnType();
                    if (returnType.equals(String.class)) {
                        ReflectHelper.setValue(instance, mappingFields[j], String.valueOf(value));
                    } else if (returnType.equals(Boolean.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Boolean.valueOf(value));
                    } else if (returnType.equals(Integer.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Boolean.valueOf(value));
                    } else if (returnType.equals(Short.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Short.valueOf(value));
                    } else if (returnType.equals(Float.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Float.valueOf(value));
                    } else if (returnType.equals(Double.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Double.valueOf(value));
                    } else if (returnType.equals(Byte.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Byte.valueOf(value));
                    } else if (returnType.equals(Long.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Long.valueOf(value));
                    }

                    else if (returnType.toString().contains("java.lang.Long")) {
                        ReflectHelper.setValue(instance, mappingFields[j], Long.valueOf(value));
                    } else if (returnType.toString().contains("java.lang.Double")) {
                        ReflectHelper.setValue(instance, mappingFields[j], Double.valueOf(value));
                    } else if (returnType.toString().contains("java.math.BigDecimal")) {
                        ReflectHelper.setValue(instance, mappingFields[j], new BigDecimal(value));
                    } else if (returnType.toString().contains("java.lang.Integer")) {
                        ReflectHelper.setValue(instance, mappingFields[j], new Integer(value));
                    } else if (returnType.toString().contains("java.util.Date")) {
                        value = value.trim();
                        value = value.replace("/","-");
                        String dateFormat = "";
                        if (value.length() == 10) dateFormat = TimeUtil.FORMAT_DATE_ONLY;
                        else if (value.length() == 19) dateFormat = TimeUtil.FORMAT_NORMAL;
                        else
                            throw new Exception(String.format("非法日期格式（合法为%s、%s）:%s", TimeUtil.FORMAT_DATE_ONLY, TimeUtil.FORMAT_NORMAL, value));
                        Date date = TimeUtil.parse(value, dateFormat);
                        ReflectHelper.setValue(instance, mappingFields[j], date);

                    } else if (returnType.toString().indexOf("java.sql.Timestamp") != -1) {
                        value = value.trim();
                        String dateFormat = "";
                        if (value.length() == 10) dateFormat = TimeUtil.FORMAT_DATE_ONLY;
                        else if (value.length() == 19) dateFormat = TimeUtil.FORMAT_NORMAL;
                        else
                            throw new Exception(String.format("非法日期格式（合法为%s、%s）:%s", TimeUtil.FORMAT_DATE_ONLY, TimeUtil.FORMAT_NORMAL, value));
                        Date date = TimeUtil.parse(value, dateFormat);
                        ReflectHelper.setValue(instance, mappingFields[j], new Timestamp(date.getTime()));

                    } else {
                        logger.error("unsupport Type: " + returnType.toString()
                                + "; fieldName: " + mappingFields[j]
                                + "; cell contents: " + value);
                    }
                } catch (Exception e) {
                    throw new Exception(e.getMessage() + " [attribute:" + mappingFields[j] + "]");
                }
            }
        }

        if (haveData) {
            return instance;
        }

        return null;
    }

    private static Object readLine(XSSFRow xssfRow, Class clazz, String[] mappingFields,String endFlag)
            throws Exception {
        boolean haveData = false;
        Object instance = clazz.newInstance();
        int length = xssfRow.getLastCellNum() > mappingFields.length ? mappingFields.length
                : xssfRow.getLastCellNum();

        for (int j = 0; j < length; j++) {
            if (StringUtils.isBlank(mappingFields[j])) {
                continue;
            }
            XSSFCell cell = xssfRow.getCell(j);
            if(StrUtil.isNotEmpty(endFlag) && endFlag.equals(ExcelTool.getXValue(cell))){
                haveData = false;
                break;
            }
            if (cell!=null&&ExcelTool.getXValue(cell) != null && !"".equals(ExcelTool.getXValue(cell))) {
                try {
                    haveData = true;
                    String value = ExcelTool.getXValue(cell).trim();

                    // get return type of fieldNames[j]
                    Method getMethod = ReflectHelper.getGetter(mappingFields[j], clazz);
                    Type returnType = getMethod.getReturnType();
                    if (returnType.equals(String.class)) {
                        ReflectHelper.setValue(instance, mappingFields[j], String.valueOf(value));
                    } else if (returnType.equals(Boolean.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Boolean.valueOf(value));
                    } else if (returnType.equals(Integer.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Boolean.valueOf(value));
                    } else if (returnType.equals(Short.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Short.valueOf(value));
                    } else if (returnType.equals(Float.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Float.valueOf(value));
                    } else if (returnType.equals(Double.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Double.valueOf(value));
                    } else if (returnType.equals(Byte.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Byte.valueOf(value));
                    } else if (returnType.equals(Long.TYPE)) {
                        ReflectHelper.setValue(instance, mappingFields[j], Long.valueOf(value));
                    }

                    else if (returnType.toString().contains("java.lang.Long")) {
                        ReflectHelper.setValue(instance, mappingFields[j], Long.valueOf(value));
                    } else if (returnType.toString().contains("java.lang.Double")) {
                        ReflectHelper.setValue(instance, mappingFields[j], Double.valueOf(value));
                    } else if (returnType.toString().contains("java.math.BigDecimal")) {
                        ReflectHelper.setValue(instance, mappingFields[j], new BigDecimal(value));
                    } else if (returnType.toString().contains("java.lang.Integer")) {
                        ReflectHelper.setValue(instance, mappingFields[j], new Integer(value));
                    } else if (returnType.toString().contains("java.util.Date")) {
                        value = value.trim();
                        value = value.replace("/","-");
                        String dateFormat = "";
                        if (value.length() == 10) dateFormat = TimeUtil.FORMAT_DATE_ONLY;
                        else if (value.length() == 19) dateFormat = TimeUtil.FORMAT_NORMAL;
                        else
                            throw new Exception(String.format("非法日期格式（合法为%s、%s）:%s", TimeUtil.FORMAT_DATE_ONLY, TimeUtil.FORMAT_NORMAL, value));
                        Date date = TimeUtil.parse(value, dateFormat);
                        ReflectHelper.setValue(instance, mappingFields[j], date);

                    } else if (returnType.toString().indexOf("java.sql.Timestamp") != -1) {
                        value = value.trim();
                        String dateFormat = "";
                        if (value.length() == 10) dateFormat = TimeUtil.FORMAT_DATE_ONLY;
                        else if (value.length() == 19) dateFormat = TimeUtil.FORMAT_NORMAL;
                        else
                            throw new Exception(String.format("非法日期格式（合法为%s、%s）:%s", TimeUtil.FORMAT_DATE_ONLY, TimeUtil.FORMAT_NORMAL, value));
                        Date date = TimeUtil.parse(value, dateFormat);
                        ReflectHelper.setValue(instance, mappingFields[j], new Timestamp(date.getTime()));

                    } else {
                        logger.error("unsupport Type: " + returnType.toString()
                                + "; fieldName: " + mappingFields[j]
                                + "; cell contents: " + value);
                    }
                } catch (Exception e) {
                    throw new Exception(e.getMessage() + " [attribute:" + mappingFields[j] + "]");
                }
            }
        }

        if (haveData) {
            return instance;
        }

        return null;
    }

    /**
     * 读取整个Excel数据
     *
     * @param clazz
     * @param mappingFields
     * @param sheetStartLine 起始行
     * @param is
     * @return key为sheet名
     * @throws Exception
     */
    public static Map<String, List> readAllSheet(Class clazz, String[] mappingFields, int sheetStartLine, InputStream is)
            throws Exception {
        Map<String, List> map = new HashMap<String, List>();
        XSSFWorkbook  excel = new XSSFWorkbook(is);

        int sheetNum = excel.getNumberOfSheets();
        for (int i=0;i<sheetNum;i++) {
            List list = new ArrayList();
            XSSFSheet sheet = excel.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            readSheet(list, clazz, mappingFields, sheet, sheetStartLine);
            map.put(sheetName, list);
        }

        return map;
    }

    /**
     * 读取某页数据
     *
     * @param clazz
     * @param mappingFields
     * @param sheetIndex
     * @param startLine
     * @param is
     * @return
     * @throws Exception
     */
    public static <T> List<T> readSheet(Class<T> clazz, String[] mappingFields, int sheetIndex, int startLine, InputStream is)
            throws Exception {
        List<T> list = new ArrayList<>();
        readSheet(list, clazz, mappingFields, sheetIndex, startLine, is);
        return list;
    }

    /**
     * 读取某页数据(模板带有结束标识)
     *
     * @param clazz
     * @param mappingFields
     * @param sheetIndex
     * @param startLine
     * @param is
     * @return
     * @throws Exception
     */
    public static <T> List<T> readSheet(Class<T> clazz, String[] mappingFields, int sheetIndex, int startLine, InputStream is,String endFlag)
            throws Exception {
        List<T> list = new ArrayList<>();
        readSheet(list, clazz, mappingFields, sheetIndex, startLine, is,endFlag);
        return list;
    }

    /**
     * 读取某页数据
     *
     * @param sheetIndex
     * @param startLine
     * @param is
     * @return
     * @throws Exception
     */
    public static List<List<String>> readSheet(int sheetIndex, int startLine, InputStream is)
            throws Exception {
        List<List<String>> list = new ArrayList<List<String>>();
        readSheet(list, sheetIndex, startLine, is);
        return list;
    }

    public static void readSheet(List datas, Class clazz, String[] mappingFields, int sheetIndex, int startLine, InputStream is)
            throws Exception {
    	XSSFWorkbook  excel = new XSSFWorkbook(is);
        XSSFSheet sheet = excel.getSheetAt(sheetIndex);
        if (null == sheet) {
            throw new NullPointerException("the sheetIndex of Excel is not exists: " + sheetIndex);
        }

        readSheet(datas, clazz, mappingFields, sheet, startLine);
    }

    public static void readSheet(List datas, Class clazz, String[] mappingFields, int sheetIndex, int startLine, InputStream is,String endFlag)
            throws Exception {
        XSSFWorkbook  excel = new XSSFWorkbook(is);
        XSSFSheet sheet = excel.getSheetAt(sheetIndex);
        if (null == sheet) {
            throw new NullPointerException("the sheetIndex of Excel is not exists: " + sheetIndex);
        }

        readSheet(datas, clazz, mappingFields, sheet, startLine,endFlag);
    }

    public static void readSheet(List<List<String>> datas, int sheetIndex, int startLine, InputStream is)
            throws Exception {
    	XSSFWorkbook  excel = new XSSFWorkbook(is);
    	XSSFSheet sheet = excel.getSheetAt(sheetIndex);
        if (null == sheet) {
            throw new NullPointerException("the sheetIndex of Excel is not exists: " + sheetIndex);
        }

        readSheet(datas, sheet, startLine);
    }

    private static void readSheet(List datas, Class clazz, String[] mappingFields, XSSFSheet sheet, int startLine)
            throws Exception {
        int rowCount = sheet.getLastRowNum();
        // loop the 行
        for (int i = startLine; i <= rowCount; i++) {
        	XSSFRow xssfRow = sheet.getRow(i);
            Object obj = null;
            try {
                obj = readLine(xssfRow, clazz, mappingFields);
            } catch (Exception e) {
                logger.error("Read obj faild in line " + (i + 1) + " of sheet "
                        + sheet.getSheetName());
                throw new Exception(sheet.getSheetName() + "-第" +(i + 1) + "行-" + e.getMessage());
            }
            if (obj != null) {
                datas.add(obj);
            }
        }
    }

    private static void readSheet(List datas, Class clazz, String[] mappingFields, XSSFSheet sheet, int startLine,String endFlag)
            throws Exception {
        int rowCount = sheet.getLastRowNum();
        // loop the 行
        for (int i = startLine; i <= rowCount; i++) {
            XSSFRow xssfRow = sheet.getRow(i);
            Object obj = null;
            try {
                obj = readLine(xssfRow, clazz, mappingFields,endFlag);
            } catch (Exception e) {
                logger.error("Read obj faild in line " + (i + 1) + " of sheet "
                        + sheet.getSheetName());
                throw new Exception(sheet.getSheetName() + "-第" +(i + 1) + "行-" + e.getMessage());
            }
            if (obj != null) {
                datas.add(obj);
            }
        }
    }

    private static void readSheet(List<List<String>> datas, XSSFSheet sheet, int startLine)
            throws Exception {
        int rowCount = sheet.getLastRowNum();
        for (int i = startLine; i <= rowCount; i++) {
        	XSSFRow xssfRow = sheet.getRow(i);
        	if(xssfRow!=null){
        		List<String> list = new ArrayList<String>();
                for (int c=0;c<xssfRow.getLastCellNum();c++) {
                	XSSFCell cell = xssfRow.getCell(c);
                	if(cell==null){
                		list.add(ExcelTool.EMPTY);
                    }else {
                    	list.add(ExcelTool.getXValue(cell).trim());
                    }

                }

                for (int j = list.size() - 1; j >= 0; j--) {
                    String data = list.get(j);
                    if (data.equals("")) {
                        list.remove(j);
                    } else {
                        break;
                    }
                }
                if (list.size() > 0) {
                    datas.add(list);
                }
        	}

        }
    }

    public static String[] readSheetNames(InputStream is)
            throws Exception {
        XSSFWorkbook  excel = new XSSFWorkbook(is);
        int sheetNum = excel.getNumberOfSheets();
        String[] names = new String[sheetNum];
        for(int i = 0; i < sheetNum; i++){
            names[i] = excel.getSheetName(i);
        }
        return names;
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
                || obj.getClass().equals(BigDecimal.class)) {
            return true;
        }

        throw new Exception("Unsupport type：" + obj.getClass().getName());
    }

}
