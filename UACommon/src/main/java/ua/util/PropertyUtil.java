package ua.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;

/**
 * Desc:properties文件获取工具类
 */
public class PropertyUtil {
	
    private static final Log logger = LogFactory.get();
    private static Properties props;
    
    private PropertyUtil(){}
    public static Properties getProperties(String classPath) {
		if (props == null) {
			loadProps(classPath);
		}
		return props;
	}
    /*static{
        loadProps();
    }*/

    synchronized static private void loadProps(String classPath){
        logger.info("开始加载properties文件内容.......");
        props = new Properties();
        InputStream in = null;
        try {
            in = PropertyUtil.class.getClassLoader().getResourceAsStream(classPath);
            props.load(in);
        } catch (FileNotFoundException e) {
            logger.error("jdbc.properties文件未找到");
        } catch (IOException e) {
            logger.error("出现IOException");
        } finally {
            try {
                if(null != in) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("jdbc.properties文件流关闭出现异常");
            }
        }
        logger.info("加载properties文件内容完成...........");
        logger.info("properties文件内容：" + props);
    }

    public String get(String key){

        return props.getProperty(key);
    }

    public String get(String key, String defaultValue) {

        return props.getProperty(key, defaultValue);
    }
}