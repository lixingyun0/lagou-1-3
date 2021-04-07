package com.xingyun.mvc.controller;

import com.xingyun.mvc.annotation.MyAutowired;
import com.xingyun.mvc.annotation.MyController;
import com.xingyun.mvc.annotation.MyRequestMapping;
import com.xingyun.mvc.annotation.MySecurity;
import com.xingyun.mvc.service.IDemoService;
import org.springframework.web.bind.annotation.RequestMapping;

@MyController
@MyRequestMapping("/demo")
public class DemoController {

    @MyAutowired
    private IDemoService demoService;

    @MyRequestMapping("/hello")
    @MySecurity({"明明"})
    public String hello(String name){
        return demoService.hello(name);
    }

    @MyRequestMapping("/bye")
    @MySecurity({"红红"})
    public String bye(String name){
        return demoService.bye(name);
    }
}
