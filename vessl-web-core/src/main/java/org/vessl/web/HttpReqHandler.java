package org.vessl.web;

import com.alibaba.fastjson.JSON;
import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.springframework.util.StringUtils;
import org.vessl.annotation.*;
import org.vessl.bind.MethodHandle;
import org.vessl.bind.WebPathHandleMapping;
import org.vessl.constant.WebConstant;
import org.vessl.entity.HttpReqEntity;
import org.vessl.entity.HttpRspEntity;
import org.vessl.exception.HttpException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpReqHandler extends SimpleChannelInboundHandler<HttpReqEntity> {
    private WebPathHandleMapping webPathHandleMapping;

    public HttpReqHandler(WebPathHandleMapping webPathHandleMapping) {
        this.webPathHandleMapping = webPathHandleMapping;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    //接收到客户都发送的消息
    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpReqEntity reqMsg) throws Exception {
        System.out.println("channel:"+ctx.channel().id());
        String uri = reqMsg.getUrl();

        String httpMethod = reqMsg.getMethod();

        String paramString = reqMsg.getParams();

        String rspMsg = null;
        try {
            MethodHandle handle = webPathHandleMapping.getHandle(RequestMethod.valueOf(httpMethod), uri);
            if (handle == null) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND, "page not found");

            }
            Map<String, String> paramMap = resoleParamString(paramString);
            List<String> pathParamName = handle.getPathParamName();
            Map<String, String> pathParam = new HashMap<>();

            if (handle.isRegex()) {
                Pattern pathRegex = handle.getPathRegex();
                Matcher matcher = pathRegex.matcher(uri);
                if(matcher.find()) {
                    for (int i = 0; i < pathParamName.size(); i++) {
                        pathParam.put(pathParamName.get(i), matcher.group(i + 1));
                    }
                }
            }
            Method method = handle.getMethod();
            Parameter[] parameters = method.getParameters();
            Object[] invokeParam = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Body body = parameters[i].getAnnotation(Body.class);
                if (body != null && body.required()) {
                    if (!StringUtils.hasLength(reqMsg.getBody())) {
                        throw new HttpException(HttpResponseStatus.BAD_REQUEST, "request body is Require");
                    }
                    Class<?> type = parameters[i].getType();
                    if (type == String.class || type == Object.class) {
                        invokeParam[i] = reqMsg.getBody();
                    } else {
                        try {
                            if(reqMsg.getContentType().equals(HttpHeaderValues.APPLICATION_JSON.toString())){
                                invokeParam[i] = JSON.parseObject(reqMsg.getBody(), type);
                            }else{
                                throw new HttpException(HttpResponseStatus.BAD_REQUEST, "request body is invalid");
                            }

                        } catch (Exception e) {
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST, "request body is invalid");
                        }
                    }
                    continue;
                }
                Param param = parameters[i].getAnnotation(Param.class);
                if(param != null) {
                    String paramKey = param.value();
                    if (!StringUtils.hasLength(paramKey)) {
                        paramKey = parameters[i].getName();
                    }
                    if (param.required()) {
                        if (!paramMap.containsKey(paramKey)) {
                            throw new HttpException(HttpResponseStatus.BAD_REQUEST, "request param " + param.value() + " is Require");
                        }
                        invokeParam[i] = paramMap.get(paramKey);
                        continue;
                    }
                }
                if (handle.isRegex()) {
                    PathParam annotation = parameters[i].getAnnotation(PathParam.class);
                    if(annotation != null) {
                        String paramKey = annotation.value();
                        if (!StringUtils.hasLength(paramKey)) {
                            paramKey = parameters[i].getName();
                        }
                        if (annotation.required()) {
                            if (!pathParam.containsKey(paramKey)) {
                                throw new HttpException(HttpResponseStatus.BAD_REQUEST, "path param " + annotation.value() + " is Require");
                            }
                            invokeParam[i] = pathParam.get(paramKey);
                            continue;
                        }
                    }
                }
                invokeParam[i] = null;

            }

            Object invoke = handle.getMethod().invoke(handle.getObject(),invokeParam);
            rspMsg = invoke.toString();
        } catch (HttpException e) {
            HttpRspEntity rspEntity = new HttpRspEntity(WebConstant.HTTP_VERSION_1_1, e.getStatus());
            write(ctx,rspEntity,reqMsg.isKeepAlive());
            return;
        }


        HttpRspEntity rspEntity = new HttpRspEntity(WebConstant.HTTP_VERSION_1_1, HttpResponseStatus.OK);
        rspEntity.setBody(rspMsg);
        write(ctx,rspEntity,reqMsg.isKeepAlive());

    }

    private void write(ChannelHandlerContext ctx,HttpRspEntity rspEntity,boolean keepAlive){
        rspEntity.getHeaders().put(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON.toString());
        rspEntity.getHeaders().put(HttpHeaders.CONTENT_LENGTH, String.valueOf(rspEntity.getBody().length()));
        if(keepAlive){
            rspEntity.getHeaders().put(HttpHeaders.CONNECTION, HttpHeaderValues.KEEP_ALIVE.toString());
            ctx.writeAndFlush(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(rspEntity.toString()), StandardCharsets.UTF_8));

        }else{
            ctx.writeAndFlush(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(rspEntity.toString()), StandardCharsets.UTF_8)).addListener(ChannelFutureListener.CLOSE);

        }
    }

    private Map<String, String> resoleParamString(String paramString) {
        Map<String, String> paramMap = new HashMap<>();
        if (!StringUtils.hasLength(paramString)) {
            return paramMap;
        }
        paramString = URLDecoder.decode(paramString, StandardCharsets.UTF_8);
        String[] split = paramString.split("&");
        for (String s : split) {
            if(!StringUtils.hasLength(s)){
                continue;
            }
            String[] split1 = s.split("=");
            if (split1.length == 1) {
                continue;
            }
            paramMap.put(split1[0], split1[1]);
        }
        return paramMap;
    }

    //客户端建立连接
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        //System.out.println(ctx.channel().remoteAddress()+"连接了!");
    }

    //关闭连接
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        //System.out.println(ctx.channel().remoteAddress()+"断开连接");
    }

    //出现异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    public static void main(String[] args) {
        Pattern p = Pattern.compile("/vessl/getName2/(.*)");
        Matcher matcher = p.matcher("/vessl/getName2/xiecan");
        System.out.println(matcher.matches());
        System.out.println(matcher.groupCount());
        for (int i = 1; i <= matcher.groupCount(); i++) {
            System.out.println(matcher.group(i));
        }
    }
}