package win.panyong;

import com.easyond.utils.DateUtil;
import com.easyond.utils.ObjectUtil;
import com.easyond.utils.StringUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import win.panyong.utils.AppCache;
import win.panyong.utils.AppInitFunction;
import win.panyong.utils.PromisionInterceptor;
import win.panyong.utils.RedisCache;

import javax.jms.ConnectionFactory;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@ServletComponentScan
@SpringBootApplication
@EnableCaching
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(this.getClass());
    }

    @Bean(name = "jedisPool")
    public JedisPool jedisPool(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port, @Value("${spring.redis.password}") String password, @Value("${spring.redis.database}") int database, @Value("${spring.redis.timeout}") int timeout, @Value("${spring.redis.pool.max-wait}") int maxWait, @Value("${spring.redis.pool.max-idle}") int maxIdle, @Value("${spring.redis.pool.min-idle}") int minIdle) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        return new JedisPool(jedisPoolConfig, host, port, timeout, StringUtil.invalid(password) ? null : password, database);
    }

    @Bean(name = "appCache")
    public AppCache appCache() {
        return new AppCache();
    }

    @Bean(name = "redisCache")
    public RedisCache redisCache() {
        return new RedisCache();
    }

    @Bean
    public ConnectionFactory activeMQConnectionFactory(@Value("${spring.activemq.broker-url}") String brokerURL) {
        return new ActiveMQConnectionFactory(brokerURL);
    }

    @Bean
    public JmsListenerContainerFactory<?> topicListenerContainer(ConnectionFactory activeMQConnectionFactory) {
        DefaultJmsListenerContainerFactory topicListenerContainer = new DefaultJmsListenerContainerFactory();
        topicListenerContainer.setPubSubDomain(true);
        topicListenerContainer.setConnectionFactory(activeMQConnectionFactory);
        return topicListenerContainer;
    }


}

@WebListener
class AppListener implements ServletContextListener {
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    AppCache appCache;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("====================|ServletContex初始化|====================");
        List<String> requestMappingList = new ArrayList<>();
        AbstractHandlerMethodMapping<RequestMappingInfo> objHandlerMethodMapping = (AbstractHandlerMethodMapping<RequestMappingInfo>) applicationContext.getBean("requestMappingHandlerMapping");
        Map<RequestMappingInfo, HandlerMethod> mapRet = objHandlerMethodMapping.getHandlerMethods();
        for (RequestMappingInfo requestMappingInfo : mapRet.keySet()) {
            Set<String> patterns = requestMappingInfo.getPatternsCondition().getPatterns();
            requestMappingList.addAll(patterns);
        }
        appCache.setRequestMappingList(requestMappingList);
        appCache.set("appStartDate", DateUtil.getDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
        System.out.println("==================|ServletContex初始化完成|==================");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("===================================================================ServletContex销毁");
    }
}

@Component
class AppRunner implements ApplicationRunner {
    @Autowired
    AppCache appCache;
    @Autowired
    RedisCache redisCache;

