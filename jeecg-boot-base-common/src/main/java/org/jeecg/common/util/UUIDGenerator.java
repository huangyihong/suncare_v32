package org.jeecg.common.util;


import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @Author  张代浩
 *
 */
public class UUIDGenerator {


	/**
	 * 产生一个32位的UUID
	 *
	 * @return
	 */

	public static String generate() {
		return new StringBuilder(32).append(format(getIP())).append(
				format(getJVM())).append(format(getHiTime())).append(
				format(getLoTime())).append(format(getCount())).toString();

	}

	private static final int IP;
	static {
		int ipadd;
		try {
			ipadd = toInt(InetAddress.getLocalHost().getAddress());
		} catch (Exception e) {
			ipadd = 0;
		}
		IP = ipadd;
	}

	private static short counter = (short) 0;

	private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

	private final static String format(int intval) {
		String formatted = Integer.toHexString(intval);
		StringBuilder buf = new StringBuilder("00000000");
		buf.replace(8 - formatted.length(), 8, formatted);
		return buf.toString();
	}

	private final static String format(short shortval) {
		String formatted = Integer.toHexString(shortval);
		StringBuilder buf = new StringBuilder("0000");
		buf.replace(4 - formatted.length(), 4, formatted);
		return buf.toString();
	}

	private final static int getJVM() {
		return JVM;
	}

	private final static short getCount() {
		synchronized (UUIDGenerator.class) {
			if (counter < 0) {
				counter = 0;
			}
			return counter++;
		}
	}

	/**
	 * Unique in a local network
	 */
	private final static int getIP() {
		return IP;
	}

	/**
	 * Unique down to millisecond
	 */
	private final static short getHiTime() {
		return (short) (System.currentTimeMillis() >>> 32);
	}

	private final static int getLoTime() {
		return (int) System.currentTimeMillis();
	}

	private final static int toInt(byte[] bytes) {
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result = (result << 8) - Byte.MIN_VALUE + (int) bytes[i];
		}
		return result;
	}


	/**
	 * 获取一个短号，取值来源YY-MM-DD H24:mm:ss
	 * @return
	 */
	public static String getShortCode() {
		Calendar cal = Calendar.getInstance();

		//取yyMMdd
		SimpleDateFormat yyMMdd = new SimpleDateFormat("yyMMdd");
		String date = yyMMdd.format(cal.getTime());
		//根据秒计算出3位字母
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE );
		int second = cal.get(Calendar.SECOND  );

		//计算当前时间再一天中的秒数
		long secondInDay = second + 60 * minute + 3600 * hour;

		//获取进制
		int c = ARRAY_DATE.length ;

		//结果
		String result="";

		while(secondInDay>0) {
			//取余数
			int modNum = (int)(secondInDay % c);

			//余数作为结果，从后往前拼
			result = ARRAY_DATE[modNum] + result;

			//取结果
			secondInDay =(long)(secondInDay/c);
		}


		//从表中获取随机字母
		int randomN =(int)( Math.random() *c);

		return  date+"-"+result + ARRAY_DATE[randomN];
	}

	public static void main(String[] args) {
		String id = UUIDGenerator.getShortCode();


		System.out.println(id);
	}


	private final static String[] ARRAY_DATE= new String[]{"2","3","4","5","6","7","8","9",
			"A","B","C","D","E","F","G","H","J","K","L","M","N","P","Q","R","S","T","U","V","W","X","Y","Z",
			"a","b","c","d","e","f","g","h","j","k","m","n","p","q","r","s","t","u","v","w","x","y","z"};

}
