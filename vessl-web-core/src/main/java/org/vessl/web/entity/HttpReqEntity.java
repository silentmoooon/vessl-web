package org.vessl.web.entity;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class HttpReqEntity {
    private String reqLine;
    private String version;
    private String url;
    private String method;
    private String paramString;
    private Map<String, String> headers = new HashMap<>();
    private String contentType;
    private int contentLength;
    private boolean keepAlive;
    private String body;

    public String getParams(){
        if (contentType.equals(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())) {
            return StringUtils.hasLength(paramString)?paramString:body;
        }else{
            return paramString;
        }
    }

    public int getBodyLength(){
        if (StringUtils.hasLength(body)) {
            return body.length();
        }
        return 0;
    }

}
