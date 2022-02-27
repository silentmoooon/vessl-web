package org.vessl.webtest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Logger;

public class TimeClientHandler extends SimpleChannelInboundHandler<Object> {
    
    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());
    
    private int counter;
    
    private byte[] req;
    
    
    public TimeClientHandler(){

        
    }
    
    //当客户端和服务端TCP链路建立成功之后，Netty的NIO线程会调用channelActive方法，发送查询时间的指令给服务端
    //调用ChannelHandlerContext的writeAndFlush方法将请求消息发送给客户端
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

            ByteBuf message = null;
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
    }
    
    //当客户端返回应答消息，channelRead方法被调用
    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("Now is :" + body + " ; the counter is : " + ++counter);
    }



    //发生异常时，释放客户端资源
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warning("Unexpected exception from downstream : " + cause.getMessage());
        ctx.close();
    }
    
}
