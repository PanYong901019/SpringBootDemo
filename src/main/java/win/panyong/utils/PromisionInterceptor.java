package win.panyong.utils;

import com.easyond.utils.DateUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

public class PromisionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            Promision authority = method.getAnnotation(Promision.class);
            if (authority == null) {
                return true;
            } else {
                String date = DateUtil.getDateString(new Date(), "【yyyy-MM-dd HH:mm:ss】");
                String uri = request.getRequestURI().substring(request.getContextPath().length());
                System.out.println(date + "request：" + request.getMethod() + "|" + uri + "|===|权限异常，已拦截|");
                response.sendRedirect("/apiError?type=1");
                return false;
            }
        } else {
            return true;
        }
    }
}
