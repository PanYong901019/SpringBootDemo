package win.panyong.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.WebUtils;
import win.panyong.model.User;
import win.panyong.service.IndexService;
import win.panyong.service.SystemService;
import win.panyong.utils.AppCache;
import win.panyong.utils.JwtUtil;
import win.panyong.utils.RedisUtil;

@Controller
public class BaseController {
    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    protected AppCache appCache;
    @Autowired
    protected RedisUtil redisUtil;
    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected SystemService systemService;
    @Autowired
    protected IndexService indexService;

    protected String getParameter(String parameterKey) {
        return WebUtils.findParameterValue(request, parameterKey);
    }

    protected User getLoginUser() {
        return (User) request.getAttribute("loginUser");
    }

    protected Long getUserId() {
        return getLoginUser().getId();
    }

}
