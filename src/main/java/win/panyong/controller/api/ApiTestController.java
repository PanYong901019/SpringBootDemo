package win.panyong.controller.api;

import com.easyond.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.panyong.utils.RedisCache;

import java.util.Date;

@RestController
@RequestMapping(value = "/api")
public class ApiTestController {
    @Autowired
    private RedisCache redisCache;

    @RequestMapping("/test")
    String index() throws Exception {
        redisCache.pipe(p -> redisCache.set("test", "testtesttest" + DateUtil.getDate(new Date(), "yyyyMMddHHssmm")));
        return "Hello World!";
    }


}
