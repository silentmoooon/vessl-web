package org.vessl.web.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {

    public void connect(int port,String host) throws Exception{
        //配置客户端NIO线程组,客户端处理I/O读写的NioEventLoopGroup线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //客户端辅助启动类Bootstrap
            Bootstrap bootstrap = new Bootstrap();
            //设置线程组
            bootstrap.group(group)
                //与服务端不同的是，它的channel需要设置为NioSocketChannel
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                //然后为其添加Handler,此处为了简单直接创建匿名内部类，实现initChannel方法
                //作用是当创建NioSocketChannel成功之后，在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new TimeClientHandler());
                    }
                });
            //调用connect发起异步连接操作，然后调用sync同步方法等待连接成功。
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //等待客户端链路关闭，当客户端连接关闭之后，客户端主函数退出，退出之前释放NIO线程组的资源
            future.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) throws Exception{
        int port = 8080;
        String host = "127.0.0.1";

        new TimeClient().connect(port, host);
    }
    
}