package org.vessl.bind;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.vessl.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class WebPathHandleMapping {
    Table<String,String,MethodHandle> pathHandleMapping= HashBasedTable.create();
    //private Map<String, MethodHandle> pathHandleMapping = new HashMap<>();
    Multimap<String, Pattern> regPathPatterns = LinkedListMultimap.create();

   // private List<Pattern> regPathPatterns = new ArrayList<>();

    public void register(RequestMethod httpMethod,String path,Object object,Method method){

        if(path.contains("{")&&path.contains("}")){
            String regPath = getRegPath(path);
            List<String> pathParams = getPathParams(path);
            MethodHandle methodHandle = new MethodHandle(method, object);
            methodHandle.setPathParamName(pathParams);
            methodHandle.setRegex(true);
            MethodHandle m = pathHandleMapping.put(httpMethod.toString(),regPath, methodHandle);
            if(m==null){
                Pattern p = Pattern.compile(regPath);
                methodHandle.setPathRegex(p);
                regPathPatterns.put(httpMethod.toString(),p);
            }
        }else{
            pathHandleMapping.put(httpMethod.toString(),path, new MethodHandle(method,object));
        }
    }

    public MethodHandle getHandle(RequestMethod httpMethod,String path){

        if(pathHandleMapping.contains(httpMethod.toString(),path)){
            return pathHandleMapping.get(httpMethod.toString(),path);
        }
        for (Pattern regPathPattern : regPathPatterns.get(httpMethod.toString())) {
            if(regPathPattern.matcher(path).matches()){
                return pathHandleMapping.get(httpMethod.toString(),regPathPattern.toString());
            }
        }
        return null;


    }
    private String getRegPath(String path){
        String[] pathSplit = path.substring(1).split("/");
        StringBuilder regPath = new StringBuilder();
        for (String s : pathSplit) {
            regPath.append("/");
            if(s.startsWith("{")&&s.endsWith("}")){
                regPath.append("(.*)");
            }else{
                regPath.append(s);
            }
        }
        return regPath.toString();
    }

    private List<String> getPathParams(String path) {
        String[] pathSplit = path.split("/");
        List<String> pathParams = new ArrayList<>();
        for (String s : pathSplit) {
            if(s.startsWith("{")&&s.endsWith("}")){
                pathParams.add(s.substring(1, s.length() - 1));
            }
        }
        return pathParams;
    }


}
