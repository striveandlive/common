package ua.util;

import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;

public class MemcacheUtil {
	private static MemCachedClient client = new MemCachedClient();
	private static String MEMCACHE_CONFIG = "/memcached.properties";
	private static URL url;
	private static MemcacheUtil cache;
	private static String server;

	private MemcacheUtil() {

	}

	private MemcacheUtil(String path) {
		url = getClass().getResource(path);
		System.out.println("url:" + path + url);
		Properties config = new Properties();
		try {
			config.load(new FileInputStream(url.getPath()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String serverGroup = server;
		if (serverGroup == null)
			serverGroup = config.getProperty("memcached.server").trim();
		String[] servers = serverGroup.split(";");

		SockIOPool pool = SockIOPool.getInstance();
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
		// client = new MemCachedClient();

	}

	public static MemcacheUtil getInstance() {
		if (cache == null) {
			cache = new MemcacheUtil(MEMCACHE_CONFIG);
		}
		return cache;
	}

	public static MemcacheUtil getInstance(String serverGroup) {
		if (cache == null) {
			cache = new MemcacheUtil(MEMCACHE_CONFIG);
			cache.server = serverGroup;
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

	public boolean replace(String key, Object value) {
		return client.replace(key, value);
	}

	public boolean replace(String key, Object value, Date expiry) {
		return client.replace(key, value, expiry);
	}

	public boolean delete(String key) {
		return client.delete(key);
	}

	/**
	 * 根据指定的关键字获取对象.
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return client.get(key);
	}

	/*
	 * public static MemCachedClient getCacheClient() { return client; }
	 */
}
