package win.panyong.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import win.panyong.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppInitFunction {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static volatile AppInitFunction instance = null;

    private AppInitFunction() {
    }

    public static AppInitFunction getInstance() {
        if (instance == null) {
            synchronized (AppInitFunction.class) {
                if (instance == null) {
                    instance = new AppInitFunction();
                }
            }
        }
        return instance;
    }

    public void initConfig(RedisUtil redisUtil) throws AppException {
        logger.info("--------------------|SystemConfig初始化|--------------------");
        AppCache.initSystemConfig("app.properties");
        logger.info("------------------|SystemConfig初始化完成|------------------");
    }

    public void initServletContext(ApplicationContext applicationContext, AppCache appCache) throws AppException {
        logger.info("--------------------|ServletContex初始化|--------------------");
        List<String> requestMappingList = new ArrayList<>();
        AbstractHandlerMethodMapping<RequestMappingInfo> objHandlerMethodMapping = (AbstractHandlerMethodMapping<RequestMappingInfo>) applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> mapRet = objHandlerMethodMapping.getHandlerMethods();
        for (RequestMappingInfo requestMappingInfo : mapRet.keySet()) {
            Set<String> patterns = requestMappingInfo.getPatternValues();
            requestMappingList.addAll(patterns);
        }
        appCache.setRequestMappingList(requestMappingList);
        logger.info("------------------|ServletContex初始化完成|------------------");
    }

}
