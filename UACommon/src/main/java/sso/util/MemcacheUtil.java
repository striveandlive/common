package sso.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.alibaba.fastjson.JSON;
import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemcacheUtil {
	private static MemCachedClient client = null;
	private static String MEMCACHE_CONFIG = "/memcached.properties";
	// private static URL url;
	private static MemcacheUtil cache;
	private static String server;

	private MemcacheUtil() {

	}

	private MemcacheUtil(String path) {
		// url = getClass().getResource(path);
		// System.out.println("url:" + path + url);
		Properties config = new Properties();
		try {
			/*
			 * URL fileURL=this.getClass().getResource(url.getPath());
			 * System.out.println(fileURL.getFile());
			 */
			config.load(getClass().getResourceAsStream(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String serverGroup = server;
		if (serverGroup == null)
			serverGroup = config.getProperty("memcached.server").trim();
		String[] servers = serverGroup.split(";");
		String poolName = "UA";
		SockIOPool pool = SockIOPool.getInstance(poolName);
		int init_conns = Integer.valueOf(config.getProperty("memcached.initConn").trim());
		int min_spare = Integer.valueOf(config.getProperty("memcached.minConn").trim());
		int max_spare = Integer.valueOf(config.getProperty("memcached.maxConn").trim());
		boolean failOver = Boolean.valueOf(config.getProperty("memcached.failOver").trim());
		long maintSleep = Long.valueOf(config.getProperty("memcached.maintSleep").trim());
		boolean nagle = Boolean.valueOf(config.getProperty("memcached.nagle").trim());
		int socketTO = Integer.valueOf(config.getProperty("memcached.socketTO").trim());
		boolean aliveCheck = Boolean.valueOf(config.getProperty("memcached.aliveCheck").trim());
		pool.setServers(servers);
		pool.setInitConn(init_conns);
		pool.setMinConn(min_spare);
		pool.setMaxConn(max_spare);
		pool.setMaxIdle(1000 * 60 * 60 * 6);
		pool.setMaintSleep(maintSleep);
		pool.setNagle(nagle);
		pool.setSocketTO(socketTO);
		pool.setFailover(failOver);
		pool.setAliveCheck(aliveCheck);
		pool.setSocketConnectTO(0);
		pool.initialize();
		client = new MemCachedClient(poolName);

	}

	public static MemcacheUtil getInstance() {
		if (cache == null) {
			cache = new MemcacheUtil(MEMCACHE_CONFIG);
		}
		return cache;
	}

	public static MemcacheUtil getInstance(String serverGroup) {
		MemcacheUtil.server = serverGroup;
		if (cache == null) {
			cache = new MemcacheUtil(MEMCACHE_CONFIG);
		}

		return cache;
	}

	/**
	 * 添加一个指定的值到缓存中.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, Object value) {
		return client.add(key, value);
	}

	public boolean add(String key, Object value, Date expiry) {
		return client.add(key, value, expiry);
	}

	public boolean set(String key, Object value) {
		return client.set(key, value);
	}

	public boolean set(String key, Object value, long expiryTime) {
		// long now = new Date().getTime();
		return client.set(key, value, new Date(expiryTime));
	}

	public boolean replace(String key, Object value) {
		return client.replace(key, value);
	}

	public boolean replace(String key, Object value, Date expiry) {
		return client.replace(key, value, expiry);
	}

	public boolean delete(String key) {
		return client.delete(key);
	}

	public boolean keyExists(String key) {
		return client.keyExists(key);
	}

	/**
	 * 根据指定的关键字获取对象.
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		Object result = null;
		try {
			result = client.get(key);
		} catch (Exception ex) {
			ex.printStackTrace();
			cache = new MemcacheUtil(MEMCACHE_CONFIG);
			result = client.get(key);
		}
		return result;
	}

	/**
	 * 清理缓存中的所有键/值对
	 * 
	 * @author GaoHuanjie
	 */
	public boolean flashAll() {
		try {
			return client.flushAll();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static MemCachedClient getCacheClient() {
		return client;
	}

	/**
	 * 获取服务器上面所有的key
	 */
	public static List<String> getAllKeys(MemCachedClient memCachedClient) {
		System.out.println("开始获取memcached中所有的key.......");
		List<String> list = new ArrayList<String>();
		Map<String, Map<String, String>> items = memCachedClient.statsItems();
		for (Iterator<String> itemIt = items.keySet().iterator(); itemIt.hasNext();) {
			String itemKey = itemIt.next();
			Map<String, String> maps = items.get(itemKey);
			for (Iterator<String> mapsIt = maps.keySet().iterator(); mapsIt.hasNext();) {
				String mapsKey = mapsIt.next();
				String mapsValue = maps.get(mapsKey);
				if (mapsKey.toUpperCase().endsWith(":number".toUpperCase())) { // memcached
																				// key
																				// 类型
					// item_str:integer:number_str
					String[] arr = mapsKey.split(":");
					int slabNumber = Integer.valueOf(arr[1].trim());
					int limit = Integer.valueOf(mapsValue.trim());
					Map<String, Map<String, String>> dumpMaps = memCachedClient.statsCacheDump(slabNumber, limit);
					for (Iterator<String> dumpIt = dumpMaps.keySet().iterator(); dumpIt.hasNext();) {
						String dumpKey = dumpIt.next();
						Map<String, String> allMap = dumpMaps.get(dumpKey);
						for (Iterator<String> allIt = allMap.keySet().iterator(); allIt.hasNext();) {
							String allKey = allIt.next();
							list.add(allKey.trim());
							System.out.println(allKey + ":" + JSON.toJSONString(allMap.get(allKey)));
						}
					}
				}
			}
		}
		System.out.println("获取memcached缓存中所有的key完成.......");
		return list;
	}
}
