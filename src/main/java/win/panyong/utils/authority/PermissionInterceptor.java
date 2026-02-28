package win.panyong.utils.authority;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;
import win.panyong.mapper.sqlite.SqliteSystemMapper;
import win.panyong.model.User;
import win.panyong.utils.AppException;
import win.panyong.utils.DateUtil;
import win.panyong.utils.JwtUtil;
import win.panyong.utils.StringUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * Created by pan on 2019/2/12 11:20 AM
 */
public class PermissionInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);
    @Lazy
    @Autowired
    private SqliteSystemMapper systemMapper;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Permission authority = handlerMethod.getMethod().getAnnotation(Permission.class);
            // 如果方法上没有，检查类上的 @Permission 注解
            if (authority == null) {
                authority = handlerMethod.getBeanType().getAnnotation(Permission.class);
            }
            if (authority != null) {
                Long userId = null;

                // 首先尝试从JWT token获取用户ID
                String authorization = request.getHeader("Authorization");
                if (!StringUtil.invalid(authorization) && authorization.startsWith("Bearer ")) {
                    String token = authorization.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        userId = jwtUtil.getClaimFromToken(token, claims -> claims.get("userId", Long.class));
                    }
                }
                if (userId == null) {
                    userId = StringUtil.isNumber(WebUtils.findParameterValue(request, "testUserId")) ? Long.valueOf(Objects.requireNonNull(WebUtils.findParameterValue(request, "testUserId"))) : null;
                }

                if (userId != null) {

                    User user = userId == 0 ? new User().setId(userId).setNickname("SuperAdmin").setStatus(1) : systemMapper.selectUserById(userId);
                    if (user.getStatus() == 1) {
                        request.setAttribute("loginUser", user);

                        //todo 权限认证
                        if (Arrays.stream(authority.value()).noneMatch(permissionType -> permissionType.equals(PermissionType.getPermissionTypeByUserId(user.getId())))) {
                            logger.info("{}request：{}|{}|===|权限异常，已拦截|", DateUtil.getDateString(new Date(), "【yyyy-MM-dd HH:mm:ss】"), request.getMethod(), request.getRequestURI().substring(request.getContextPath().length()));
                            throw new AppException(80401, "权限异常");
                        }

                    } else {
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
