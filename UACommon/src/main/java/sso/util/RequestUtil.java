package sso.util;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.management.ReflectionException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

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

	public static void addCookie(HttpServletResponse response, String name, String value, int time) {
		Cookie cookie = new Cookie(name.trim(), value.trim());
		cookie.setMaxAge(time);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	public static void addCookie(HttpServletResponse response, String name, String value,String domain,int time) {
		Cookie cookie = new Cookie(name.trim(), value.trim());
		cookie.setMaxAge(time);
		cookie.setPath("/");
		cookie.setDomain(domain);
		response.addCookie(cookie);
	}

	public static String getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie c : cookies) {
				String cName = c.getName();
				if (name.equalsIgnoreCase(cName)) {
					return c.getValue();
				}
			}
		}
		return null;
	}

	public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie c : cookies) {
				String cName = c.getName();
				if (name.equalsIgnoreCase(cName)) {
					c.setValue(null);
					c.setMaxAge(0);// 立即销毁cookie
					c.setPath("/");
					response.addCookie(c);
				}
			}
		}
	}
	
	public static void printAllCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null && cookies.length > 0) {
			int i =0;
			for (Cookie c : cookies) {
				++i;
				String cName = c.getName();
				String cValue = c.getValue();
				System.out.println(i+":"+cName+"="+cValue);
			}
		}
		
	}

	public static String urlEncode(Object param) throws UnsupportedEncodingException {
		param = param == null ? "" : param;
		return URLEncoder.encode(URLEncoder.encode(JSON.toJSONString(param), "utf-8"), "utf-8");
	}

	public static String getIpAddress(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获取服务端口号
	 * 
	 * @return 端口号
	 * @throws ReflectionException
	 * @throws MBeanException
	 * @throws InstanceNotFoundException
	 * @throws AttributeNotFoundException
	 */
	public static Integer getServerPort() {
		MBeanServer mBeanServer = null;
		if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
			mBeanServer = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
		}

		if (mBeanServer == null) {
			System.out.println("调用findMBeanServer查询到的结果为null");
			return null;
		}

		Set<ObjectName> names = null;
		try {
			names = mBeanServer.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
		} catch (Exception e) {
			return null;
		}
		Iterator<ObjectName> it = names.iterator();
		ObjectName oname = null;
		try {
			while (it.hasNext()) {
				oname = (ObjectName) it.next();
				String protocol = (String) mBeanServer.getAttribute(oname, "protocol");

				if (protocol != null && ("HTTP/1.1".equals(protocol) || protocol.contains("http"))) {
					return ((Integer) mBeanServer.getAttribute(oname, "port"));

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static List<String> getEndPoints()
			throws MalformedObjectNameException, NullPointerException, UnknownHostException, AttributeNotFoundException,
			InstanceNotFoundException, MBeanException, ReflectionException {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectName> objs = mbs.queryNames(new ObjectName("*:type=Connector,*"),
				Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
		String hostname = InetAddress.getLocalHost().getHostName();
		InetAddress[] addresses = InetAddress.getAllByName(hostname);
		ArrayList<String> endPoints = new ArrayList<String>();
		for (Iterator<ObjectName> i = objs.iterator(); i.hasNext();) {
			ObjectName obj = i.next();
			String scheme = mbs.getAttribute(obj, "scheme").toString();
			String port = obj.getKeyProperty("port");
			for (InetAddress addr : addresses) {
				String host = addr.getHostAddress();
				String ep = scheme + "://" + host + ":" + port;
				endPoints.add(ep);
			}
		}
		return endPoints;
	}

}
