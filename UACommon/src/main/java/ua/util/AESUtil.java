package ua.util;

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

import org.apache.commons.codec.binary.Base64;

import com.alibaba.fastjson.JSON;


public class AESUtil {
	public static String encode(String encryptSecret, Map<String, Object> paraMap) throws UnsupportedEncodingException {
		/*AES aes = SecureUtil.aes(encryptSecret.getBytes("UTF-8"));

		return Base64.encodeBase64String(aes.encrypt(JSON.toJSONString(paraMap), "UTF-8"));*/

		return Base64.encodeBase64String(encryptAES(JSON.toJSONString(paraMap), encryptSecret));
	}

	public static Map decode(String encryptSecret, String token) throws Exception {
//		AES aes = SecureUtil.aes(encryptSecret.getBytes("UTF-8"));
//		String jsonString = null;

		String jsonString = new String(decryptAES(Base64.decodeBase64(token),encryptSecret));

		return JSON.parseObject(jsonString, Map.class);

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
