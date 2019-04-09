package win.panyong.controller;

import com.easyond.utils.ObjectUtil;
import com.easyond.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.WebUtils;
import win.panyong.utils.AppCache;
import win.panyong.utils.RedisCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class BaseController {
    protected static Integer FAIL = 0;
    protected static Integer OK = 1;
    protected Integer rspCode = FAIL;
    protected String rspInfo = "fail";
    protected LinkedHashMap<String, Object> rspResult = new LinkedHashMap<>();
    @Autowired
    protected AppCache appCache;
    @Autowired
    protected RedisCache redisCache;
    @Autowired
    protected JmsTemplate jmsTemplate;
    @Autowired
    protected HttpSession session;
    @Autowired
    protected HttpServletRequest request;

    protected String getParameter(String parameterKey) {
        return WebUtils.findParameterValue(request, parameterKey);
    }

    protected String getRequestBody() {
        String body = request.getAttribute("body").toString();
        return StringUtil.invalid(body) ? null : body;
    }

    protected Map<String, Object> getResultObject() {
        Map<String, Object> result = new LinkedHashMap<String, Object>() {{
            put("rspCode", rspCode);
            put("rspInfo", rspInfo);
            put("rspResult", rspResult);
        }};
        rspResult = new LinkedHashMap<>();
        return result;
    }

    protected String getResultJsonString() {
        //注释的代码是用来支持jsonp
//        String callback = getParameter("callback");
//        if (!StringUtil.invalid(callback) && !StringUtil.invalid(getParameter("_"))) {
//            return callback + "(" + ObjectUtil.mapToJsonString(getResultObject()) + ");";
//        } else {
        return ObjectUtil.mapToJsonString(getResultObject());
//        }
    }

}
