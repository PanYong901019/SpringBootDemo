package win.panyong.utils;


import com.easyond.utils.StringUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AppCache {

    //系统参数配置缓存信息
    private static Map<String, String> systemConfig = null;
    private static List<String> requestMappingList = new ArrayList<>();
    private static Map<String, Object> systemCache = new HashMap<>();

    public static Resource getResource(String pathname) {
        File file = new File(pathname);
        if (!file.exists()) {
            file = new File("config/" + pathname);
        }
        Resource resource = new FileSystemResource(file);
        if (!resource.exists()) {
            resource = new ClassPathResource("/" + pathname);
        }
        return resource;
    }

    public static Map<String, String> initSystemConfig(String... resourceNames) {
        systemConfig = new HashMap<>();
        try {
            for (String resourceName : resourceNames) {
                Properties props = PropertiesLoaderUtils.loadProperties(AppCache.getResource(resourceName));
                Enumeration<Object> enumeration = props.keys();
                while (enumeration.hasMoreElements()) {
                    String s = (String) enumeration.nextElement();
                    systemConfig.put(s, props.getProperty(s));
                }
            }
        } catch (IOException e) {
            throw new AppException("读取配置失败");
        }
        return systemConfig;
    }

    public static String getConfigValue(String key, String defaultValue) {
        String value = "";
        value = getSystemConfig().get(key);
        if (StringUtil.invalid(value)) {
            value = defaultValue;
        }
        return value;
    }

    public static Map<String, String> getSystemConfig() {
        if (systemConfig == null) {
            initSystemConfig("application.properties", "app.properties");
        }
        return systemConfig;
    }

    public void setSystemConfig(Map<String, String> systemConfig) {
        if (AppCache.systemConfig != null) {
            AppCache.systemConfig.putAll(systemConfig);
        } else {
            AppCache.systemConfig = systemConfig;
        }
    }

    public Map<String, Object> set(String key, Object value) {
        systemCache.put(key, value);
        return systemCache;
    }

    public <T> T get(String key) {
        return (T) systemCache.get(key);
    }

    public void remove(String key) {
        systemConfig.remove(key);
    }

    public List<String> getRequestMappingList() {
        return new ArrayList<>(new TreeSet<>(requestMappingList));
    }

    public void setRequestMappingList(List<String> requestMappingList) {
        AppCache.requestMappingList = requestMappingList;
    }

    public void addToRequestMappingList(String url) {
        requestMappingList.add(url);
    }
}
