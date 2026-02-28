package win.panyong;

import com.alibaba.fastjson2.JSONObject;
import jakarta.jms.ConnectionFactory;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.server.servlet.context.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import win.panyong.utils.*;
import win.panyong.utils.authority.PermissionInterceptor;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@ServletComponentScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "appCache")
    public AppCache appCache() {
        return new AppCache();
    }

    @Bean(name = "redisUtil")
    public RedisUtil redisUtil(@Value("${datasource.redis.host}") String host, @Value("${datasource.redis.port}") int port, @Value("${datasource.redis.password}") String password, @Value("${datasource.redis.database}") int database, @Value("${datasource.redis.timeout}") int timeout, @Value("${datasource.redis.pool.max-wait}") int maxWait, @Value("${datasource.redis.pool.max-idle}") int maxIdle, @Value("${datasource.redis.pool.min-idle}") int minIdle) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWait);
        return new RedisUtil(database, new JedisPool(jedisPoolConfig, host, port, timeout, StringUtil.invalid(password) ? null : password, database));
    }

    @Bean(name = "jwtUtil")
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration:7200000}") Long expiration) {
        return new JwtUtil(secret, expiration);
    }

    @Bean
    public PermissionInterceptor permissionInterceptor() {
        return new PermissionInterceptor();
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

@Configuration
@MapperScan(basePackages = "win.panyong.mapper.sqlite", sqlSessionTemplateRef = "sqliteSqlSessionTemplate")
class SqliteDataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    @Value("${datasource.sqlite.jdbc_url}")
    String sqliteDbFilePath;

    @Primary
    @Bean(name = "sqliteDataSource")
    @Qualifier("sqliteDataSource")
    @ConfigurationProperties(prefix = "datasource.sqlite")
    public DataSource sqliteDataSource() throws IOException, SQLException {
        logger.info("------------------- sqliteDataSource init ---------------------");
        File targetFile = new File(sqliteDbFilePath.split(":")[2]);
        if (!targetFile.exists()) {
            logger.info("-------------- SqliteDB file not found now init ---------------");
            File sourceFile = AppCache.getResource("localDB.db").getFile();
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "sqliteSqlSessionFactory")
    public SqlSessionFactory sqliteSqlSessionFactory(@Qualifier("sqliteDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean.getObject();
    }

    @Primary
    @Bean(name = "sqliteTransactionManager")
    public DataSourceTransactionManager sqliteTransactionManager(@Qualifier("sqliteDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Primary
    @Bean(name = "sqliteSqlSessionTemplate")
    public SqlSessionTemplate sqliteSqlSessionTemplate(@Qualifier("sqliteSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

//@Configuration
//@MapperScan(basePackages = "win.panyong.mapper.mysql", sqlSessionTemplateRef = "mysqlSqlSessionTemplate")
//class MysqlDataSourceConfig {
//    private static final Logger logger = LoggerFactory.getLogger(Application.class);
//
//    @Bean(name = "mysqlDataSource")
//    @Qualifier("mysqlDataSource")
//    @ConfigurationProperties(prefix = "datasource.mysql")
//    public DataSource mysqlDataSource() {
//        logger.info("-------------------- mysqlDataSource init ---------------------");
//        return DataSourceBuilder.create().build();
//    }
//
//    @Bean(name = "mysqlSqlSessionFactory")
//    public SqlSessionFactory mysqlSqlSessionFactory(@Qualifier("mysqlDataSource") DataSource dataSource) throws Exception {
//        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
//        bean.setDataSource(dataSource);
//        return bean.getObject();
//    }
//
//    @Bean(name = "mysqlTransactionManager")
//    public DataSourceTransactionManager mysqlTransactionManager(@Qualifier("mysqlDataSource") DataSource dataSource) {
//        return new DataSourceTransactionManager(dataSource);
//    }
//
//    @Bean(name = "mysqlSqlSessionTemplate")
//    public SqlSessionTemplate mysqlSqlSessionTemplate(@Qualifier("mysqlSqlSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }
//}

@RestControllerAdvice
class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<String> appExceptionHandler(AppException appException) {
        logger.error("error:", appException);
        if (appException.getErrorCode() == 80401) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON).body(Result.fail(80401, appException.getMessage()).buildJsonString());
        } else {
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).contentType(MediaType.APPLICATION_JSON).body(Result.fail(appException.getMessage()).buildJsonString());
        }
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception exception) {
        logger.error("error:", exception);
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).contentType(MediaType.APPLICATION_JSON).body(Result.fail("服务器异常").buildJsonString());
    }
}

@Component
class AppRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private final ApplicationContext applicationContext;
    private final AppCache appCache;
    private final RedisUtil redisUtil;
    @Value("${server.port}")
    private String port;

    public AppRunner(ApplicationContext applicationContext, AppCache appCache, RedisUtil redisUtil) {
        this.applicationContext = applicationContext;
        this.appCache = appCache;
        this.redisUtil = redisUtil;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) {
        try {
            logger.info("=====================|系统配置初始化|======================");
            AppInitFunction.getInstance().initConfig(redisUtil);
            AppInitFunction.getInstance().initServletContext(applicationContext, appCache);
            logger.info("====================|系统配置初始化完成|====================");
            logger.info("|SystemConfig|查看命令：curl http://localhost:{}/checkConfig", port);
            logger.info("======================|项目启动成功|=======================");
        } catch (Exception _) {
        }
    }
}

@Component
class MvcConfig implements WebMvcConfigurer {
    @Value("${page.path}")
    String pagePath;
    @Autowired
    PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/pages/**").addResourceLocations(pagePath);
        registry.addResourceHandler("/static/**").addResourceLocations("file:" + new ApplicationHome(getClass()) + "/");
    }
}

@Aspect
@Component
class ServiceAop {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @AfterReturning(value = "execution(* win.panyong.controller.*Controller.*(..)) && @annotation(appLog)", returning = "result")
    public void setAppLogAfterReturning(JoinPoint joinPoint, AppLog appLog, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        //获取 请求处理方法
        String methodFullName = signature.getDeclaringTypeName() + "." + signature.getName() + "(..)";
        //获取request query
        Map<String, String> requestParameter = request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]));
        //获取request body
        Object requestBody = Arrays.stream(signature.getMethod().getParameterAnnotations()).map(ListUtil.functionWithIndex((annotations, index) -> Arrays.stream(annotations).anyMatch(annotation -> annotation instanceof RequestBody) ? joinPoint.getArgs()[index] : null)).filter(Objects::nonNull).findFirst().orElse(null);
        logger.info("\n========================================================================================================================" +
                "\n描述:|" + appLog.value() +
                "\n请求:|" + method + "|" + uri +
                "\n处理:|" + methodFullName +
                "\n参数:|" + ObjectUtil.objectToJsonString(requestParameter) + "|" + requestBody +
                "\n返回:|" + result.toString() +
                "\n========================================================================================================================");
    }

    @AfterThrowing(value = "execution(* win.panyong.controller.*Controller.*(..)) && @annotation(appLog)", throwing = "exception")
    public void setAppLogAfterThrowing(JoinPoint joinPoint, AppLog appLog, Exception exception) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        //获取 请求处理方法
        String methodFullName = signature.getDeclaringTypeName() + "." + signature.getName() + "(..)";
        //获取request query
        Map<String, String> requestParameter = request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]));
        //获取request body
        Object requestBody = Arrays.stream(signature.getMethod().getParameterAnnotations()).map(ListUtil.functionWithIndex((annotations, index) -> Arrays.stream(annotations).anyMatch(annotation -> annotation instanceof RequestBody) ? joinPoint.getArgs()[index] : null)).filter(Objects::nonNull).findFirst().orElse(null);

        AppException appException = exception instanceof AppException ? (AppException) exception : new AppException(exception.getMessage());
        logger.info("\n========================================================================================================================" +
                "\n描述:|" + appLog.value() +
                "\n请求:|" + method + "|" + uri +
                "\n处理:|" + methodFullName +
                "\n参数:|" + ObjectUtil.objectToJsonString(requestParameter) + "|" + requestBody +
                "\n异常:|" + appException +
                "\n========================================================================================================================");
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
//        String body = StringUtil.InputStreamToString(request.getInputStream());
//        request.setAttribute("body", body);
        String body = "{}";
        Map<String, String> requestParameter = request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]));
        String date = DateUtil.getDateString(new Date(), "【yyyy-MM-dd HH:mm:ss】");
        String[] whitelist = {};
        String[] pathWhitelist = {"/static/"};
        String[] rmiWhitelist = {"/heartbeat", "/checkConfig", "/refreshConfig",};
        String[] rmiPathWhitelist = {};
        String uri = request.getRequestURI().substring(request.getContextPath().length());
        if (StringUtil.isHave(uri, whitelist) || Arrays.stream(pathWhitelist).anyMatch(uri::startsWith)) {
            filterChain.doFilter(request, response);
        } else {
            if (!appCache.getRequestMappingList().contains(uri)) {
                System.out.println(date + "request：====【找不到路径】===|" + request.getMethod() + "|" + uri + "|===|" + ObjectUtil.objectToJsonString(requestParameter) + "|===|" + body + "|");
                JSONObject result = new JSONObject().fluentPut("rspCode", 0).fluentPut("rspInfo", "Path request denied");
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(result.toString());
            } else if (StringUtil.isHave(uri, rmiWhitelist) || Arrays.stream(rmiPathWhitelist).anyMatch(uri::startsWith)) {
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

@Component
@EnableAsync
class AppScheduled {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(200);
        executor.setQueueCapacity(10);
        executor.initialize();
        return executor;
    }

    @Async
//    @Scheduled(fixedRate = 1000 * 10, initialDelay = 1000 * 10)
    void scheduled1() {
        try {
            System.out.println("|定时|" + "方式1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
//    @Scheduled(cron = "0 */1 * * * ?")
    void scheduled2() throws InterruptedException {
        try {
            System.out.println("|定时|" + "方式2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}