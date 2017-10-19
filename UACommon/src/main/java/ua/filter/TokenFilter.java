package ua.filter;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;

import ua.common.WritableHttpServletRequest;
import ua.common.CommonConstant;
import ua.common.CommonResponse;
import ua.model.Token;
import ua.util.AESUtil;
import ua.util.MemcacheUtil;
import ua.util.PoolHttpClientUtil;
import ua.util.RequestUtil;
import ua.util.TokenUtil;

/**
 * accessToken过滤器 
 * 1. 不传accessToken参数 直接返回：10001 无令牌 
 * 2. 找不到accessToken 直接返回：10002
 * 令牌错误 
 * 3. 找到accessToken 
 * 3.1 超过过期时间 直接返回：10003 令牌过期 
 * 3.2 没超过过期时间
 * a)判断过期时间与当前时间相隔小于15分钟，此时令牌过期时间增加二小时 
 * b)通过
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
	
	protected static Log log = LogFactory.get();

	private final static String contentType = "application/json;charset=utf-8";

	private static String excludedUris;
	private static String[] excludedUriArray;

	
	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
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

		log.info("令牌过滤器……");
		try {
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
			WritableHttpServletRequest wr = new WritableHttpServletRequest(httpRequest);
			if (accessToken!=null) {
				//强制设置到请求头
				wr.addHeader(CommonConstant.TOKEN_PARAM_NAME, accessToken);
			}
			log.info("accessToken:{}", accessToken);
			if (StringUtils.isBlank(accessToken)) {
				response.setContentType(contentType);
				response.getWriter().write(JSON.toJSONString(CommonResponse.failTokenIsBlank()));
				return;
			} else {
				Map<String, Object> tokenMap = AESUtil.decode(encryptSecret, accessToken);
				if (tokenMap != null && tokenMap.get("expiresTime") != null) {
					// 考虑用缓存
					Object obj = MemcacheUtil.getInstance(CommonConstant.memcachedServer).get(CommonConstant.CACHE_TOKEN_PREFIX + accessToken);
					if (obj != null) {
						Token result = (Token) obj;
						Date now = new Date();
						Date et = result.getExpiresTime();
						/*log.debug("缓存中过期时间:" + et.getTime() + "-" + et);

						log.debug("服务器当前时间:" + now.getTime() + "-" + now);*/
						if (et.getTime() > now.getTime()) {
							if (TokenUtil.validAuth(accessToken, httpRequest.getRequestURI())){
								chain.doFilter(wr, response);
							}
							long diffTime = et.getTime() - now.getTime();
							// 当前距离过期十五分钟内
							if (diffTime <= (limitMinute * 60 * 1000)) {
								// 过期时间增加
								String delayExpireUrl = baseUrl + "/delayExpire?accessToken=" + accessToken;
								PoolHttpClientUtil.getInstance().poolHttpGet(delayExpireUrl);
							}
							return;
						} else {
							response.setContentType(contentType);
							response.getWriter().write(JSON.toJSONString(CommonResponse.failTokenExpires()));
							return;
						}
					}
				}
				response.setContentType(contentType);
				response.getWriter().write(JSON.toJSONString(CommonResponse.failTokenNotExists()));
				return;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setContentType(contentType);
			response.getWriter().write(JSON.toJSONString(CommonResponse.failTokenNotExists()));
			return;
		}
	}

	
	public void init(FilterConfig filterConfig) throws ServletException {

		encryptSecret = filterConfig.getInitParameter("ua.encryptSecret");
		String temp = filterConfig.getInitParameter("ua.limitMinute");
		if (StringUtils.isNotBlank(temp)){
			limitMinute = Long.valueOf(temp);
		}else{
			limitMinute = 15;
		}
		baseUrl = filterConfig.getInitParameter("ua.baseUrl");
		CommonConstant.memcachedServer= filterConfig.getInitParameter("memcached.server");
		excludedUris = filterConfig.getInitParameter("EXCLUDED_URIS");
		if (StringUtils.isNotBlank(excludedUris)) { // 例外页面不为空
			excludedUriArray = excludedUris.split(";");
		}
	}

}
