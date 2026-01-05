package win.panyong.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.WebUtils;
import win.panyong.utils.AppCache;
import win.panyong.utils.RedisUtil;

@Controller
public class BaseController {
    @Autowired
    protected AppCache appCache;
    @Autowired
    protected RedisUtil redisUtil;
    @Autowired
    protected HttpServletRequest request;

    protected String getParameter(String parameterKey) {
        return WebUtils.findParameterValue(request, parameterKey);
    }

    protected Long getUserId() {
        return (Long) request.getAttribute("userId");
    }

}
