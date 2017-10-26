package sso.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.alibaba.fastjson.JSON;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;

import sso.common.CommonConstant;
import sso.common.CommonResponse;
import sso.common.WritableHttpServletRequest;
import sso.model.Token;
import sso.util.AESUtil;
import sso.util.MemcacheUtil;
import sso.util.PoolHttpClientUtil;
import sso.util.RequestUtil;
import sso.util.TokenUtil;

/**
 * accessToken过滤器 1. 不传accessToken参数 直接返回：10001 无令牌 2. 找不到accessToken 直接返回：10002
 * 令牌错误 3. 找到accessToken 3.1 超过过期时间 直接返回：10003 令牌过期 3.2 没超过过期时间
 * a)判断过期时间与当前时间相隔小于15分钟，此时令牌过期时间增加二小时 b)通过
 * 
 * @author ruby
 *
 */
public class TokenFilter implements Filter {
	private static String encryptSecret;
	/**
	 * 未活动分钟数
	 */
	private static long limitMinute;
	private static String baseUrl;
	private static Boolean isNotice;
//	private static String runContainer;
	private static String containerPort;

	protected static Log log = LogFactory.get();

	private final static String contentType = "application/json;charset=utf-8";

	private static String excludedUris;
	private static String[] excludedUriArray;

