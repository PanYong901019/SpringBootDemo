package win.panyong.controller;

import com.easyond.utils.DateUtil;
import com.easyond.utils.ObjectUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import win.panyong.utils.AppCache;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CommonController extends BaseController {

    @ResponseBody
    @RequestMapping(value = "heartbeat", produces = "application/json;charset=UTF-8")
    String heartbeat() {
        rspCode = OK;
        rspInfo = "success";
        rspResult.put("uptime", DateUtil.daysBetweenString(appCache.get("appStartDate"), new Date()));
        rspResult.put("requestParameter", request.getParameterMap().isEmpty() ? null : ObjectUtil.objectToJsonString(request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]))));
        rspResult.put("requestBody", getRequestBody());
        return getResultJsonString();
    }

    @ResponseBody
    @RequestMapping(value = "/apiError", produces = "application/json;charset=UTF-8")
    String error() {
        String type = getParameter("type");
        switch (type) {
            case "1":
                rspInfo = "没有权限";
                break;
            case "2":
                rspInfo = "访问异常";
                break;
            default:
        }
        return getResultJsonString();
    }

    @RequestMapping(value = "/webError")
    String errorPage() {
        return "errorPage";
    }

    @ResponseBody
    @RequestMapping(value = "/checkConfig", produces = "application/json;charset=UTF-8")
    String checkConfig() {
        Map<String, String> config = AppCache.getSystemConfig();
        rspCode = OK;
        rspInfo = "success";
        rspResult.put("config", config);
        return getResultJsonString();
    }

    @ResponseBody
    @RequestMapping(value = "/refreshConfig", produces = "application/json;charset=UTF-8")
    String refreshConfig() {
        Map<String, String> config = AppCache.initSystemConfig("application.properties", "app.properties");
        appCache.setSystemConfig(config);
        rspCode = OK;
        rspInfo = "success";
        rspResult.put("config", config);
        return getResultJsonString();
    }
}
