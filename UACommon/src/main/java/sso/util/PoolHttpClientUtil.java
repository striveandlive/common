package sso.util;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;

public class PoolHttpClientUtil {
	private static Log log = LogFactory.get();
	// 日志
	// private static final SimpleLogger LOGGER =
	// SimpleLogger.getLogger(PoolHttpsClientService.class);

	private static final String CHAR_SET = "UTF-8";

	// 代理IP
	private static String proxyIp;

	// 代理端口
	private static Integer proxyPort;

	/**
	 * 最大连接数400
	 */
	private static int MAX_CONNECTION_NUM = 400;

	/**
	 * 单路由最大连接数80
	 */
	private static int MAX_PER_ROUTE = 80;

	/**
	 * 向服务端请求超时时间设置(单位:毫秒)
	 */
	private static int SERVER_REQUEST_TIME_OUT = 2000;

	/**
	 * 服务端响应超时时间设置(单位:毫秒)
	 */
	private static int SERVER_RESPONSE_TIME_OUT = 2000;
	
	private static PoolHttpClientUtil instance;

	/**
	 * 构造函数
	 */
	private PoolHttpClientUtil() {
	}
	
	public static PoolHttpClientUtil getInstance() {
		if (instance == null) {
			instance = new PoolHttpClientUtil();
		}
		return instance;
	}
	
	public static PoolHttpClientUtil getInstance(String proxyIp,Integer proxyPort) {
		if (instance == null) {
			instance = new PoolHttpClientUtil();
			instance.proxyIp = proxyIp;
			instance.proxyPort = proxyPort;
		}
		return instance;
	}

	private static Object LOCAL_LOCK = new Object();

	/**
	 * 连接池管理对象
	 */
	PoolingHttpClientConnectionManager cm = null;

	/**
	 * 
	 * 功能描述: <br>
	 * 初始化连接池管理对象
	 * 
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	private PoolingHttpClientConnectionManager getPoolManager() {
		final String methodName = "getPoolManager";
		if (null == cm) {
			synchronized (LOCAL_LOCK) {
				if (null == cm) {
					SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
					try {
						sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
						SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
								sslContextBuilder.build());
						Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
								.<ConnectionSocketFactory>create().register("https", socketFactory)
								.register("http", new PlainConnectionSocketFactory()).build();

						cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
						cm.setMaxTotal(MAX_CONNECTION_NUM);
						cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);
					} catch (Exception e) {
						log.error(methodName, "init PoolingHttpClientConnectionManager Error" + e);
					}

				}
			}
		}
		return cm;
	}

	/**
	 * 创建线程安全的HttpClient
	 * 
	 * @param config
	 *            客户端超时设置
	 * 
	 * @return
	 */
	public CloseableHttpClient getHttpsClient(RequestConfig config) {
		CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config)
				.setConnectionManager(this.getPoolManager()).build();
		return httpClient;
	}

	public String poolHttpGet(String url) {

		HttpGet httpget = new HttpGet(url);
		String result = null;
		CloseableHttpResponse response = null;
		try {
			// connectTimeout设置服务器请求超时时间
			// socketTimeout设置服务器响应超时时间
			org.apache.http.client.config.RequestConfig.Builder b = RequestConfig.custom()
					.setSocketTimeout(SERVER_REQUEST_TIME_OUT).setConnectTimeout(SERVER_RESPONSE_TIME_OUT);
			if (proxyPort != null && proxyIp != null) {
				// 设置代理
				HttpHost proxy = new HttpHost(proxyIp, proxyPort);
				b.setProxy(proxy);
			}
			RequestConfig requestConfig = b.build();
			httpget.setConfig(requestConfig);
			response = getHttpsClient(requestConfig).execute(httpget);
			int status = response.getStatusLine().getStatusCode();
			if (status == 200) {
				result = EntityUtils.toString(response.getEntity(), CHAR_SET);
			}
			EntityUtils.consume(response.getEntity());
			response.close();
			return result;
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpget.releaseConnection();
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// 超时或者网络不通时返回值
		return null;
	}
	
	

	/**
	 * Https post请求
	 * 
	 * @param url
	 *            请求地址
	 * @param json
	 *            请求参数(如果为null,则表示不请求参数) return 返回结果
	 */
	public String poolHttpsPost(String url, JSONObject json) {
		final String methodName = "poolHttpsPost";
		CloseableHttpResponse response = null;

		HttpPost httpPost = null;
		try {
			// connectTimeout设置服务器请求超时时间
			// socketTimeout设置服务器响应超时时间
			org.apache.http.client.config.RequestConfig.Builder b = RequestConfig.custom()
					.setSocketTimeout(SERVER_REQUEST_TIME_OUT).setConnectTimeout(SERVER_RESPONSE_TIME_OUT);
			if (proxyPort != null && proxyIp != null) {
				// 设置代理
				HttpHost proxy = new HttpHost(proxyIp, proxyPort);
				b.setProxy(proxy);
			}

			RequestConfig requestConfig = b.build();
			httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);

			if (json != null) {
				StringEntity se = new StringEntity(json.toString(), CHAR_SET);
				se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;"));
				httpPost.setEntity(se);
			}

			log.info(methodName, "start post to weixin");
			response = getHttpsClient(requestConfig).execute(httpPost);
			log.info(methodName, "end post to weixin");
			int status = response.getStatusLine().getStatusCode();
			log.info(methodName, "return status:" + status);

			String result = null;
			if (status == 200) {
				result = EntityUtils.toString(response.getEntity(), CHAR_SET);
			}
			EntityUtils.consume(response.getEntity());
			response.close();
			return result;
		} catch (Exception e) {
			if (e instanceof SocketTimeoutException) {
				// 服务器请求超时
				log.error(methodName, "server request time out");
			} else if (e instanceof ConnectTimeoutException) {
				// 服务器响应超时(已经请求了)
				log.error(methodName, "server response time out");
			}
			log.error(methodName, e.getMessage());
		} finally {

			httpPost.releaseConnection();
			if (response != null) {
				try {
					EntityUtils.consume(response.getEntity());
					response.close();
				} catch (IOException e) {
					log.error(methodName, e.getMessage());
				}
			}
		}
		// 超时或者网络不通时返回值
		return null;
	}
}