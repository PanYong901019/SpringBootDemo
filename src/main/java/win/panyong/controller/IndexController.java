package win.panyong.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.panyong.utils.AppLog;
import win.panyong.utils.Result;
import win.panyong.utils.authority.Permission;
import win.panyong.utils.authority.PermissionType;

@Permission({PermissionType.ADMIN, PermissionType.USER})
@RestController
@RequestMapping(value = "/api")
public class IndexController extends BaseController {

    @GetMapping(value = "/index")
    @AppLog("测试")
    String index() {
        return Result.ok().putData("userId", getUserId()).buildJsonString();
    }
}