	public void destroy() {
		// 告知主控下线

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		boolean isExcludedUri = false;
		if (excludedUriArray != null)
			for (String uri : excludedUriArray) {// 遍历例外url数组
				// 判断当前URL是否与例外页面相同
				if (httpRequest.getServletPath()
						.startsWith(uri)/* .equals(uri) */) { // 从第2个字符开始取（把前面的/去掉）
					isExcludedUri = true;
					break;
				}
			}
		if (isExcludedUri) {// 在过滤url之外
			chain.doFilter(request, response);
			return;
		}

		try {
			
			/*if (httpRequest.getServletPath().equalsIgnoreCase("_getAppInfo")) {
				ua.model.AppInfo app = new ua.model.AppInfo();
				app.setContextPath(httpRequest.getContextPath());
				app.setPort(httpRequest.getLocalPort());
				app.setProtocol(httpRequest.getProtocol());
				app.setServerIp(httpRequest.getLocalAddr());
				app.setServerName(httpRequest.getServerName());
				response.setContentType(contentType);
				response.getWriter().write(JSON.toJSONString(app));
				return;
			}*/

			// 从请求头取
			String accessToken = httpRequest.getHeader(CommonConstant.TOKEN_PARAM_NAME);

			if (accessToken == null) {
				// 从参数取
				accessToken = httpRequest.getParameter(CommonConstant.TOKEN_PARAM_NAME);
			}
			if (accessToken == null) {
				// 从cookie取
				accessToken = RequestUtil.getCookie((HttpServletRequest) request, CommonConstant.TOKEN_PARAM_NAME);
			}
			//for cross domain cookie config
			if (StringUtils.isNotBlank(accessToken)&&httpRequest.getServletPath().equalsIgnoreCase("/_ssoConfig")) {
				/*String domain = httpRequest.getServerName();
				if (httpRequest.getServerPort()!=80) domain+=":"+httpRequest.getServerPort();*/
				RequestUtil.addCookie(httpResponse, CommonConstant.TOKEN_PARAM_NAME, accessToken/*,domain*/, 24*60*60);
				httpResponse.setContentType(contentType);
				//允许跨域
				httpResponse.setHeader("Access-Control-Allow-Origin", "*");
				httpResponse.getWriter().write(JSON.toJSONString(CommonResponse.success(null)));
				return;
			}
			
			log.info("令牌过滤器-accessToken:{}", accessToken);
			if (StringUtils.isBlank(accessToken)) {
				/*
				 * response.setContentType(contentType);
				 * response.getWriter().write(JSON.toJSONString(CommonResponse.
				 * failTokenIsBlank()));
				 */
				httpResponse.setStatus(301);
				httpResponse.setHeader("Location",
						baseUrl + "login?return=" + RequestUtil.urlEncode(CommonResponse.failTokenIsBlank()));
				httpResponse.setHeader("Connection", "close");
				return;
			} else {
				WritableHttpServletRequest wr = new WritableHttpServletRequest(httpRequest);
				// 强制设置到请求头
				wr.addHeader(CommonConstant.TOKEN_PARAM_NAME, accessToken);
				Map<String, Object> tokenMap = AESUtil.decode(encryptSecret, accessToken);
				if (tokenMap != null && tokenMap.get(CommonConstant.TOKEN_PARAM_EXPIRE) != null) {
					// 考虑用缓存
					Object obj = MemcacheUtil.getInstance(CommonConstant.memcachedServer)
							.get(CommonConstant.CACHE_TOKEN_PREFIX + accessToken);
					if (obj != null) {
						Token result = (Token) obj;
						Date now = new Date();
						Date et = result.getExpiresTime();

						if (et.getTime() > now.getTime()) {
							long diffTime = et.getTime() - now.getTime();
							// 当前距离过期十五分钟内
							if (diffTime <= (limitMinute * 60 * 1000)) {
								// 过期时间增加
								String delayExpireUrl = baseUrl + "/delayExpire?" + CommonConstant.TOKEN_PARAM_NAME
										+ "=" + accessToken;
								PoolHttpClientUtil.getInstance().poolHttpGet(delayExpireUrl);
							}
						} else {
							// response.setContentType(contentType);
							// response.getWriter().write(JSON.toJSONString(CommonResponse.failTokenExpires(baseUrl
							// + "/login")));
							httpResponse.setStatus(301);
							httpResponse.setHeader("Location", baseUrl + "login?return="
									+ RequestUtil.urlEncode(CommonResponse.failTokenExpires()));
							httpResponse.setHeader("Connection", "close");
							return;
						}
						String uri = httpRequest.getRequestURI();
						uri = uri.replaceFirst(httpRequest.getContextPath(), "");
						if (TokenUtil.validAuth(accessToken, uri)) {
							chain.doFilter(wr, response);
							return;
						} else {
							response.setContentType(contentType);
							response.getWriter().write(JSON.toJSONString(CommonResponse.failNoAccess()));
							return;
						}

					}
				}
				// response.setContentType(contentType);
				// response.getWriter().write(JSON.toJSONString(CommonResponse.failTokenNotExists()));
				httpResponse.setStatus(301);
				httpResponse.setHeader("Location",
						baseUrl + "login?return=" + RequestUtil.urlEncode(CommonResponse.failTokenNotExists()));
				httpResponse.setHeader("Connection", "close");
				return;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			/*
			 * response.setContentType(contentType);
			 * response.getWriter().write(JSON.toJSONString(CommonResponse.fail(
			 * ex.getMessage())));
			 */
			httpResponse.setStatus(301);
			httpResponse.setHeader("Location",
					baseUrl + "login?return=" + RequestUtil.urlEncode(CommonResponse.fail(ex.getMessage())));
			httpResponse.setHeader("Connection", "close");
			return;
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		ServletContext context = filterConfig.getServletContext();// 这里获取applicationContext
		encryptSecret = filterConfig.getInitParameter("ua.encryptSecret");
		String temp = filterConfig.getInitParameter("ua.limitMinute");
		if (StringUtils.isNotBlank(temp)) {
			limitMinute = Long.valueOf(temp);
		} else {
			limitMinute = 15;
		}
		baseUrl = filterConfig.getInitParameter("ua.baseUrl");
		CommonConstant.memcachedServer = filterConfig.getInitParameter("memcached.server");
		excludedUris = filterConfig.getInitParameter("EXCLUDED_URIS");
		if (StringUtils.isNotBlank(excludedUris)) { // 例外页面不为空
			excludedUriArray = excludedUris.split(";");
		}
		
		
		String notice = filterConfig.getInitParameter("ua.isNotice");
		isNotice = true;
		if (StringUtils.isNotBlank(notice)) {
			isNotice = Boolean.valueOf(notice);
		}
		if (isNotice) {
			containerPort = filterConfig.getInitParameter("server.port");
			if (StringUtils.isBlank(containerPort)) {
				log.error("server.port为空");
				return;
			}
			/*runContainer = filterConfig.getInitParameter("ua.runContainer");
			Integer port=null;
			if (StringUtils.isNotBlank(runContainer)) {
				if (runContainer.equalsIgnoreCase(CommonConstant.CONTAINER_TOMCAT)){
					port = RequestUtil.getServerPort();
				}else if (runContainer.equalsIgnoreCase(CommonConstant.CONTAINER_SPRINGBOOT)){
					port = SpringBootUtil.getPort();
				}
			}else{
				port = SpringBootUtil.getPort();
			}
			if (port==null) {
				log.info("取不到运行容器端口：{}",runContainer);
			}*/
			// 告知主控上线
			String host = "";
	        try {
	            host = InetAddress.getLocalHost().getHostAddress();
	        } catch (UnknownHostException e) {
	            log.error("get server host Exception e:", e);
	        }

	        CloseableHttpClient httpCilent = HttpClients.createDefault();
			try {
				String url = baseUrl + "/token/online?ip="+host+"&port=" + containerPort + "&contextPath="
						+ context.getContextPath();
				HttpGet httpget = new HttpGet(url);

				httpCilent.execute(httpget);
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					httpCilent.close();// 释放资源
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
