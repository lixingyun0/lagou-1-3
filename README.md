# lagou-1-3
第一阶段模块3


#### 有两个handler
- /demo/hello
- /demo/bye

```java

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
```

使用不匹配的用户名 会跳转权限不足页面
