package win.panyong.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AppCache {

    private static List<String> requestMappingList = new ArrayList<>();
    private static Map<String, String> systemConfig = null;

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
                    systemConfig.put(s, StringUtil.InputStreamToString(new ByteArrayInputStream(props.getProperty(s).getBytes(StandardCharsets.ISO_8859_1))));
                }
            }
        } catch (IOException e) {
            throw new AppException("读取配置失败");
        }
        return systemConfig;
    }

    public static Map<String, String> getSystemConfig() {
        if (systemConfig == null) {
            initSystemConfig("app.properties");
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


    public List<String> getRequestMappingList() {
        return new ArrayList<>(new TreeSet<>(requestMappingList));
    }

    public void setRequestMappingList(List<String> requestMappingList) {
        AppCache.requestMappingList = requestMappingList;
    }


}
