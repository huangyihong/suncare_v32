package com.ai.common.utils;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class StringUtil extends StringUtils {

	public static boolean isNull(Object object) {

		if ((object instanceof String)) {
			return isEmpty(object.toString());
		}
		return object == null;
	}

	public static boolean isEmpty(String value) {

		return (value == null) || (value.trim().length() == 0);
	}

	public static String null2String(String string) {

		return string == null ? "" : string;
	}

	public static String null2String(Object o) {

		return o == null ? "" : o.toString();
	}

	public static String iso2Gb(String gbString) {

		if (gbString == null)
			return null;
		String outString = "";
		try {
			byte[] temp = null;
			temp = gbString.getBytes("ISO8859-1");
			outString = new String(temp, "GB2312");
		} catch (UnsupportedEncodingException e) {
		}
		return outString;
	}

	public static String iso2Utf(String isoString) {

		if (isoString == null)
			return null;
		String outString = "";
		try {
			byte[] temp = null;
			temp = isoString.getBytes("ISO-8859-1");
			outString = new String(temp, "UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		return outString;
	}

	public static String str2Gb(String inString) {

		if (inString == null)
			return null;
		String outString = "";
		try {
			byte[] temp = null;
			temp = inString.getBytes();
			outString = new String(temp, "GB2312");
		} catch (UnsupportedEncodingException e) {
		}
		return outString;
	}

	public static String fillZero(String dealCode) {

		String zero = "";
		if (dealCode.length() < 3) {
			for (int i = 0; i < 3 - dealCode.length(); i++) {
				zero = zero + "0";
			}
		}
		return zero + dealCode;
	}

	public static String convertAmount(String amount) {

		String str = String.valueOf(Double.parseDouble(amount));
		int pos = str.indexOf(".");
		int len = str.length();
		if (len - pos < 3) {
			return str.substring(0, pos + 2) + "0";
		}
		return str.substring(0, pos + 3);
	}

	public static String to10(String opStr) {

		String zm = "#123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int lenOfOp = opStr.length();
		long result = 0L;
		for (int i = 0; i < lenOfOp; i++) {
			String indexOfOp = opStr.substring(i, i + 1);
			int js = zm.indexOf(indexOfOp);
			result = result * 36L + js;
		}
		String jg = String.valueOf(result);
		int bc = 12 - jg.length();
		String jgq = "";
		for (int j = 0; j < bc; j++) {
			jgq = jgq + "0";
		}
		return jgq + jg;
	}

	public static String to36(String originalStr) {

		long oVal = Long.parseLong(originalStr);
		String result = "";
		String zm = "#123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 1; i < 9; i++) {
			long shang = oVal / 36L;
			int yushu = (int) (oVal % 36L);
			result = zm.substring(yushu, yushu + 1) + result;
			oVal = shang;
		}
		return result;
	}

	public static String encDealId(String dealid) {

		if (dealid.length() != 23)
			return "notval";
		String ny = dealid.substring(5, 7);
		String sq = dealid.substring(11, 21);
		return to36(ny + sq);
	}

	public static String decDealId(String opStr) {

		return to10(opStr);
	}

	public static String[] numToWords(String money) {

		int j = money.length();
		String[] str = new String[j];
		for (int i = 0; i < j; i++) {
			switch (money.charAt(i)) {
			case '0':
				str[i] = "零";
				break;
			case '1':
				str[i] = "壹";
				break;
			case '2':
				str[i] = "贰";
				break;
			case '3':
				str[i] = "叁";
				break;
			case '4':
				str[i] = "肆";
				break;
			case '5':
				str[i] = "伍";
				break;
			case '6':
				str[i] = "陆";
				break;
			case '7':
				str[i] = "柒";
				break;
			case '8':
				str[i] = "捌";
				break;
			case '9':
				str[i] = "玖";
				break;
			case '.':
				str[i] = "点";
			}
		}
		return str;
	}

	public static String money2BigFormat(String money) {

		String[] bigNumber = numToWords(money);
		int len = bigNumber.length;
		if (len > 11)
			return "数额过高";
		StringBuffer sb = new StringBuffer();
		if (len >= 7) {
			if (len == 11) {
				sb.append(bigNumber[0] + "仟");
				sb.append(bigNumber[1] + "佰" + bigNumber[2] + "拾" + bigNumber[3] + "万");
			}
			if (len == 10) {
				sb.append(bigNumber[0] + "佰" + bigNumber[1] + "拾" + bigNumber[2] + "万");
			}
			if (len == 9) {
				sb.append(bigNumber[0] + "拾" + bigNumber[1] + "万");
			}
			if (len == 8) {
				sb.append(bigNumber[0] + "万");
			}
			sb.append(bigNumber[(len - 7)] + "仟" + bigNumber[(len - 6)] + "佰" + bigNumber[(len - 5)] + "拾");
		}
		if (len == 6) {
			sb.append(bigNumber[0] + "佰" + bigNumber[1] + "拾");
		}
		if (len == 5) {
			sb.append(bigNumber[0] + "拾");
		}
		sb.append(bigNumber[(len - 4)] + "元" + bigNumber[(len - 2)] + "角" + bigNumber[(len - 1)] + "分整");
		return sb.toString();
	}

	public static String formatCurrecy(String currency) {

		if ((null == currency) || ("".equals(currency)) || ("null".equals(currency))) {
			return "";
		}
		NumberFormat usFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
		try {
			return usFormat.format(Double.parseDouble(currency));
		} catch (Exception e) {
		}
		return "";
	}

	public static String formatCurrecy(String currency, String currencyCode) {

		try {
			if ((null == currency) || ("".equals(currency)) || ("null".equals(currency))) {
				return "";
			}
			if (currencyCode.equalsIgnoreCase("1")) {
				NumberFormat usFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
				return usFormat.format(Double.parseDouble(currency));
			}
			return currency + "点";
		} catch (Exception e) {
		}
		return "";
	}

	public static String[] split(String str) {

		return split(str, null, -1);
	}

	public static String[] split(String text, String separator) {

		return split(text, separator, -1);
	}

	public static String[] split(String str, String separator, int max) {

		StringTokenizer tok = null;
		if (separator == null) {
			tok = new StringTokenizer(str);
		} else {
			tok = new StringTokenizer(str, separator);
		}
		int listSize = tok.countTokens();
		if ((max > 0) && (listSize > max)) {
			listSize = max;
		}
		String[] list = new String[listSize];
		int i = 0;
		int lastTokenBegin = 0;
		int lastTokenEnd = 0;
		while (tok.hasMoreTokens()) {
			if ((max > 0) && (i == listSize - 1)) {
				String endToken = tok.nextToken();
				lastTokenBegin = str.indexOf(endToken, lastTokenEnd);
				list[i] = str.substring(lastTokenBegin);
				break;
			}
			list[i] = tok.nextToken();
			lastTokenBegin = str.indexOf(list[i], lastTokenEnd);
			lastTokenEnd = lastTokenBegin + list[i].length();
			i++;
		}
		return list;
	}

	public static String replace(String text, String repl, String with) {

		return replace(text, repl, with, -1);
	}

	public static String[] getArr(String str) {

		String[] strArray = { "" };
		if (str.indexOf(",") < 0) {
			strArray[0] = str;
			return strArray;
		}
		StringTokenizer st = new StringTokenizer(str, ",");
		strArray = new String[st.countTokens()];
		int strLeng = st.countTokens();
		for (int i = 0; i < strLeng; i++) {
			strArray[i] = st.nextToken();
		}
		return strArray;
	}

	public static String getIds(String[] ids) {

		StringBuffer strb = new StringBuffer();
		for (String str : ids) {
			strb.append("'");
			strb.append(str);
			strb.append("',");
		}
		return StringUtils.substringBeforeLast(strb.toString(), ",");
	}

	public static String StringFilter(String str) {

		if (str != null) {
			String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（） |　——+|{}【】'；：”“’。，、？\n\t\r]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(str);
			return m.replaceAll("").trim();
		}
		return "";
	}
}
