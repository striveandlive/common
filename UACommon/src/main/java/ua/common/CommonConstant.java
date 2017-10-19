package ua.common;

/**
 * @createDate 2017年7月7日下午3:06:14
 * @description
 */
public class CommonConstant {

	public static final String VERIFY_CODE = "imageVerifyCode";
	
	public static final String GRANT_TYPE_PASSWORD = "password";
	public static final String GRANT_TYPE_AUTHORIZATION = "authorization";
	public static final String GRANT_TYPE_IMPLICIT = "implicit";
	public static final String GRANT_TYPE_CLIENT = "client";
	
	public static final String CACHE_AUTHORITY_PREFIX = "AUTHORITY_";
	public static final String CACHE_TOKEN_PREFIX = "TOKEN_";
	public static final String CACHE_USER_PREFIX = "USER_";
	public static final String CACHE_DEPT_PREFIX = "DEPT_";
	
	public static final String CODE_TOKEN_IS_BLANK = "10001";
	public static final String CODE_TOKEN_NOT_EXIST = "10002";
	public static final String CODE_TOKEN_EXPIRES = "10003";
	
	public final static String TOKEN_PARAM_NAME = "accessToken";
	
	public static String memcachedServer;
}
