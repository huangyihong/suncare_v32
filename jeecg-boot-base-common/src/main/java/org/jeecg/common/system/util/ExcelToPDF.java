package org.jeecg.common.system.util;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;

import com.spire.xls.FileFormat;
import com.spire.xls.Workbook;

public class ExcelToPDF {
	public static void excelConverterToPdf(String sourceExcelFilePath, String destFilePath) throws Exception {
		
		// 调用spire.xls生成pdf
		Workbook wb = new Workbook();
		wb.loadFromFile(sourceExcelFilePath);

		String tmpDest = destFilePath + ".tmp";
		// 调用方法保存为PDF格式
		wb.saveToFile(tmpDest, FileFormat.PDF);
		wb.dispose();

		// 删除最后一页
		removeLastPage(tmpDest);
		
		// 破解pdf的水印
		CrackForSpirePdf.crack(tmpDest, destFilePath);
		
		File f = new File(tmpDest);
		f.delete();

	}

	/**
	 * 去除最后一页
	 * @param pdfPath
	 * @throws Exception
	 */
	public static void removeLastPage(String pdfPath) throws Exception {
		PDDocument document = null;
		try {
			document = PDDocument.load(new File(pdfPath));
			if (document.isEncrypted()) {
				System.err.println("Error: Encrypted documents are not supported for this example.");
				System.exit(1);
			}

			// 去除最后一页
			document.removePage(document.getNumberOfPages() - 1);

			document.save(pdfPath);
		} finally {
			if (document != null) {
				document.close();
			}
		}
	}
}
