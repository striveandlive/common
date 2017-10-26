package sso.util;

import java.util.List;
import java.util.Map;

import sso.common.CommonConstant;

public class TokenUtil {

	/**
	 * 通过令牌判断权限
	 * 
	 * @param accessToken
	 * @param uri
	 * @return
	 */
	public static boolean validAuth(String accessToken, String uri) {
		Object obj = MemcacheUtil.getInstance(CommonConstant.memcachedServer)
				.get(CommonConstant.CACHE_AUTHORITY_PREFIX + accessToken);

		if (obj != null) {
				Map map = (Map) obj;
				Object authData = map.get(CommonConstant.AUTH_PARAM_NAME);
				if (authData != null) {
					List<Map> authList = (List<Map>) authData;
					for (Map auth : authList) {
						Object temp = auth.get(CommonConstant.AUTH_FIELD_URL);
						if (temp != null && uri.startsWith(temp.toString())) {
							return true;
						}
					}
				}
		}
		return false;
	}

	
}
