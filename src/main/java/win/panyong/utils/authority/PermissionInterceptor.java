package win.panyong.utils.authority;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
import win.panyong.utils.AppException;
import win.panyong.utils.DateUtil;
import win.panyong.utils.StringUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Created by pan on 2019/2/12 11:20 AM
 */
public class PermissionInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Permission authority = handlerMethod.getMethod().getAnnotation(Permission.class);
            // 如果方法上没有，检查类上的 @Permission 注解
            if (authority == null) {
                authority = handlerMethod.getBeanType().getAnnotation(Permission.class);
            }
            if (authority != null) {
                Long userId = request.getSession().getAttribute("userId") != null ? Long.valueOf(request.getSession().getAttribute("userId").toString()) : (StringUtil.isNumber(WebUtils.findParameterValue(request, "testUserId")) ? Long.valueOf(Objects.requireNonNull(WebUtils.findParameterValue(request, "testUserId"))) : null);
                if (userId != null) {
                    request.setAttribute("userId", userId);
                    if (Arrays.stream(authority.value()).noneMatch(permissionType -> permissionType.equals(PermissionType.getPermissionTypeByUserId(userId)))) {
                        logger.info("{}request：{}|{}|===|权限异常，已拦截|", DateUtil.getDateString(new Date(), "【yyyy-MM-dd HH:mm:ss】"), request.getMethod(), request.getRequestURI().substring(request.getContextPath().length()));
                        throw new AppException(80401, "权限异常");
                    }
                } else {
                    throw new AppException(80401, "未登录");
                }
            }
        }
        return true;
    }
}
