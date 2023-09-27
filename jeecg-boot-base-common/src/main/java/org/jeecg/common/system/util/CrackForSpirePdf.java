package org.jeecg.common.system.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.util.Hex;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfLiteral;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.ContentOperator;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * <a href=
 * "http://stackoverflow.com/questions/35526822/removing-watermark-from-pdf-itextsharp">
 * Removing Watermark from PDF iTextSharp </a>
 * <p>
 * This class presents a simple content stream editing framework. As is it
 * creates an equivalent copy of the original page content stream. To actually
 * edit, simply overwrite the method
 * {@link #write(PdfContentStreamProcessor, PdfLiteral, List)} to not (as in
 * this class) write the given operations as they are but change them in some
 * fancy way.
 * </p>
 * 
 * @author mkl
 */
public class CrackForSpirePdf extends PdfContentStreamProcessor {
	
	public static void main(String[] args) {
		String pdfSource = "C:\\Users\\Administrator\\Desktop\\11.pdf";
		String destFile = "C:\\Users\\Administrator\\Desktop\\22.pdf";
		crack(pdfSource,destFile);
	}

	public static void crack(String pdfSource ,String destFile) {
		try {
			PdfReader pdfReader = new PdfReader(pdfSource);
			FileOutputStream os = new FileOutputStream(destFile);
			PdfStamper stamper = new PdfStamper(pdfReader, os);
			
			
			CrackForSpirePdf editor = new CrackForSpirePdf() {

				@Override
				protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands)
						throws IOException {
					String operatorString = operator.toString();

					// getTextMatrix()

					// getGraphicsState().
					// Tj 操作通过当前的字体和其他文字相关的图形状态参数来取走一串操作和绘制相应的字形
					// Tr操作设置的文本渲染模式
					// 一个文本对象开始于BT，结束于ET
					final List<String> TEXT_SHOWING_OPERATORS = Arrays.asList("Tj", "'", "\\", "TJ");
					// System.out.println(operatorString);

					// if(TEXT_SHOWING_OPERATORS.contains(operatorString)){
					if ("Tj".equalsIgnoreCase(operatorString)) {

						for (int i = 0; i < operands.size(); i++) {
							if (!operands.get(i).isString())
								continue;

							PdfString text = (PdfString) operands.get(i);

							byte[] b = text.getBytes();
							String str = Hex.getString(b);
							if (str.equals(
									"0001000200030004000500030006000700080009000A000B0003000C000900070009000D000A000E000A000F00100011000A00120008001300050014001100090006000A001500030016000A0013000C00110003000600110012000A0015000700060010000A000A001700180007000C00110019001A001B0017000A001C0008000C000A001D000300020003")
								|| str.equals("001700210008000400090008002200230024000A000B002500080007000A0023000A000F000B0026000B0027001A000D000B00280024002900090003000D000A0022000B002A0008000E000B00290007000D00080022000D0028000B002A00230022001A000B000B001C002B00230007000D0006002C0016001C000B002D00240007000B002E000800210008")	
									) {
								return;
							}
						}

//						int r = gs().getFillColor().getRGB();
//						if (r==-65536) {
//							
//							 for(int  i = 0; i < operands.size(); i++)  {
//				                    if(!operands.get(i).isString())
//				                        continue;
//
//				                    PdfString text =(PdfString) operands.get(i);
//				                    
//				                    byte[] b=text.getBytes();
//				                    String str = Hex.getString(b);
//				                    System.out.println("a" + str + "b");
//				                }
//							 
//							return;
//						}
//						PdfDictionary dic = gs().getFont().getFontDictionary();		
//						 
//						if(gs().getFont().getPostscriptFontName().endsWith("BoldMT")){//BoldMT字体的名称
//							return;
//						}
//
//						for (int i = 0; i < operands.size(); i++) {
//							if (!operands.get(i).isString())
//								continue;
//
//							PdfString text = (PdfString) operands.get(i);
//
//							// if(text.toUnicodeString().indexOf("Evaluation")>=0) {
//							// System.out.println("tt="+text.toUnicodeString());
//							// }
//						}
					}

					// Evaluation Warning : The document was created with Spire.XLS for Java
					super.write(processor, operator, operands);
				}
			};
			
			for (int i = 1; i <= pdfReader.getNumberOfPages() ; i++) {
				editor.editPage(stamper, i);				
			} 
			 
			
			stamper.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method edits the immediate contents of a page, i.e. its content stream.
	 * It explicitly does not descent into form xobjects, patterns, or annotations.
	 */
	public void editPage(PdfStamper pdfStamper, int pageNum) throws IOException {
		PdfReader pdfReader = pdfStamper.getReader();
		PdfDictionary page = pdfReader.getPageN(pageNum);
		byte[] pageContentInput = ContentByteUtils.getContentBytesForPage(pdfReader, pageNum);
		page.remove(PdfName.CONTENTS);
		editContent(pageContentInput, page.getAsDict(PdfName.RESOURCES), pdfStamper.getUnderContent(pageNum));
	}

	/**
	 * This method processes the content bytes and outputs to the given canvas. It
	 * explicitly does not descent into form xobjects, patterns, or annotations.
	 */
	public void editContent(byte[] contentBytes, PdfDictionary resources, PdfContentByte canvas) {
		this.canvas = canvas;
		processContent(contentBytes, resources);
		this.canvas = null;
	}

	/**
	 * <p>
	 * This method writes content stream operations to the target canvas. The
	 * default implementation writes them as they come, so it essentially generates
	 * identical copies of the original instructions the
	 * {@link ContentOperatorWrapper} instances forward to it.
	 * </p>
	 * <p>
	 * Override this method to achieve some fancy editing effect.
	 * </p>
	 */
	protected void write(PdfContentStreamProcessor processor, PdfLiteral operator, List<PdfObject> operands)
			throws IOException {
		int index = 0;

		for (PdfObject object : operands) {
			object.toPdf(canvas.getPdfWriter(), canvas.getInternalBuffer());
			canvas.getInternalBuffer().append(operands.size() > ++index ? (byte) ' ' : (byte) '\n');
		}
	}

	//
	// constructor giving the parent a dummy listener to talk to
	//
	public CrackForSpirePdf() {
		super(new DummyRenderListener());
	}

	//
	// Overrides of PdfContentStreamProcessor methods
	//
	@Override
	public ContentOperator registerContentOperator(String operatorString, ContentOperator operator) {
		ContentOperatorWrapper wrapper = new ContentOperatorWrapper();
		wrapper.setOriginalOperator(operator);
		ContentOperator formerOperator = super.registerContentOperator(operatorString, wrapper);
		return formerOperator instanceof ContentOperatorWrapper
				? ((ContentOperatorWrapper) formerOperator).getOriginalOperator()
				: formerOperator;
	}

	@Override
	public void processContent(byte[] contentBytes, PdfDictionary resources) {
		this.resources = resources;
		super.processContent(contentBytes, resources);
		this.resources = null;
	}

	//
	// members holding the output canvas and the resources
	//
	protected PdfContentByte canvas = null;
	protected PdfDictionary resources = null;

	//
	// A content operator class to wrap all content operators to forward the
	// invocation to the editor
	//
	class ContentOperatorWrapper implements ContentOperator {
		public ContentOperator getOriginalOperator() {
			return originalOperator;
		}

		public void setOriginalOperator(ContentOperator originalOperator) {
			this.originalOperator = originalOperator;
		}

		@Override
		public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList<PdfObject> operands)
				throws Exception {
			if (originalOperator != null && !"Do".equals(operator.toString())) {
				originalOperator.invoke(processor, operator, operands);
			}
			write(processor, operator, operands);
		}

		private ContentOperator originalOperator = null;
	}

	//
	// A dummy render listener to give to the underlying content stream processor to
	// feed events to
	//
	static class DummyRenderListener implements RenderListener {
		@Override
		public void beginTextBlock() {
		}

		@Override
		public void renderText(TextRenderInfo renderInfo) {
		}

		@Override
		public void endTextBlock() {
		}

		@Override
		public void renderImage(ImageRenderInfo renderInfo) {
		}
	}
}