package com.xingyun.mvc.controller;

import com.xingyun.mvc.annotation.MyController;
import com.xingyun.mvc.annotation.MyRequestMapping;
import com.xingyun.mvc.annotation.MySecurity;
import com.xingyun.mvc.factory.BeanFactory;
import com.xingyun.mvc.handler.Handler;
import org.springframework.util.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class DemoServlet extends HttpServlet {

    private BeanFactory beanFactory;

    private Properties properties;

    private List<Handler> handlerList = new ArrayList<>();


    @Override
    public void init(ServletConfig config)  {
        System.out.println("tomcat 调用init方法");
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        properties = new Properties();
        try {
            properties.load(DemoServlet.class.getClassLoader().getResourceAsStream(contextConfigLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
        beanFactory = new BeanFactory(properties.getProperty("basePackage"));
        beanFactory.init();

        for (Map.Entry<String, Object> stringObjectEntry : beanFactory.getContainer().entrySet()) {
            Object value = stringObjectEntry.getValue();
            addHandler(value);
        }


    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestURI = req.getRequestURI();
        for (Handler handler : handlerList) {
            if (requestURI.equals(handler.getUrl())){
                String name = req.getParameter("name");
                MySecurity annotation = handler.getMethod().getAnnotation(MySecurity.class);
                if (annotation != null) {
                    List<String> nameList = Arrays.stream(annotation.value()).collect(Collectors.toList());
                    if (!nameList.contains(name)){
                        req.getRequestDispatcher("/WEB-INF/error.jsp").forward(req,resp);
                        //resp.sendRedirect("/WEB-INF/error.jsp");

                        return;
                    }


                }
                Map<String, String[]> parameterMap = req.getParameterMap();
                Object[] objects = bindParams(handler.getMethod(), parameterMap, req, resp);
                try {
                    Object invoke = handler.getMethod().invoke(handler.getController(), objects);
                    resp.setContentType("text/html; charset=utf-8");
                    resp.setCharacterEncoding("utf-8");
                    resp.getWriter().println(invoke);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Object[] bindParams(Method method,Map<String, String[]> parameterMap,HttpServletRequest req, HttpServletResponse resp){
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> type = parameters[i].getType();
            if (HttpServletRequest.class.isAssignableFrom(type)){
                params[i] = req;
                continue;
            }
            if (HttpServletResponse.class.isAssignableFrom(type)){
                params[i] = resp;
                continue;
            }
            String name = parameters[i].getName();
            params[i] = parameterMap.get(name)[0];

        }

        return params;

    }

    private void addHandler(Object target){

        if (!target.getClass().isAnnotationPresent(MyController.class)){
            return;
        }
        String baseUrl = "";
        MyRequestMapping annotation = target.getClass().getAnnotation(MyRequestMapping.class);
        if (annotation != null){
            baseUrl =  annotation.value();
        }

        for (Method method : target.getClass().getMethods()) {
            MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
            if (myRequestMapping == null){
                return;
            }
            String url = baseUrl + myRequestMapping.value();
            if (StringUtils.hasText(url)){
                Handler handler = new Handler();
                handler.setUrl(url);
                handler.setController(target);
                handler.setMethod(method);
                handlerList.add(handler);
            }

        }



    }
}