    @Override
    public void run(ApplicationArguments applicationArguments) {
        try {
            System.out.println("=====================|工具配置初始化|======================");
            String text = AppInitFunction.getInstance().getTextInfo();
            System.out.println("|获取readme|" + text);
            appCache.set("readme", text);

            System.out.println("|初始化SystemConfig|查看命令：curl http://localhost:" + AppCache.getConfigValue("server.port", "1205") + "/checkConfig");
            appCache.setSystemConfig(AppCache.getSystemConfig());

            System.out.println("====================|工具配置初始化完成|====================");
            System.out.println("======================|项目启动成功|=======================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@WebFilter(filterName = "sessionFilter", urlPatterns = {"/*"})
class SessionFilter implements Filter {
    @Autowired
    AppCache appCache;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String body = StringUtil.InputStreamToString(request.getInputStream());
        request.setAttribute("body", body);
        Map<String, String> requestParameter = request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]));
        String date = DateUtil.getDateString(new Date(), "【yyyy-MM-dd HH:mm:ss】");
        String[] whitelist = {"/apiError", "/webError", "/", "/index"};
        String[] rpcApiList = {"/heartbeat", "/checkConfig", "/refreshConfig"};
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        if (StringUtil.isHave(whitelist, uri) || uri.startsWith("/static") || "/favicon.ico".equals(uri)) {
            filterChain.doFilter(request, response);
        } else {
            if (!appCache.getRequestMappingList().contains(uri)) {
                System.out.println(date + "request：====【找不到路径】===|" + request.getMethod() + "|" + uri + "|===|" + ObjectUtil.objectToJsonString(requestParameter) + "|===|" + body + "|");
                Map<String, Object> result = new LinkedHashMap<String, Object>() {{
                    put("rspCode", 0);
                    put("rspInfo", "Path request denied");
                }};
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(ObjectUtil.mapToJsonString(result));
            } else if (StringUtil.isHave(rpcApiList, uri)) {
                System.out.println(date + "request：========rmi========|" + request.getMethod() + "|" + uri + "|===|" + ObjectUtil.objectToJsonString(requestParameter) + "|===|" + body + "|");
                filterChain.doFilter(request, response);
            } else {
                if (uri.startsWith("/api")) {
                    System.out.println(date + "request：========api========|" + request.getMethod() + "|" + uri + "|===|" + ObjectUtil.objectToJsonString(requestParameter) + "|===|" + body + "|");
                } else {
                    System.out.println(date + "request：========web========|" + request.getMethod() + "|" + uri + "|===|" + ObjectUtil.objectToJsonString(requestParameter) + "|===|" + body + "|");
                }
                filterChain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {
    }
}

@Configuration
@MapperScan(basePackages = "win.panyong.dao.mysql", sqlSessionTemplateRef = "mysqlSqlSessionTemplate")
class MysqlDataSourceConfig {

    @Bean(name = "mysqlDataSource")
    @Qualifier("mysqlDataSource")
    @Primary
    @ConfigurationProperties(prefix = "datasource.mysql")
    public DataSource mysqlDataSource() {
        System.out.println("-------------------- mysqlDataSource init ---------------------");
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "mysqlSqlSessionFactory")
    @Primary
    public SqlSessionFactory mysqlSqlSessionFactory(@Qualifier("mysqlDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "mysqlTransactionManager")
    @Primary
    public DataSourceTransactionManager mysqlTransactionManager(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "mysqlSqlSessionTemplate")
    @Primary
    public SqlSessionTemplate mysqlSqlSessionTemplate(@Qualifier("mysqlSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

@Configuration
@MapperScan(basePackages = "win.panyong.dao.oracle", sqlSessionTemplateRef = "oracleSqlSessionTemplate")
class OracleDataSourceConfig {
    @Bean(name = "oracleDataSource")
    @Qualifier("oracleDataSource")
    @ConfigurationProperties(prefix = "datasource.oracle")
    public DataSource oracleDataSource() {
        System.out.println("-------------------- oracleDataSource init ---------------------");
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "oracleSqlSessionFactory")
    public SqlSessionFactory oracleSqlSessionFactory(@Qualifier("oracleDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Bean(name = "oracleTransactionManager")
    public DataSourceTransactionManager oracleTransactionManager(@Qualifier("oracleDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "oracleSqlSessionTemplate")
    public SqlSessionTemplate oracleSqlSessionTemplate(@Qualifier("oracleSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

@Configuration
@EnableAsync
class AppScheduled {
    @Autowired
    AppCache appCache;

    private int corePoolSize = 10;
    private int maxPoolSize = 200;
    private int queueCapacity = 10;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.initialize();
        return executor;
    }

    @Async
    @Scheduled(fixedRate = 1000 * 7100, initialDelay = 1000 * 7100)
    void reflushDingtalkAccessToken() {
        try {
            System.out.println("|定时|" + "方式1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    @Scheduled(cron = "0 */2 * * * ?")
    void synchronousDingtalkStaffData() {
        System.out.println("|定时|" + "方式2");

    }
}

@Configuration
class MvcConfig implements WebMvcConfigurer {
    @Autowired
    AppCache appCache;

    private LinkedHashMap<String, String> viewList = new LinkedHashMap<String, String>() {{
        //k:访问地址，v:模板位置
        put("/testpage", "/testpage/index");
    }};

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        viewList.forEach((k, v) -> {
            registry.addViewController(k).setViewName(v);
            appCache.addToRequestMappingList(k);
        });
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String configDir = AppCache.getConfigValue("spring.freemarker.template-loader-path", "classpath:/");
        if (configDir.startsWith("file:")) {
            File file = new File(configDir.substring(5));
            if (file.isDirectory()) {
                registry.addResourceHandler("/page/**").addResourceLocations(configDir);
            }
        }
    }

//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("GET", "POST").allowCredentials(false).maxAge(3600);
//    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PromisionInterceptor()).addPathPatterns("/**");
    }
}