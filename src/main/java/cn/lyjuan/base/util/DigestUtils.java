package cn.lyjuan.base.util;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 加密解密工具类
 * 
 * @author chad
 * 
 */
public class DigestUtils
{
	/**
	 * 算法工具 ，需要同步
	 */
	private static MessageDigest alga;

	/**
	 * MD5加密工具
	 */
	private static MessageDigest md5;

	/**
	 * alga的同步锁对象
	 */
	private static String LOCK_ALGA = "#L_lock_alga_L#";

	/**
	 * md5的同步锁对象
	 */
	private static String LOCK_MD5 = "#L_lock_md5_L#";

	static
	{
		try
		{
			alga = MessageDigest.getInstance("SHA-1");
			md5 = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
	}


	/**
	 * 加密字符
	 * 
	 * @param str
	 * @return
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static String encipher(String str, String encode) throws UnsupportedEncodingException
	{
		String enStr = "";
		String tmp = "";
		byte[] bs = null;

		// 获取加密字符
		synchronized (LOCK_ALGA)
		{
			alga.update(str.getBytes(encode));
			bs = alga.digest();
			alga.reset();
		}

		// 处理为十六进制串
		for (byte b : bs)
		{
			tmp = Integer.toHexString(b & 0xFF);
			enStr += tmp;
		}

		return enStr;
	}


	/**
	 * 使用 MD5 签名，小写 32 位
	 * 
	 * @param src
	 * @return
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.UnsupportedEncodingException
	 */
	public static final String md5LowerCase(String src, String encode)
	{
		byte[] b = null;
		synchronized (LOCK_MD5)
		{
			try
			{
				md5.update(src.getBytes(encode));
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
			b = md5.digest();
			md5.reset();
		}
		int i;
		StringBuilder buf = new StringBuilder("");
		for (int offset = 0; offset < b.length; offset++)
		{
			i = b[offset];
			if (i < 0)
				i += 256;
			if (i < 16)
				buf.append("0");
			buf.append(Integer.toHexString(i));
		}
		return buf.toString();// 32位的加密
	}

	public final static String md5UpperCase(String s, String encode) throws NoSuchAlgorithmException,
			UnsupportedEncodingException
	{
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
				'E', 'F' };
		byte[] btInput = s.getBytes(encode);
		byte[] b = null;
		
		synchronized (LOCK_MD5)
		{
			md5.update(btInput);
			b = md5.digest();
			md5.reset();
		}
		
		// 把密文转换成十六进制的字符串形式
		// byte[] data = { (byte) 0xfe, (byte) 0xff, 0x00, 0x61 };
		int j = b.length;
		char str[] = new char[j * 2];
		int k = 0;

		for (int i = 0; i < j; i++)
		{
			byte byte0 = b[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}
}
