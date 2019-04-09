package win.panyong.service.impl;


import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import win.panyong.service.BaseService;
import win.panyong.service.CommonService;

@Service(value = "commonService")
public class CommonServiceImpl extends BaseService implements CommonService {


    @JmsListener(destination = "queue-1")
    public void queueRecieve1(String message) {
        System.out.println("queue-1###################" + message + "###################");
    }

    @JmsListener(destination = "queue-2")
    public void queueRecieve2(String message) {
        System.out.println("queue-2###################" + message + "###################");
    }

    @JmsListener(destination = "topic-1", containerFactory = "topicListenerContainer")
    public void topicRecieve1(String message) {
        System.out.println("topic-1.1###################" + message + "###################");
    }

    @JmsListener(destination = "topic-1", containerFactory = "topicListenerContainer")
    public void topicRecieve2(String message) {
        System.out.println("topic-1.2###################" + message + "###################");
    }

}