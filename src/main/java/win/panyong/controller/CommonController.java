package win.panyong.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import win.panyong.utils.*;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class CommonController extends BaseController {

    @RequestMapping(value = "/")
    String index() {
        return "redirect:/heartbeat";
    }

    @AppLog
    @ResponseBody
    @RequestMapping(value = "/heartbeat", produces = "application/json;charset=UTF-8")
    String heartbeat(@RequestBody(required = false) JSONObject body) {
        return Result.ok()
                .putData("serverName", AppException.service)
                .putData("requestParameter", request.getParameterMap().isEmpty() ? null : ObjectUtil.objectToJsonString(request.getParameterMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue()[0]))))
                .putData("body", body)
                .buildJsonString();
    }

    @AppLog
    @ResponseBody
    @RequestMapping(value = "/checkConfig", produces = "application/json;charset=UTF-8")
    String checkConfig() {
        return Result.ok().setData(AppCache.getSystemConfig()).buildJsonString();
    }

    @AppLog
    @ResponseBody
    @RequestMapping(value = "/refreshConfig", produces = "application/json;charset=UTF-8")
    String refreshConfig() {
        return Result.ok().setData(AppCache.initSystemConfig("app.properties")).buildJsonString();
    }

}
