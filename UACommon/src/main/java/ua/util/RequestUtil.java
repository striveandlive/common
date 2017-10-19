package ua.util;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ua.common.CommonConstant;

public class RequestUtil {
	public static Map<String, String> handleParams(Map<String, String[]> params) {
		if (params == null)
			return null;
		Map<String, String> map = new HashMap();
		for (String key : params.keySet()) {
			map.put(key, params.get(key)[0]);
		}
		return map;
	}

	public static void addCookie(HttpServletResponse response, String name, String value,int time) {
		Cookie cookie = new Cookie(name.trim(), value.trim());
		cookie.setMaxAge(time);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	

	public static String getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();  
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {  
            	String cName = c.getName(); 
            	if (name.equalsIgnoreCase(cName)){
            		return c.getValue();
            	}
            }  
        } 
        return null;
	}
	
	public static void deleteCookie(HttpServletRequest request,HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();  
        if (cookies != null && cookies.length > 0) {
            for (Cookie c : cookies) {  
            	String cName = c.getName(); 
            	if (name.equalsIgnoreCase(cName)){
            		c.setValue(null);  
                    c.setMaxAge(0);// 立即销毁cookie  
                    c.setPath("/");  
                    response.addCookie(c);
            	}
            }  
        } 
	}

}
