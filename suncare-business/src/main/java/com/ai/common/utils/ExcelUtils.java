package com.ai.common.utils;

import com.alibaba.fastjson.JSONObject;
import jxl.*;
import jxl.write.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.*;
import java.lang.Boolean;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author vincent
 */
public class ExcelUtils {
    private static Log logger = LogFactory.getLog(ExcelUtils.class);
    private static final int DEF_START_ROW = 0; // 默认起始列

    public static final FileFilter EXCEL_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.getName().endsWith(".xls")) {
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
    private static Object readLine(Cell[] cells, Class clazz, String[] mappingFields)
            throws Exception {
        boolean haveData = false;
        Object instance = clazz.newInstance();
        int length = cells.length > mappingFields.length ? mappingFields.length
                : cells.length;

        for (int j = 0; j < length; j++) {
            if (StringUtils.isBlank(mappingFields[j])) {
                continue;
            }
            Cell cell = cells[j];
            if (cell.getContents() != null && !"".equals(cell.getContents())) {
                try {
                    haveData = true;
                    String value = cell.getContents().trim();
                    if(cell.getType()==CellType.NUMBER||cell.getType()==CellType.NUMBER_FORMULA){
                        if(value.indexOf(".")!=-1){
                            NumberCell nc=(NumberCell)cell;
                            value=""+nc.getValue();
                        }
                    }

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
                    } else if (returnType.toString().contains("java.lang.Long")) {
                        ReflectHelper.setValue(instance, mappingFields[j], Long.valueOf(value));
                    } else if (returnType.toString().contains("java.math.BigDecimal")) {
                        ReflectHelper.setValue(instance, mappingFields[j], new BigDecimal(value));
                    } else if (returnType.toString().contains("java.lang.Integer")) {
                        ReflectHelper.setValue(instance, mappingFields[j], new Integer(value));
                    } else if (returnType.toString().contains("java.util.Date")) {
                        value = value.trim();
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

        Workbook excel = Workbook.getWorkbook(is);
        String[] sheetNames = excel.getSheetNames();
        for (String sheetName : sheetNames) {
            List list = new ArrayList();
            Sheet sheet = excel.getSheet(sheetName);
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
        Workbook excel = Workbook.getWorkbook(is);
        Sheet sheet = excel.getSheet(sheetIndex);
        if (null == sheet) {
            throw new NullPointerException("the sheetIndex of Excel is not exists: " + sheetIndex);
        }

        readSheet(datas, clazz, mappingFields, sheet, startLine);
    }

    public static void readSheet(List<List<String>> datas, int sheetIndex, int startLine, InputStream is)
            throws Exception {
        Workbook excel = Workbook.getWorkbook(is);
        Sheet sheet = excel.getSheet(sheetIndex);
        if (null == sheet) {
            throw new NullPointerException("the sheetIndex of Excel is not exists: " + sheetIndex);
        }

        readSheet(datas, sheet, startLine);
    }

    private static void readSheet(List datas, Class clazz, String[] mappingFields, Sheet sheet, int startLine)
            throws Exception {
        int rowCount = sheet.getRows();
        // loop the 行
        for (int i = startLine; i < rowCount; i++) {
            Cell[] cells = sheet.getRow(i);
            Object obj = null;
            try {
                obj = readLine(cells, clazz, mappingFields);
            } catch (Exception e) {
                logger.error("Read obj faild in line " + (i + 1) + " of sheet "
                        + sheet.getName());
                throw e;
            }
            if (obj != null) {
                datas.add(obj);
            }
        }
    }

    private static void readSheet(List<List<String>> datas, Sheet sheet, int startLine)
            throws Exception {
        int rowCount = sheet.getRows();
        for (int i = startLine; i < rowCount; i++) {
            Cell[] cells = sheet.getRow(i);
            List<String> list = new ArrayList<String>();
            for (Cell cell : cells) {
                list.add(cell.getContents().trim());
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

    /**
     * 一行对应一个object的输出
     *
     * @param obj
     * @param mappingFields
     * @param startRow      起始列
     * @param currLine      当前行
     * @return
     * @throws Exception
     */
    private static Label[] objectToLabels(Object obj, String[] mappingFields, int startRow, int currLine)
            throws Exception {
        if (mappingFields == null || mappingFields.length == 0) {
            throw new Exception("the mappingFields is not allowed empty");
        }

        Label[] labels = new Label[mappingFields.length];

        for (int i = 0; i < mappingFields.length; i++) {
            String fieldName = mappingFields[i];

            Object value = null;
            try {
                value = ReflectHelper.getValue(obj, fieldName);
            } catch (Exception e) {
                logger.error("", e);
            }
            value = value == null ? "" : value;

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

    private static Label[] mapToLabels(Map map, String[] mappingFields, int startRow, int currLine)
            throws Exception {
        if (mappingFields == null || mappingFields.length == 0) {
            throw new Exception("the mappingFields is not allowed empty");
        }

        Label[] labels = new Label[mappingFields.length];

        for (int i = 0; i < mappingFields.length; i++) {
            String fieldName = mappingFields[i];

            Object value = map.get(fieldName);
            value = value == null ? "" : value;

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
     * 多页输出
     *
     * @param map
     * @param titles
     * @param mappingFields
     * @param file
     */
    public static void writeSheets(Map<String, List<Object>> map, String[] titles, String[] mappingFields, OutputStream os)
            throws Exception {
        Set<String> names = map.keySet();
        int nameIndex = 0;
        if (names != null && names.size() > 0) {
            WritableWorkbook wwb = Workbook.createWorkbook(os);
            for (String name : names) {
                WritableSheet sheet = wwb.createSheet(name, nameIndex++);

                int startLie = DEF_START_ROW;
                int startHang = 0;
                // out put title
                if (titles != null && titles.length > 0) {
                    for (String title : titles) {
                        sheet.addCell(new Label(startLie++, startHang, title,
                                new WritableCellFormat(NumberFormats.TEXT)));
                    }
                    startLie = DEF_START_ROW;
                    startHang++;
                }
                // out put body
                List objs = map.get(name);
                for (Object bean : objs) {
                    Label[] labels = objectToLabels(bean, mappingFields, startLie, startHang++);
                    for (Label label : labels) {
                        sheet.addCell(label);
                    }
                }
            }

            wwb.write();
            wwb.close();
        }
    }

    /**
     * list数据输出
     *
     * @param objs
     * @param titles
     * @param mappingFields
     * @param file
     * @param sheetName
     */
    public static void writeOneSheet(List objs, String[] titles, String[] mappingFields, String sheetName, OutputStream os)
            throws Exception {
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet(sheetName, 0);

        int startLie = DEF_START_ROW;
        int startHang = 0;
        // out put title
        if (titles != null && titles.length > 0) {
            for (String title : titles) {
                sheet.addCell(new Label(startLie++, startHang, title,
                        new WritableCellFormat(NumberFormats.TEXT)));
            }
            startLie = DEF_START_ROW;
            startHang++;
        }
        // out put body
        if (objs != null) {
            for (Object bean : objs) {
                Label[] labels = objectToLabels(bean, mappingFields, startLie,
                        startHang++);
                for (Label label : labels) {
                    sheet.addCell(label);
                }
            }
        }

        wwb.write();
        wwb.close();
    }

    /**
     * list数据输出
     *
     * @param dataList
     * @param sheetName
     * @param titles
     * @param os
     * @throws Exception
     */
    public static void writeOneSheet(List<List<String>> dataList, String sheetName, String[] titles, OutputStream os)
            throws Exception {
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet(sheetName, 0);

        int startLie = DEF_START_ROW;
        int startHang = 0;
        if (titles != null && titles.length > 0) {
            for (String title : titles) {
                sheet.addCell(new Label(startLie++, startHang, title,
                        new WritableCellFormat(NumberFormats.TEXT)));
            }
            startLie = DEF_START_ROW;
            startHang++;
        }
        if (dataList != null) {
            for (List<String> datas : dataList) {
                startLie = DEF_START_ROW;
                for (String data : datas) {
                    sheet.addCell(new Label(startLie++, startHang, data,
                            new WritableCellFormat(NumberFormats.TEXT)));
                }
                startHang++;
            }
        }
        wwb.write();
        wwb.close();
    }

    /**
     * Map的key作为标题，value作为值的输出，
     *
     * @param mapList
     * @param sheetName
     * @param os
     * @throws Exception
     */
    public static void writeOneSheet(List<Map<String, Object>> mapList, String sheetName, OutputStream os)
            throws Exception {
        if (mapList != null && mapList.size() > 0) {
            Object[] objs = mapList.get(0).keySet().toArray();
            String[] titles = new String[objs.length];
            int i = 0;
            for (Object obj : objs) {
                titles[i++] = obj.toString();
            }

            List<List<String>> rowList = new ArrayList<List<String>>();

            for (Map<String, Object> map : mapList) {
                List<String> row = new ArrayList<String>();
                for (String title : titles) {
                    row.add(map.get(title).toString());
                }
                rowList.add(row);
            }
            writeOneSheet(rowList, sheetName, titles, os);
        }
    }

    public static void writeOneSheet(List<Object[]> objsList, String[] titles, String sheetName, OutputStream os)
            throws Exception {
        if (objsList != null && objsList.size() > 0) {
            List<List<String>> rowList = new ArrayList<List<String>>();

            for (Object[] objs : objsList) {
                List<String> row = new ArrayList<String>();
                for (Object obj : objs) {
                    if (obj == null) {
                        row.add("");
                    } else {
                        if (obj instanceof Date) {
                            row.add(TimeUtil.format((Date) obj, TimeUtil.FORMAT_NORMAL));
                        } else {
                            row.add(obj.toString());
                        }
                    }
                }
                rowList.add(row);
            }

            writeOneSheet(rowList, sheetName, titles, os);
        }
    }

    public static void writeOneSheet1(List<JSONObject> objsList, String[] titles, String[] mappingFields, String sheetName, OutputStream os, String cellTitle)
            throws Exception {
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet(sheetName, 0);

        //设置标题
        WritableCellFormat wc = new WritableCellFormat();
        wc.setAlignment(Alignment.CENTRE); // 设置居中
        sheet.mergeCells(0, 0, titles.length - 1, 0);
        sheet.addCell(new Label(0, 0, cellTitle, wc));

        int startLie = DEF_START_ROW;
        int startHang = 1;
        // out put title
        if (titles != null && titles.length > 0) {
            for (String title : titles) {
                sheet.addCell(new Label(startLie++, startHang, title,
                        new WritableCellFormat(NumberFormats.TEXT)));
            }
            startLie = DEF_START_ROW;
            startHang++;
        }
        // out put body
        if (objsList != null) {
            for (JSONObject jsonObj : objsList) {
                startLie = DEF_START_ROW;
                for (int i = 0; i < mappingFields.length; i++) {
                    String data = jsonObj.getString(mappingFields[i]);
                    sheet.addCell(new Label(startLie++, startHang, data,
                            new WritableCellFormat(NumberFormats.TEXT)));
                }
                startHang++;
            }
        }

        wwb.write();
        wwb.close();
    }

    public static int writeSheets(List objs, String[] titles, String[] mappingFields, WritableSheet sheet, int startHang)
            throws Exception {

        int startLie = DEF_START_ROW;
        // out put title
        if (titles != null && titles.length > 0) {
            for (String title : titles) {
                sheet.addCell(new Label(startLie++, startHang, title,
                        new WritableCellFormat(NumberFormats.TEXT)));
            }
            startLie = DEF_START_ROW;
            startHang++;
        }
        // out put body
        if (objs != null) {
            for (Object bean : objs) {
                Label[] labels = objectToLabels(bean, mappingFields, startLie,
                        startHang++);
                for (Label label : labels) {
                    sheet.addCell(label);
                }
            }
        }
        return startHang;
    }


    public static void main(String[] args) throws Exception {
        int sheetIndex = 0;
        int startLine = 1;
        //读string测试
//		System.out.println("<<<<<读string测试>>>>>");
//		String file = "E:/excel读测试.xls";
//		FileInputStream is = new FileInputStream(file);
//		List<List<String>> datas  = readSheet( sheetIndex, startLine, is);
//		for (List<String> list : datas) {
//			StringBuffer sb = new StringBuffer();
//			for (String str : list) {
//				sb.append(str).append(",");
//			}
//			System.out.println(sb);
//		}
//		//读对象测试
//		System.out.println("<<<<<读对象测试>>>>>");
//		is = new FileInputStream(file);
//		List<UserTest> objList = readSheet(UserTest.class, new String[]{"name","age","createTime"}, sheetIndex, startLine, is);
//		for (UserTest user : objList) {
//			if(user.getCreateTime()!=null){
//				System.out.println(user.getName()+","+user.getAge()+","+TimeUtil.format(user.getCreateTime(), TimeUtil.FORMAT_NORMAL));
//			}else{
//				System.out.println(user.getName()+","+user.getAge());
//			}
//		}
        //写string测试
//		System.out.println("<<<<<写string测试>>>>>");
//		String file = "E:/excel写string测试.xls";
//		List<List<String>> listDatas = new ArrayList<List<String>>();
//		List<String> list1 = new ArrayList<>();
//		list1.add("测试列1");
//		list1.add("测试列2");
//		list1.add("测试列3");
//		listDatas.add(list1);
//		List<String> list2 = new ArrayList<>();
//		list2.add("测试列1");
//		list2.add("测试列2");
//		listDatas.add(list2);
//		FileOutputStream os = new FileOutputStream(file);
//		writeOneSheet(listDatas, "页1", new String[]{"标题1","标题2"}, os);
        //写对象测试
        System.out.println("<<<<<写对象测试>>>>>");
        String file = "E:/excel写对象测试.xls";
        List<UserTest> userList = new ArrayList<>();
        userList.add(new UserTest("张三", 67, new Date()));
        userList.add(new UserTest("李四", 98, null));
        userList.add(new UserTest("李四12", 98, null));
        userList.add(new UserTest("李四123", 98, null));
        FileOutputStream os = new FileOutputStream(file);
        WritableWorkbook wwb = Workbook.createWorkbook(os);
        WritableSheet sheet = wwb.createSheet("用户", 0);
        int startHang = 0;
        WritableCellFormat wc = new WritableCellFormat();
        wc.setAlignment(Alignment.CENTRE); // 设置居中
        sheet.mergeCells(0, 0, 2, 0);
        sheet.addCell(new Label(0, startHang, "hyh1", wc));
        startHang++;
        startHang = writeSheets(userList, new String[]{"姓名", "年龄", "创建时间"}, new String[]{"name", "age", "createTime"}, sheet, startHang);
        sheet.mergeCells(0, startHang, 3, 0);
        sheet.addCell(new Label(0, startHang, "hyh2", wc));
        startHang++;
        startHang = writeSheets(userList, new String[]{"姓名111", "年龄222", "创建时间333"}, new String[]{"name", "age", "createTime"}, sheet, startHang);
        wwb.write();
        wwb.close();
    }

    public static void writeOneSheetSXSSFWorkbook(List objs, String[] titles, String[] mappingFields,String sheetName,OutputStream os)
            throws Exception {
    	SXSSFWorkbook workbook = new SXSSFWorkbook();
    	// 生成一个表格
    	SXSSFSheet sheet = (SXSSFSheet) workbook.createSheet(sheetName);

        int startLie = DEF_START_ROW;
        int startHang = 0;
        // out put title
        if (titles != null && titles.length > 0) {
        	Row row = sheet.createRow(startHang);
            for (String title : titles) {
            	row.createCell(startLie++).setCellValue(title);
            }
            startLie = DEF_START_ROW;
            startHang++;
        }
        // out put body
        if (objs != null && objs.size() > 0) {
            if(objs.get(0) instanceof Map){
                for (Object bean : objs) {
                    Row row = sheet.createRow(startHang);
                    Label[] labels = mapToLabels((Map) bean, mappingFields, 0,
                            0);
                    for (Label label : labels) {
                        row.createCell(startLie++).setCellValue(label.getContents());
                    }
                    startLie = DEF_START_ROW;
                    startHang++;
                }
            } else {
                for (Object bean : objs) {
                    Row row = sheet.createRow(startHang);
                    Label[] labels = objectToLabels(bean, mappingFields, 0,
                            0);
                    for (Label label : labels) {
                        row.createCell(startLie++).setCellValue(label.getContents());
                    }
                    startLie = DEF_START_ROW;
                    startHang++;
                }
            }

        }
        workbook.write(os);
        workbook.dispose();// 释放workbook所占用的所有windows资源
    }
}

class UserTest {
    private String name;
    private Integer age;
    private Date createTime;

    public UserTest() {
    }

    public UserTest(String name, Integer age, Date createTime) {
        super();
        this.name = name;
        this.age = age;
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
