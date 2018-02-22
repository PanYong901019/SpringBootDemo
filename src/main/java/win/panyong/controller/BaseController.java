package win.panyong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BaseController {


    @RequestMapping(value = "/apiError")
    String error() {
        return "";
    }

    @RequestMapping(value = "/webError")
    String errorPage() {
        return "errorPage";
    }

}
