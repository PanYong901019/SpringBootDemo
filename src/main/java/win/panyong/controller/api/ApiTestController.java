package win.panyong.controller.api;

import com.easyond.utils.DateUtil;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import win.panyong.controller.BaseController;

import java.util.Date;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class ApiTestController extends BaseController {

    @RequestMapping("/mqtest")
    String index() throws Exception {
        jmsTemplate.convertAndSend(new ActiveMQQueue("queue-1"), DateUtil.getDateString(new Date(), "yyyy-MM-dd HH:mm:ss") + " hello,activeMQ queue1");
        jmsTemplate.convertAndSend(new ActiveMQTopic("topic-1"), DateUtil.getDateString(new Date(), "yyyy-MM-dd HH:mm:ss") + " hello,activeMQ topic1");
        rspCode = OK;
        rspInfo = "发送成功";
        return getResultJsonString();
    }
}
