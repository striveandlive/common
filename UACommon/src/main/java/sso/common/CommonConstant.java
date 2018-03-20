package sso.common;

/**
 * @createDate 2017年7月7日下午3:06:14
 * @description
 */
public class CommonConstant {
	
	public static final String CACHE_AUTHORITY_PREFIX = "AUTHORITY_";
	public static final String CACHE_TOKEN_PREFIX = "TOKEN_";
	public static final String CACHE_VERIFYCODE_PREFIX = "VERIFYCODE_";
	public static final String CACHE_USER_PREFIX = "USER_";
	public static final String CACHE_DEPT_PREFIX = "DEPT_";

	public static final String CODE_SUCCESS = "1";
	public static final String CODE_FAIL = "0";
	
	public static final String CODE_TOKEN_IS_BLANK = "10001";
	public static final String CODE_TOKEN_NOT_EXIST = "10002";
	public static final String CODE_TOKEN_EXPIRES = "10003";
	
	public static final String CODE_NO_ACCESS = "401";
	
	public final static String TOKEN_PARAM_NAME = "accessToken";
	
	public final static String AUTH_PARAM_NAME = "AUTH_LIST";
	
	public final static String AUTH_FIELD_URL = "uri";
	
	public final static String TOKEN_PARAM_EXPIRE = "expiresTime";
	
	public final static String URI_OFFLINE = "offline";
	public final static String URI_ONLINE = "online";
	public final static String URI_SSO_CONFIG = "_ssoConfig";
	public final static String URI_DELAY_EXPIRE = "delayExpire";
	public final static String URI_VALID_URI = "validUri";
	
	/*public final static String CONTAINER_TOMCAT = "Tomcat";
	public final static String CONTAINER_SPRINGBOOT = "SpringBoot";*/
	
//	public final static String CACHE_SSO_DATA = "SSO_DATA";
	
	/**
	 * memcachedServer=ip:port
	 */
	public static String memcachedServer;
}
