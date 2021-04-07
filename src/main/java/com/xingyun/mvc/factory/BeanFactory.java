package com.xingyun.mvc.factory;


import com.xingyun.mvc.annotation.MyAutowired;
import com.xingyun.mvc.annotation.MyController;
import com.xingyun.mvc.annotation.MyService;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author xingyun
 * @date 2021/3/30
 */

public class BeanFactory {

    private String basePackage;

    public BeanFactory(String basePackage) {
        this.basePackage = basePackage;
    }

    private Map<String,Object> container = new HashMap<>();

    public void init(){
        loadFromAnnotation(basePackage);
    }

    public  <T> T getBeanById(String id){
        return (T) container.get(id);
    }

    public Map<String, Object> getContainer() {
        return container;
    }

    private void loadFromAnnotation(String basePackage){

        //扫描包下面带有自定义注解的所有类
        List<String> classNameList = classNameList(basePackage);

        //需要注入依赖的beanId
        Set<String> beanNeedPropertyIdList = new HashSet<>();

        //需要生成代理类的beanId
        Set<String> beanNeedProxyIdList = new HashSet<>();

        //实例化bean并放入容器

        for (String className : classNameList) {

            try {
                Class<?> aClass = Class.forName(className);
                String value = "";
                //获取MyService注解的value值，如果不为空，则bean的ID为value
                MyService annotation = aClass.getAnnotation(MyService.class);
                if (annotation!=null){
                    value = annotation.value();
                }

                Object o = aClass.newInstance();

                //如果value为空，则beanId为类名的首字母小写
                if (!StringUtils.hasText(value)){
                    value = getBeanName(className);
                }

                container.put(value,o);
                //获取需要注入属性或者生成代理的beanId，放入set集合
                needPropertyOrProxy(value,beanNeedPropertyIdList);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        //注入bean的依赖
        doDI(beanNeedPropertyIdList);

    }
    private void doDI(Set<String> beanNeedPropertyIdList){
        for (String beanId : beanNeedPropertyIdList) {
            Object o = container.get(beanId);
            Class<?> aClass = o.getClass();
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {

                MyAutowired annotation = declaredField.getAnnotation(MyAutowired.class);
                if (annotation != null){
                    Class<?> type = declaredField.getType();
                    declaredField.setAccessible(true);

                    try {
                        declaredField.set(o,findProperty(type));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }



    private Object findProperty(Class<?> aClass){
        for (Map.Entry<String, Object> stringObjectEntry : container.entrySet()) {
            Class<?> aClass1 = stringObjectEntry.getValue().getClass();
            if (aClass.isAssignableFrom(aClass1)){
                return stringObjectEntry.getValue();
            }
        }
        return null;
    }
    private List<String> classNameList(String basePackages){
        List<String> classNameList = new ArrayList<>();

        List<MetadataReader> metaDataReaderList = getMetaDataReaderList(basePackages);

        for (MetadataReader metadataReader : metaDataReaderList) {

            String className = metadataReader.getClassMetadata().getClassName();
            AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

            if (annotationMetadata.hasAnnotation("com.xingyun.mvc.annotation.MyService")
                    ||annotationMetadata.hasAnnotation("com.xingyun.mvc.annotation.MyController")){
                System.out.println(className);
                classNameList.add(className);
            }
        }
        return classNameList;

    }

    private List<MetadataReader> getMetaDataReaderList(String basePackage){
        String resourcePattern = "**/*.class";
        String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

        String resourcePath = basePackage.replace('.','/');

        String packageSearchPath = CLASSPATH_ALL_URL_PREFIX +
                resourcePath + '/' + resourcePattern;

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();

        Resource[] resources = new Resource[0];
        try {
            resources = pathMatchingResourcePatternResolver.getResources(packageSearchPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<MetadataReader> metadataReaderList = new ArrayList<>();
        CachingMetadataReaderFactory cachingMetadataReaderFactory = new CachingMetadataReaderFactory();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                try {

                    MetadataReader metadataReader = cachingMetadataReaderFactory.getMetadataReader(resource);
                    metadataReaderList.add(metadataReader);
                    String className = metadataReader.getClassMetadata().getClassName();
                    AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();

                    boolean b = annotationMetadata.hasAnnotation("org.springframework.context.annotation.Configuration");
                    if (b){
                        System.out.println(className);
                    }
                }
                catch (Throwable ex) {
                    throw new BeanDefinitionStoreException(
                            "Failed to read candidate component class: " + resource, ex);
                }
            }
        }
        return metadataReaderList;

    }


    private String getBeanName(String className){
        String substring = className.substring(className.lastIndexOf(".")+1);
        return substring.substring(0,1).toLowerCase() + substring.substring(1);
    }

    private void needPropertyOrProxy(String beanId,Set<String> beanNeedPropertyIdList){
        Class<?> aClass = container.get(beanId).getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        //属性上有MyAutowired ，则需要注入属性
        for (Field declaredField : declaredFields) {

            MyAutowired annotation = declaredField.getAnnotation(MyAutowired.class);
            if (annotation != null){
                beanNeedPropertyIdList.add(beanId);
            }
        }

    }



}
