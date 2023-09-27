package org.jeecg.common.system.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;

import cn.hutool.system.OsInfo;

public class WordToPDF {
	private final static String LICENSE_STR="<?xml version=\"1.0\" encoding=\"UTF-8\" ?><License><Data> <Products><Product>Aspose.Total for Java</Product><Product>Aspose.Words for Java</Product></Products><EditionType>Enterprise</EditionType><SubscriptionExpiry>20991231</SubscriptionExpiry><LicenseExpiry>20991231</LicenseExpiry><SerialNumber>8bfe198c-7f0c-4ef8-8ff0-acc3237bf0d7</SerialNumber> </Data><Signature>sNLLKGMUdF0r8O1kKilWAGdgfs2BvJb/2Xp8p5iuDVfZXmhppo+d0Ran1P9TKdjV4ABwAgKXxJ3jcQTqE/2IRfqwnPf8itN8aFZlV3TJPYeD3yWE7IT55Gz6EijUpC7aKeoohTb4w2fpox58wWoF3SNp6sK6jDfiAUGEHYJ9pjU=</Signature></License>";

	public static void wordConverterToPdf(String sourceFilePath, String destFilePath) throws Exception {
		InputStream source = new FileInputStream(sourceFilePath);
		OutputStream target = new FileOutputStream(destFilePath);

		wordConverterToPdf(source, target, null);
	}

	public static boolean wordConverterToPdf(InputStream inputStream, OutputStream outStream, Map<String, String> params)
			throws Exception {
		if (!getLicense()) { // 验证License 若不验证则转化出的pdf文档会有水印产生
			return false;
		}
		
		String osName = System.getProperty("os.name");
		
		if(osName !=null && osName.toLowerCase().indexOf("windows")<0) {
			FontSettings.setFontsFolders(
					new String[] {"/usr/share/fonts/dejavu",
							"/usr/share/fonts",
							"/usr/share/xpdf/xpdf-chinese-simplified",
							"/usr/share/xpdf/xpdf-chinese-simplified/CMap"}, 
					true);
			System.out.println("set fonts folder");
		}
				

		try {
			long old = System.currentTimeMillis();

			Document doc = new Document(inputStream); // Address是将要被转化的word文档
			
			doc.save(outStream, SaveFormat.PDF);// 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF,
			// EPUB, XPS, SWF 相互转换
			long now = System.currentTimeMillis();
			System.out.println("pdf转换成功，共耗时：" + ((now - old) / 1000.0) + "秒"); // 转化用时
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (outStream != null) {
				try {
					outStream.flush();
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private static boolean getLicense() {
		boolean result = false;
		InputStream is = null;
		try {

			is = new ByteArrayInputStream(LICENSE_STR.getBytes());

			License aposeLic = new License();
			aposeLic.setLicense(is);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	/**
	 * 将word文档， 转换成pdf, 中间替换掉变量
	 *
	 * @param source 源为word文档， 必须为docx文档
	 * @param target 目标输出
	 * @param params 需要替换的变量
	 * @throws Exception
	 */
//    public static void wordConverterToPdf(InputStream source,
//                                          OutputStream target, Map<String, String> params) throws Exception {
//        wordConverterToPdf(source, target, null, params);
//    }
//
//    public static void wordConverterToPdf(String sourceFilePath,
//    		String destFilePath) throws Exception {
//    	InputStream source = new FileInputStream(sourceFilePath);
//    	OutputStream target = new FileOutputStream(destFilePath);
//        
//    	wordConverterToPdf(source, target, null, null);
//    }
//    
//    /**
//     * 将word文档， 转换成pdf, 中间替换掉变量
//     *
//     * @param source  源为word文档， 必须为docx文档
//     * @param target  目标输出
//     * @param params  需要替换的变量
//     * @param options PdfOptions.create().fontEncoding( "windows-1250" ) 或者其他
//     * @throws Exception
//     */
//    public static void wordConverterToPdf(InputStream source, OutputStream target,
//                                          PdfOptions options,
//                                          Map<String, String> params) throws Exception {
//        XWPFDocument doc = new XWPFDocument(source);
//        paragraphReplace(doc.getParagraphs(), params);
//        for (XWPFTable table : doc.getTables()) {
//            for (XWPFTableRow row : table.getRows()) {
//                for (XWPFTableCell cell : row.getTableCells()) {
//                    paragraphReplace(cell.getParagraphs(), params);
//                }
//            }
//        }
//        PdfConverter.getInstance().convert(doc, target, options);
//    }
//
//    /**
//     * 替换段落中内容
//     */
//    private static void paragraphReplace(List<XWPFParagraph> paragraphs, Map<String, String> params) {
//        if (MapUtils.isNotEmpty(params)) {
//            for (XWPFParagraph p : paragraphs) {
//                for (XWPFRun r : p.getRuns()) {
//                    String content = r.getText(r.getTextPosition());
//                    if (StringUtils.isNotEmpty(content) && params.containsKey(content)) {
//                        r.setText(params.get(content), 0);
//                    }
//                }
//            }
//        }
//    }

    public static void main(String[] arg){
    	try {
    		String docPath = "C:\\Users\\Administrator\\Desktop\\JXFZ-9-ZW-3712药品合规新维护方式业务需求v1.0.4_20210723.docx";
            String pdfPath = "C:\\Users\\Administrator\\Desktop\\2.pdf";
            
            if(arg != null && arg.length>0) {
            	docPath= arg[0];
            }
            
            if(arg != null && arg.length>1) {
            	pdfPath= arg[1];
            }
            
            wordConverterToPdf(docPath,pdfPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }

}
