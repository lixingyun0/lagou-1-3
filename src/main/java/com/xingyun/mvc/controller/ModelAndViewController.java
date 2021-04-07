package com.xingyun.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/demo")
public class ModelAndViewController {

    @RequestMapping("/model")
    public ModelAndView model(){
        ModelAndView modelAndView =new ModelAndView();
        modelAndView.addObject("date",new Date());
        modelAndView.setViewName("success");
        return modelAndView;
    }

    @RequestMapping("/model2")
    public String model2(){
        Map<String,Object> data = new HashMap<>();

        data.put("date",new Date());
        return "success";
    }

    @RequestMapping("/model3")
    public String model3(Map<String,Object> data){
        data.put("date",new Date());
        data.put("name","星云");
        return "success";
    }

}
