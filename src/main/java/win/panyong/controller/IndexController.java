package win.panyong.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.panyong.model.User;
import win.panyong.utils.AppException;
import win.panyong.utils.AppLog;
import win.panyong.utils.Result;
import win.panyong.utils.StringUtil;
import win.panyong.utils.authority.Permission;
import win.panyong.utils.authority.PermissionType;

@RestController
@RequestMapping(value = "/api")
public class IndexController extends BaseController {

    @PostMapping(value = "/test")
    @AppLog("测试")
    @Permission({PermissionType.ADMIN, PermissionType.USER})
    String test() {
        return Result.ok(getLoginUser()).buildJsonString();
    }

    @PostMapping(value = "/login")
    @AppLog("登录")
    String login(@RequestBody JSONObject body) {
        Result.Builder result;
        if (!StringUtil.anyInvalid(body.getString("username"), body.getString("password"))) {
            try {
                User user = indexService.doLogin(body.getString("username"), body.getString("password"));
                // 生成JWT token
                String token = jwtUtil.generateToken(user.getId(), user.getNickname());
                response.setHeader("Authorization", "Bearer " + token);

                result = Result.ok(user.setPassword(null));
            } catch (AppException e) {
                result = Result.fail(e.getMessage());
            }
        } else {
            throw new AppException("参数错误");
        }
        return result.buildJsonString();
    }
}
