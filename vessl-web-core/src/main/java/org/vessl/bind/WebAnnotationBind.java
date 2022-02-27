package org.vessl.bind;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.vessl.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WebAnnotationBind implements ApplicationListener<ContextRefreshedEvent> {
@Autowired
WebPathHandleMapping webPathHandleMapping;
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 根容器为Spring容器
        if(event.getApplicationContext().getParent()==null){
            Map<String,Object> beans = event.getApplicationContext().getBeansWithAnnotation(VesslWeb.class);
            for(Object bean : beans.values()){
                VesslWeb ca = bean.getClass().getAnnotation(VesslWeb.class);
                String basePath= ca.value();
                Method[] methods = bean.getClass().getMethods();
                for (Method declaredMethod : methods) {
                    Get get = AnnotationUtils.findAnnotation(declaredMethod, Get.class);
                    if(get != null){
                            System.out.println(RequestMethod.GET+"==="+"/"+basePath+"/"+get.value().replaceAll("//","/"));
                            webPathHandleMapping.register(RequestMethod.GET,("/"+basePath+"/"+get.value()).replaceAll("//","/"),bean,declaredMethod);

                    }
                    Post post = AnnotationUtils.findAnnotation(declaredMethod, Post.class);
                    if(post != null){
                        System.out.println(RequestMethod.POST+"==="+"/"+basePath+"/"+get.value().replaceAll("//","/"));
                        webPathHandleMapping.register(RequestMethod.POST,("/"+basePath+"/"+get.value()).replaceAll("//","/"),bean,declaredMethod);

                    }
                    HttpMethod httpMethod = AnnotationUtils.findAnnotation(declaredMethod, HttpMethod.class);
                    if(httpMethod != null){
                        for (RequestMethod requestMethod : httpMethod.method()) {
                            System.out.println(requestMethod+"==="+"/"+basePath+"/"+get.value().replaceAll("//","/"));
                            webPathHandleMapping.register(requestMethod,("/"+basePath+"/"+get.value()).replaceAll("//","/"),bean,declaredMethod);
                        }

                    }

                }

            }

        }

    }


}
