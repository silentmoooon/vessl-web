package org.vessl.webtest;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.vessl.annotation.*;

@VesslWeb("vessl")
public class WebController {

    @Autowired
    WebService webService;
    @Get("getName")
    public String getName(){
        return webService.getName();
    }
    @Get("getName1")
    public String getName1(@Param String name){
        return name+" "+webService.getName();
    }
    @Get("getName2/{name}")
    public String getName2(@PathParam String name){
        return name+" "+webService.getName();
    }
    @Get("getName3")
    public String getName3(@Body String name){
        System.out.println(name);
        return name+" "+webService.getName();
    }
    @Get("getName4")
    public String getName4(@Body TestBean name){
        System.out.println(JSON.toJSONString(name));
        return name.getAge()+" "+webService.getName();
    }
}
