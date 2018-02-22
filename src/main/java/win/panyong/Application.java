package win.panyong;

import com.easyond.utils.ObjectUtil;
import com.easyond.utils.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import win.panyong.utils.RedisCache;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@ServletComponentScan
@SpringBootApplication
@EnableCaching
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "jedisPool")
    public JedisPool jedisPool(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port, @Value("${spring.redis.password}") String password, @Value("${spring.redis.database}") int database, @Value("${spring.redis.timeout}") int timeout, @Value("${spring.redis.pool.max-wait}") int maxWait, @Value("${spring.redis.pool.max-idle}") int maxIdle, @Value("${spring.redis.pool.min-idle}") int minIdle) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        return new JedisPool(jedisPoolConfig, host, port, timeout, StringUtil.invalid(password) ? null : password, database);
    }

    @Bean(name = "redisCache")
    public RedisCache redisCache() {
        return new RedisCache();
    }
}

@WebListener
class ApplicationListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("=================================================================ServletContex初始化");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("===================================================================ServletContex销毁");
    }
}

@WebFilter(filterName = "sessionFilter", urlPatterns = {"/*"})
class SessionFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Map<String, Object> errorResult = new HashMap<>();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> requestParameter = new HashMap<>();
        for (String s : parameterMap.keySet()) {
            requestParameter.put(s, parameterMap.get(s)[0]);
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("【yyyy-MM-dd HH:mm:ss】");
        String date = sdf.format(now);
        String[] whitelist = {"/apiError", "/webError"};
        String[] api = {"/api/test", "/api/download"};
        String[] web = {};
        if (!request.getRequestURI().substring(request.getContextPath().length()).equals("/favicon.ico")) {
            if (StringUtil.isHave(whitelist, request.getRequestURI().substring(request.getContextPath().length()))) {
                System.out.println("request：=====whitelist=====|" + request.getMethod() + "|" + request.getRequestURI().substring(request.getContextPath().length()) + "|===|" + ObjectUtil.mapToJsonString(requestParameter));
                filterChain.doFilter(request, response);
            } else if (StringUtil.isHave(api, request.getRequestURI().substring(request.getContextPath().length()))) {
                System.out.println("request：========api========|" + request.getMethod() + "|" + request.getRequestURI().substring(request.getContextPath().length()) + "|===|" + ObjectUtil.mapToJsonString(requestParameter));
                filterChain.doFilter(request, response);
            } else if (StringUtil.isHave(web, request.getRequestURI().substring(request.getContextPath().length()))) {
                System.out.println("request：========web========|" + request.getMethod() + "|" + request.getRequestURI().substring(request.getContextPath().length()) + "|===|" + ObjectUtil.mapToJsonString(requestParameter));
                filterChain.doFilter(request, response);
            } else {
                System.out.println("request：======没有权限======|" + request.getMethod() + "|" + request.getRequestURI().substring(request.getContextPath().length()) + "|===|" + ObjectUtil.mapToJsonString(requestParameter));
            }
        }
    }

    @Override
    public void destroy() {

    }
}

