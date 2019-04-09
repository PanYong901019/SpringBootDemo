package win.panyong.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import win.panyong.controller.BaseController;

@Controller
public class WebTestController extends BaseController {


    @RequestMapping(value = {"/freemarkerPage"})
    ModelAndView freemarkerPage() {
        return new ModelAndView("freemarkerPage/hellofreemarker").addObject("text", "freemarker");
    }

    @RequestMapping(value = {"/jspPage"})
    ModelAndView jspPage() {
        return new ModelAndView("jspPage/hellojsp").addObject("text", "jsp");
    }
}
