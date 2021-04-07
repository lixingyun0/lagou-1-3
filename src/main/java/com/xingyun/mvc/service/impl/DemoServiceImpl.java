package com.xingyun.mvc.service.impl;

import com.xingyun.mvc.annotation.MyService;
import com.xingyun.mvc.service.IDemoService;

@MyService("demoService")
public class DemoServiceImpl implements IDemoService {
    @Override
    public String hello(String name) {
        return "hello " + name;
    }

    @Override
    public String bye(String name) {
        return "bye " + name;
    }
}
