package ua.util;

import java.util.List;
import java.util.Map;

import ua.common.CommonConstant;

public class TokenUtil {
	/**
	 * 通过令牌取权限
	 * @param accessToken
	 * @return
	 */
	public static Object getAuth(String accessToken){
		return MemcacheUtil.getInstance(CommonConstant.memcachedServer).get(CommonConstant.CACHE_AUTHORITY_PREFIX+accessToken);
	}
	
	/**
	 * 通过令牌判断权限
	 * @param accessToken
	 * @param uri
	 * @return
	 */
	public static boolean validAuth(String accessToken,String uri){
		Object obj = MemcacheUtil.getInstance(CommonConstant.memcachedServer).get(CommonConstant.CACHE_AUTHORITY_PREFIX+accessToken);
		if (obj!=null) {
			Map map = (Map)obj;
			Object data = map.get("data");
			if (data!=null){
				Map dataMap = (Map)data;
				Object authsObj = dataMap.get("authList");
				if(authsObj!=null){
					List<Map> authList = (List<Map>)authsObj;
					for (Map auth:authList){
						if (uri.startsWith(auth.get("uri").toString())){
							return true;
						}
					}
				}
				
			}
		}
		return false;
	}
	
	/**
	 * 通过令牌取用户信息
	 * @param accessToken
	 * @return
	 */
	public static Object getUserInfo(String accessToken){
		return MemcacheUtil.getInstance(CommonConstant.memcachedServer).get(CommonConstant.CACHE_USER_PREFIX+accessToken);
	}
}
