package sso.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.alibaba.fastjson.JSON;


public class AESUtil {
	public static String encode(String encryptSecret, Map<String, Object> paraMap) throws UnsupportedEncodingException {
		
		return parseByte2HexStr(encryptAES(JSON.toJSONString(paraMap), encryptSecret));
	}
	
	public static String encode(String encryptSecret,String content) throws UnsupportedEncodingException {
		
		return parseByte2HexStr(encryptAES(content, encryptSecret));
	}

	public static Map decode(String encryptSecret, String token) throws Exception {

		return JSON.parseObject(decodeForString(encryptSecret, token), Map.class);

	}
	
	public static String decodeForString(String encryptSecret, String token) throws Exception {

		return new String(decryptAES(parseHexStr2Byte(token),encryptSecret));

	}
	
	/** 
	 * 校验和 
	 *  
	 * @param msg 需要计算校验和的byte数组 
	 * @param length 校验和位数 
	 * @return 计算出的校验和数组 
	*/  
	public static byte[] SumCheck(byte[] msg, int length) {  
	    long mSum = 0;  
	    byte[] mByte = new byte[length];  
	          
	    /** 逐Byte添加位数和 */  
	    for (byte byteMsg : msg) {  
	        long mNum = ((long)byteMsg >= 0) ? (long)byteMsg : ((long)byteMsg + 256);  
	        mSum += mNum;  
	    } /** end of for (byte byteMsg : msg) */  
	          
	    /** 位数和转化为Byte数组 */  
	    for (int liv_Count = 0; liv_Count < length; liv_Count++) {  
	        mByte[length - liv_Count - 1] = (byte)(mSum >> (liv_Count * 8) & 0xff);  
	    } /** end of for (int liv_Count = 0; liv_Count < length; liv_Count++) */  
	          
	    return mByte;  
	}  

	/**
	 * 加密
	 * 
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密码
	 * @return
	 */
	public static byte[] encryptAES(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解密
	 * 
	 * @param content
	 *            待解密内容
	 * @param password
	 *            解密密钥
	 * @return
	 */
	public static byte[] decryptAES(byte[] content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(content);
			return result; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** 
     * 将二进制转换成16进制 
     * @method parseByte2HexStr 
     * @param buf 
     * @return 
     * @throws  
     * @since v1.0 
     */  
    public static String parseByte2HexStr(byte buf[]){  
        StringBuffer sb = new StringBuffer();  
        for(int i = 0; i < buf.length; i++){  
            String hex = Integer.toHexString(buf[i] & 0xFF);  
            if (hex.length() == 1) {  
                hex = '0' + hex;  
            }  
            sb.append(hex.toUpperCase());  
        }  
        return sb.toString();  
    }  
      
    /** 
     * 将16进制转换为二进制 
     * @method parseHexStr2Byte 
     * @param hexStr 
     * @return 
     * @throws  
     * @since v1.0 
     */  
    public static byte[] parseHexStr2Byte(String hexStr){  
        if(hexStr.length() < 1)  
            return null;  
        byte[] result = new byte[hexStr.length()/2];  
        for (int i = 0;i< hexStr.length()/2; i++) {  
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
            result[i] = (byte) (high * 16 + low);  
        }  
        return result;  
    }  

	public static void main(String[] args) throws Exception {
		// byte[] key =
		// SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
		String key = "5TS1TT54RN6IJOVK";
		Map paraMap = new HashMap();

		paraMap.put("id", "ksjdhfknsf");
		String result = encode(key, paraMap);
		// String result =
		// "mUpXYaSLK+6+TTys4kAftrKdSU15S/AJ1wonr7MuaVFol1OV6Ol7HkKcaO26zvo7J2w0NwaV6dLC";//
		System.out.println(result);
		Map map = decode(key, result);
		System.out.println(JSON.toJSONString(map));
	}
}
